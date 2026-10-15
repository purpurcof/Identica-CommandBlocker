package me.purpurcof.identica.addon.commandblocker.testing;

import me.purpurcof.identica.addon.commandblocker.testing.environment.AllowedServerCommand;
import me.purpurcof.identica.addon.commandblocker.testing.environment.DefaultSettings;
import me.purpurcof.identica.addon.commandblocker.testing.environment.IdenticaNetwork;
import me.purpurcof.identica.addon.commandblocker.testing.journey.Player;
import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.integration.junit.AnvilTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * The packaged addon on a real Velocity proxy next to Identica. A command is held back from a player who has not
 * authenticated, except those Identica needs and those the settings allow, and nothing is held back afterwards.
 */
final class CommandBlockerTest {
	private static final String PASSWORD = "Secret-123";
	/** Plain text of the default blocked message. */
	private static final String BLOCKED = "before using this command";
	/** What Velocity's own {@code /server} answers with: the server the player stands on. */
	private static final String SERVER_ANSWER = IdenticaNetwork.LOBBY;

	@Test
	@AnvilTest(DefaultSettings.class)
	void startsOnceIdenticaIsReady(ScenarioContext anvil) {
		anvil.processes().proxy(IdenticaNetwork.PROXY).console()
				.await("Identica-CommandBlocker initialized", 0, Duration.ofSeconds(60));
	}

	@Test
	@AnvilTest(DefaultSettings.class)
	void holdsBackACommandWhileThePlayerAuthenticates(ScenarioContext anvil) {
		Player.join(anvil, "Alice")
				.command("server " + IdenticaNetwork.LOBBY)
				.seesText(BLOCKED)
				.remainsOnLobby();
	}

	@Test
	@AnvilTest(DefaultSettings.class)
	void letsIdenticasOwnCommandsThrough(ScenarioContext anvil) {
		Player.join(anvil, "Alice")
				.command("identica enroll credential")
				.doesNotSeeText(BLOCKED);
	}

	@Test
	@AnvilTest(AllowedServerCommand.class)
	void letsThroughACommandTheSettingsAllow(ScenarioContext anvil) {
		Player.join(anvil, "Alice")
				.command("server")
				.seesText(SERVER_ANSWER)
				.doesNotSeeText(BLOCKED);
	}

	@Test
	@AnvilTest(DefaultSettings.class)
	void holdsNothingBackOnceThePlayerHasRegistered(ScenarioContext anvil) {
		Player.join(anvil, "Alice")
				.register(PASSWORD)
				.command("server")
				.seesText(SERVER_ANSWER)
				.doesNotSeeText(BLOCKED);
	}
}
