package me.purpurcof.identica.addon.commandblocker.testing.environment;

import me.whereareiam.anvil.api.model.scenario.AnvilScenario;
import me.whereareiam.anvil.api.scenario.AnvilScenarioDefinition;

/** The addon with the settings it generates on a new installation. */
public final class DefaultSettings implements AnvilScenarioDefinition {
	@Override
	public AnvilScenario define() {
		return IdenticaNetwork.velocity("commandblocker-default");
	}
}
