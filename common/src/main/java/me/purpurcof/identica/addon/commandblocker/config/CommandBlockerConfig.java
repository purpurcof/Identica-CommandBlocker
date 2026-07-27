package me.purpurcof.identica.addon.commandblocker.config;

import lombok.Getter;
import lombok.Setter;
import me.whereareiam.configura.ConfigDocument;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CommandBlockerConfig extends ConfigDocument {

    private List<String> allowedCommands = new ArrayList<>();
    private String prefix;
    private String blockedMessage;
}