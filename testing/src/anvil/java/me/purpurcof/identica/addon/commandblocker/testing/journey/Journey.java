package me.purpurcof.identica.addon.commandblocker.testing.journey;

import me.purpurcof.identica.addon.commandblocker.testing.environment.IdenticaNetwork;
import me.whereareiam.anvil.api.player.SimulatedPlayer;
import me.whereareiam.anvil.api.process.ConsoleWatch;
import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.capability.messages.MessageWatch;
import me.whereareiam.anvil.capability.messages.Messages;
import me.whereareiam.anvil.capability.players.PlayerWatch;
import me.whereareiam.anvil.capability.players.Players;
import me.whereareiam.anvil.capability.session.Session;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * One offline player's journey through the test network. Expectations check what the player's client receives and,
 * for connections and server changes, what the proxy reports on its own console, so a journey fails when either
 * Identica, the addon or the platform behaves differently.
 *
 * <p>Message expectations only consider messages received since the previous expectation, so a prompt that
 * repeats is not satisfied by its earlier occurrence.</p>
 */
public final class Journey {

	private static final Duration TIMEOUT = Duration.ofSeconds(20);
	private static final Duration SETTLE = Duration.ofSeconds(3);

	private final String name;
	private final SimulatedPlayer player;
	private final Session session;
	private final Messages messages;
	private final MessageWatch messagesWatch;
	private final Players players;
	private final PlayerWatch playersWatch;
	private final ConsoleWatch proxy;
	private final IdenticaMessages configured;

	private Journey(ScenarioContext anvil, SimulatedPlayer player, String name) {
		var runningProxy = anvil.processes().proxy(IdenticaNetwork.PROXY);
		this.name = name;
		this.player = player;
		this.session = player.capability(Session.class);
		this.messages = player.capability(Messages.class);
		this.messagesWatch = messages.watch();
		this.players = runningProxy.capability(Players.class);
		this.playersWatch = players.watch();
		this.proxy = runningProxy.console().watchFromStart();
		this.configured = new IdenticaMessages(runningProxy.workDirectory().resolve("plugins"));
	}

	/**
	 * Creates an offline player that has not connected yet.
	 */
	public static Journey offline(ScenarioContext anvil, String name) {
		return new Journey(anvil, anvil.players().create(name), name);
	}

	/**
	 * Connects and expects the proxy to accept the connection.
	 */
	public Journey join() {
		// Identica becomes ready after the proxy reports it is done, and the addon follows; a player who joins earlier
		// is not held back by an addon that has not started yet.
		proxy.printed(IdenticaNetwork.READY, Duration.ofSeconds(60));
		session.connect();
		session.connected(Duration.ofSeconds(30));
		reported("[connected player] " + name + " (", "has connected");
		return this;
	}

	/**
	 * Expects the player on a backend: observed by that server's agent and reported by the proxy.
	 */
	public Journey on(String backend) {
		playersWatch.joined(player, backend, TIMEOUT);
		reported("[server connection] " + name + " -> " + backend, "has connected");
		return this;
	}

	/**
	 * Expects the player to stay on a backend for a while, without the proxy connecting them anywhere else.
	 */
	public Journey remainsOn(String backend) {
		Instant deadline = Instant.now().plus(SETTLE);
		while (Instant.now().isBefore(deadline)) {
			assertEquals(Optional.of(backend), players.backend(player), name + " must stay on " + backend);
			assertTrue(session.state().connected(), name + " must stay connected");
			sleep();
		}

		return this;
	}

	/**
	 * Expects a message, arrived after the previous expectation, that contains every line of a configured message.
	 *
	 * @param message selects the message, a line or a list of lines, from the proxy's configuration
	 */
	public Journey sees(Function<IdenticaMessages, ?> message) {
		MessageText expected = new MessageText(message.apply(configured));
		receive(expected::in, expected);
		return this;
	}

	/**
	 * Expects that no message containing a configured message has arrived since the previous expectation, after
	 * giving it time to.
	 */
	public Journey doesNotSee(Function<IdenticaMessages, ?> message) {
		MessageText unexpected = new MessageText(message.apply(configured));
		messagesWatch.neverReceived(unexpected::in, SETTLE);
		return this;
	}

	/**
	 * Expects a message, arrived after the previous expectation, that contains a text, for what no configuration
	 * file holds, such as a chat message.
	 */
	public Journey seesText(String text) {
		receive(received -> received.contains(text), new MessageText(text));
		return this;
	}

	/**
	 * Expects that no message containing a text has arrived since the previous expectation, after giving it time to.
	 */
	public Journey doesNotSeeText(String text) {
		messagesWatch.neverReceived(received -> received.contains(text), SETTLE);
		return this;
	}

	public Journey enroll() {
		return command("identica enroll credential");
	}

	/**
	 * Completes Credential's two registration steps. Each step waits for the prompt the provider announces before
	 * it reads a command, so a password only follows the step that asks for it.
	 */
	public Journey register(String password) {
		command("pass " + password);
		sees(m -> m.credential().getScenario().getRegistration().getConfirmPrompt());
		return command("passconfirm " + password);
	}

	public Journey command(String command) {
		messages.command(command);
		return this;
	}

	/**
	 * Sends a chat message, as the player types it without a leading slash.
	 */
	public Journey chat(String message) {
		messages.chat(message);
		return this;
	}

	/**
	 * Returns the messages received since the previous expectation, without consuming them.
	 */
	private List<String> unread() {
		return List.copyOf(messagesWatch.unread());
	}

	/**
	 * Waits for a message after the previous expectation and moves this journey's position behind it.
	 */
	private void receive(Predicate<String> matcher, MessageText expected) {
		try {
			messagesWatch.received(matcher, TIMEOUT);
		} catch (IllegalStateException timeout) {
			fail(name + " did not receive " + expected + "; new messages: " + unread(), timeout);
		}
	}

	/**
	 * Waits for a proxy console line after the last one this journey matched.
	 */
	private void reported(String subject, String event) {
		try {
			proxy.printed(line -> line.contains(subject) && line.contains(event), TIMEOUT);
		} catch (IllegalStateException timeout) {
			fail("The proxy did not report \"" + subject + " ... " + event + "\"", timeout);
		}
	}

	private static void sleep() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted during a journey", interrupted);
		}
	}
}