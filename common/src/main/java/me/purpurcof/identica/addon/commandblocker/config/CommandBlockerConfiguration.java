package me.purpurcof.identica.addon.commandblocker.config;

import me.whereareiam.configura.Config;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import me.whereareiam.identica.config.ConfigurationTypeResolver;

import java.nio.file.Path;
import java.util.Set;

public class CommandBlockerConfiguration {

    private final Configura config;
    private final Path path;
    private CommandBlockerConfig data;

    public CommandBlockerConfiguration(Path dataDirectory) {
        this(dataDirectory, null);
    }

    public CommandBlockerConfiguration(Path dataDirectory, ConfigurationTypeResolver typeResolver) {
        Format format = typeResolver != null ? typeResolver.getConfigurationType() : Format.YAML;
        this.config = Config.builder()
                .format(format)
                .defaults(CommandBlockerConfigDefaults.class)
                .build();
        this.path = dataDirectory.resolve("settings");
        this.data = load();
    }

    private CommandBlockerConfig load() {
        return config.update(path, CommandBlockerConfig.class);
    }

    public void reload() {
        data = load();
    }

    public String getPrefix() {
        return data.getPrefix();
    }

    public Set<String> getAllowedCommands() {
        return data.getAllowedCommands();
    }

    public String getBlockedMessage() {
        return data.getBlockedMessage();
    }
}