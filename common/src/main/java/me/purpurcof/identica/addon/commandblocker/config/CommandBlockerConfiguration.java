package me.purpurcof.identica.addon.commandblocker.config;

import me.whereareiam.configura.Configura;

import java.nio.file.Path;
import java.util.List;

public class CommandBlockerConfiguration {

    private final Configura config;
    private final Path path;
    private volatile CommandBlockerConfig data;

    /**
     * @param base Identica's shared Configura instance ({@code IdenticaAPI.getConfigura()}); the settings file
     *             is read and written in the format Identica is configured with, using Identica's own copy of
     *             the library
     */
    public CommandBlockerConfiguration(Path dataDirectory, Configura base) {
        this.config = base.withDefaults(CommandBlockerConfigDefaults.class);
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

    public List<String> getAllowedCommands() {
        return data.getAllowedCommands();
    }

    public String getBlockedMessage() {
        return data.getBlockedMessage();
    }
}