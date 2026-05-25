package me.purpurcof.identica.addon.commandblocker.config;

import me.whereareiam.configura.merge.defaults.MergeDefaultsProvider;

import java.util.List;

public class CommandBlockerConfigDefaults implements MergeDefaultsProvider<CommandBlockerConfig> {

    @Override
    public CommandBlockerConfig supply(CommandBlockerConfig config) {
        config.getAllowedCommands().addAll(List.of(
                "login", "l", "pass", "passconfirm",
                "auth", "identica",
                "enroll",
                "2fa", "2fa status", "2fa enroll", "2fa confirm",
                "2fa enroll confirm", "2fa use", "2fa disable", "2fa enroll cancel",
                "credential", "credential confirm", "credential cancel"
        ));
        config.setPrefix("<green>Identica</green> <dark_gray>| ");
        config.setBlockedMessage("<white>You must <red>authenticate</red> before using this command.</white>");
        return config;
    }
}