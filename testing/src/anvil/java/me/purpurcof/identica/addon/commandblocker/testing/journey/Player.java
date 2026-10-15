package me.purpurcof.identica.addon.commandblocker.testing.journey;

import me.purpurcof.identica.addon.commandblocker.testing.environment.IdenticaNetwork;
import me.whereareiam.anvil.api.player.SimulatedPlayer;
import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.capability.messages.Messages;
import me.whereareiam.anvil.capability.server.Server;
import me.whereareiam.anvil.capability.session.Session;

import java.time.Duration;

/**
 * One offline player's journey on the test network. Every expectation only considers messages received since the
 * previous command, so a text that repeats is not satisfied by its earlier occurrence.
 */
public final class Player {
	private static final Duration TIMEOUT = Duration.ofSeconds(20);
	private static final Duration SETTLE = Duration.ofSeconds(3);
	/** A line of Identica's default enrollment prompt, the first thing a pending player is shown. */
	private static final String PROMPT = "Select an authentication method";

	private final Session session;
	private final Messages messages;
	private final Server server;
	private int read;

	private Player(SimulatedPlayer player) {
		this.session = player.capability(Session.class);
		this.messages = player.capability(Messages.class);
		this.server = player.capability(Server.class);
	}

	/**
	 * Connects an offline player and waits until the proxy has put them on the lobby and Identica has shown its
	 * prompt, the sign that the player is pending. A command sent earlier could still reach the server.
	 */
	public static Player join(ScenarioContext anvil, String name) {
		Player player = new Player(anvil.players().create(name));
		player.session.connect();
		player.session.connected(TIMEOUT);
		player.server.joined(IdenticaNetwork.LOBBY, TIMEOUT);
		player.read = player.messages.received(message -> message.contains(PROMPT), 0, TIMEOUT).getCheckpoint();
		return player;
	}

	public Player command(String command) {
		read = messages.checkpoint();
		messages.command(command);
		return this;
	}

	/** Expects a message containing the text, received after the previous command. */
	public Player seesText(String text) {
		read = messages.received(message -> message.contains(text), read, TIMEOUT).getCheckpoint();
		return this;
	}

	/** Expects that no message containing the text arrives for a few seconds after the previous command. */
	public Player doesNotSeeText(String text) {
		messages.notReceived(message -> message.contains(text), read, SETTLE);
		return this;
	}

	/** Waits for any answer to the previous command. */
	private void answered() {
		read = messages.received(message -> !message.isBlank(), read, TIMEOUT).getCheckpoint();
	}

	/** Completes Credential's two registration steps, after which the player has authenticated. */
	public Player register(String password) {
		command("identica enroll credential");
		answered();
		command("pass " + password);
		answered();
		command("passconfirm " + password);
		answered();
		return this;
	}

	public Player remainsOnLobby() {
		server.stayed(IdenticaNetwork.LOBBY, SETTLE);
		return this;
	}
}
