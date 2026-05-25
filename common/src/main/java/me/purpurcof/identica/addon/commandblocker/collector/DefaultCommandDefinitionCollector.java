package me.purpurcof.identica.addon.commandblocker.collector;

import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.whereareiam.identica.Reloadable;

import java.util.Set;

public class DefaultCommandDefinitionCollector implements CommandDefinitionCollector, Reloadable {

    private final CommandBlockerConfiguration config;

    public DefaultCommandDefinitionCollector(CommandBlockerConfiguration config) {
        this.config = config;
    }

    @Override
    public Set<String> getAllowedDuringAuthAliases() {
        return config.getAllowedCommands();
    }

    @Override
    public void reload() {
        config.reload();
    }
}