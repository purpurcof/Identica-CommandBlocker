package me.purpurcof.identica.addon.commandblocker.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CommandBlockerConfig {

    private List<String> allowedCommands = new ArrayList<>();
    private String prefix;
    private String blockedMessage;
}