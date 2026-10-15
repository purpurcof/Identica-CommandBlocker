package me.purpurcof.identica.addon.commandblocker.testing.environment;

import me.whereareiam.anvil.api.model.scenario.AnvilScenario;
import me.whereareiam.anvil.api.scenario.AnvilScenarioDefinition;

import java.util.Map;

/** The addon with {@code server} listed under {@code allowedCommands}. */
public final class AllowedServerCommand implements AnvilScenarioDefinition {
	@Override
	public AnvilScenario define() {
		return IdenticaNetwork.velocity("commandblocker-allows-server", Map.of("settings.yml", """
				allowedCommands:
				  - "server"
				"""));
	}
}
