package me.purpurcof.identica.addon.commandblocker.testing.environment;

import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfig;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfigDefaults;
import me.whereareiam.anvil.api.model.scenario.AnvilScenario;
import me.whereareiam.anvil.integration.junit.AnvilScenarioFactory;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds the network a {@link CommandBlockerNetwork} declaration describes.
 */
public final class CommandBlockerScenarios implements AnvilScenarioFactory<CommandBlockerNetwork> {

	private static final Configura CONFIGURA = Config.builder().format(Format.YAML).build();

	@Override
	public @NotNull AnvilScenario create(@NotNull CommandBlockerNetwork declaration) {
		return IdenticaNetwork.velocity(name(declaration), settings(declaration));
	}

	/**
	 * The addon's settings file, written through the addon's own model and defaults. A declaration that allows
	 * nothing writes none, so the addon generates its settings as on a new installation.
	 */
	private static Map<String, String> settings(@NotNull CommandBlockerNetwork declaration) {
		if (declaration.allowedCommands().length == 0) return Map.of();

		CommandBlockerConfig config = new CommandBlockerConfigDefaults().supply(new CommandBlockerConfig());
		config.setAllowedCommands(List.of(declaration.allowedCommands()));
		return Map.of("settings.yml", new String(CONFIGURA.writeBytes(config), StandardCharsets.UTF_8));
	}

	private static String name(@NotNull CommandBlockerNetwork declaration) {
		String allowed = declaration.allowedCommands().length == 0
				? "defaults"
				: String.join("+", Arrays.asList(declaration.allowedCommands()));
		return ("commandblocker-" + allowed).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9+_-]", "_");
	}
}