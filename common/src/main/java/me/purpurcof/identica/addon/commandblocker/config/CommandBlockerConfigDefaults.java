package me.purpurcof.identica.addon.commandblocker.config;

import me.whereareiam.configura.merge.defaults.DefaultsProvider;

import java.util.List;

public class CommandBlockerConfigDefaults implements DefaultsProvider<CommandBlockerConfig> {

    @Override
    public CommandBlockerConfig supply(CommandBlockerConfig config) {
        config.getAllowedCommands().addAll(List.of(
                "login", "l", "pass", "passconfirm",
                "auth", "identica",
                "enroll",
                "2fa",
                "credential"
        ));
        config.setPrefix("<green>Identica</green> <dark_gray>| ");
        config.setBlockedMessage("<white>You must <red>authenticate</red> before using this command.</white>");
        return config;
    }
}