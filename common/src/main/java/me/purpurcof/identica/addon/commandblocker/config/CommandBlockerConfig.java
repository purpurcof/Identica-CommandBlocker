package me.purpurcof.identica.addon.commandblocker.config;

import me.whereareiam.configura.ConfigDocument;

import java.util.ArrayList;
import java.util.List;

public class CommandBlockerConfig extends ConfigDocument {

    private List<String> allowedCommands = new ArrayList<>();
    private String prefix;
    private String blockedMessage;

    public List<String> getAllowedCommands() {
        return allowedCommands;
    }

    @SuppressWarnings("unused")
    public void setAllowedCommands(List<String> allowedCommands) {
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