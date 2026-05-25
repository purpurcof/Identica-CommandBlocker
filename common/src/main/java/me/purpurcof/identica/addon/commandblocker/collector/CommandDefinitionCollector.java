package me.purpurcof.identica.addon.commandblocker.collector;

import java.util.Set;

public interface CommandDefinitionCollector {

    Set<String> getAllowedDuringAuthAliases();
}