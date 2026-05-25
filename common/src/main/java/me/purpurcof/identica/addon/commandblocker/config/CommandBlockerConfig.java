package me.purpurcof.identica.addon.commandblocker.config;

import me.whereareiam.configura.ConfigDocument;

import java.util.HashSet;
import java.util.Set;

public class CommandBlockerConfig extends ConfigDocument {

    private Set<String> allowedCommands = new HashSet<>();
    private String prefix;
    private String blockedMessage;

    public Set<String> getAllowedCommands() {
        return allowedCommands;
    }

    @SuppressWarnings("unused")
    public void setAllowedCommands(Set<String> allowedCommands) {
        this.allowedCommands = allowedCommands;
    }

    public String getPrefix() {
        return prefix;
    }

    @SuppressWarnings("unused")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getBlockedMessage() {
        return blockedMessage;
    }

    @SuppressWarnings("unused")
    public void setBlockedMessage(String blockedMessage) {
        this.blockedMessage = blockedMessage;
    }
}