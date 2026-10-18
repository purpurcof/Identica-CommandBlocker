package me.purpurcof.identica.addon.commandblocker.testing;

import me.purpurcof.identica.addon.commandblocker.testing.environment.CommandBlockerNetwork;
import me.purpurcof.identica.addon.commandblocker.testing.journey.Journey;
import me.whereareiam.anvil.api.scenario.ScenarioContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static me.purpurcof.identica.addon.commandblocker.testing.environment.IdenticaNetwork.AUTH;
import static me.purpurcof.identica.addon.commandblocker.testing.environment.IdenticaNetwork.LOBBY;
import static me.purpurcof.identica.addon.commandblocker.testing.environment.IdenticaNetwork.PROXY;
import static me.purpurcof.identica.addon.commandblocker.testing.environment.IdenticaNetwork.READY;

/**
 * The packaged addon on a real Velocity proxy next to Identica. A command is held back from a player who has not
 * authenticated, except the ones Identica needs and the ones the settings allow, and nothing is held back once the
 * player is done.
 */
class CommandBlockerTest {

	private static final String PASSWORD = "Secret-123";

	@Test
	@CommandBlockerNetwork
	void startsOnceIdenticaIsReady(ScenarioContext anvil) {
		// The addon reads Identica's commands whenever a player runs one, so nothing else has to be loaded first.
		anvil.processes().proxy(PROXY).console().watchFromStart().printed(READY, Duration.ofSeconds(60));
	}

	@Test
	@CommandBlockerNetwork
	void holdsBackOtherCommandsButLetsTheRegistrationCommandsThrough(ScenarioContext anvil) {
		Journey.offline(anvil, "Alice").join()
				.on(AUTH)
				.sees(m -> m.identica().getEngine().getJourney().getStep().getEnrollment().getBody())
				.enroll()
				.sees(m -> m.credential().getScenario().getRegistration().getPrompt())
				.doesNotSee(m -> m.commandBlocker().getBlockedMessage())
				.command("server " + LOBBY)
				// The message tells the addon held it back. Staying on auth proves little by itself, as Identica
				// sends a player who is still being authenticated back to auth.
				.sees(m -> m.commandBlocker().getBlockedMessage())
				.remainsOn(AUTH)
				.register(PASSWORD)
				.on(LOBBY);
	}

	@Test
	@CommandBlockerNetwork(allowedCommands = "server")
	void letsThroughACommandTheSettingsAllow(ScenarioContext anvil) {
		// The default network holds back server too. Here only the command the settings name gets through, while
		// another one is still held back, so the settings, and not a lenient addon, made the difference.
		Journey.offline(anvil, "Alice").join()
				.on(AUTH)
				.sees(m -> m.identica().getEngine().getJourney().getStep().getEnrollment().getBody())
				.command("server")
				.doesNotSee(m -> m.commandBlocker().getBlockedMessage())
				.command("glist")
				.sees(m -> m.commandBlocker().getBlockedMessage());
	}

	@Test
	@CommandBlockerNetwork
	void holdsNothingBackOnceThePlayerHasRegistered(ScenarioContext anvil) {
		Journey.offline(anvil, "Alice").join()
				.on(AUTH)
				.sees(m -> m.identica().getEngine().getJourney().getStep().getEnrollment().getBody())
				.enroll()
				.sees(m -> m.credential().getScenario().getRegistration().getPrompt())
				.register(PASSWORD)
				.on(LOBBY)
				.command("server")
				.doesNotSee(m -> m.commandBlocker().getBlockedMessage())
				.command("glist")
				.doesNotSee(m -> m.commandBlocker().getBlockedMessage());
	}
}