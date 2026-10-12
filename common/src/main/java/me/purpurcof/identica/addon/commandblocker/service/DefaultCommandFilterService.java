package me.purpurcof.identica.addon.commandblocker.service;

import lombok.RequiredArgsConstructor;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.util.AliasNormalizer;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.identica.pipeline.PendingScenarioService;
import me.whereareiam.keystone.Actor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
public class DefaultCommandFilterService implements CommandFilterService {

    private final PendingScenarioService pendingScenarios;
    private final CommandDefinitionCollector definitionCollector;

    @Override
    public boolean isAllowed(@NotNull Actor actor, @NotNull String commandLine) {
        if (commandLine.isBlank()) return false;

        if (!(actor instanceof Identity identity)) return true;

        UUID connectionUniqueId = identity.getConnectionUniqueId();
        if (connectionUniqueId == null || !isBlocked(connectionUniqueId)) return true;

        return isAllowedCommand(commandLine);
    }

    @Override
    public boolean isAllowed(@NotNull UUID connectionUniqueId, @NotNull String commandLine) {
        if (commandLine.isBlank()) return false;
        if (!isBlocked(connectionUniqueId)) return true;

        return isAllowedCommand(commandLine);
    }

    @Override
    public boolean isBlocked(@NotNull UUID connectionUniqueId) {
        // a connection without a UUID is not one Identica follows
        return connectionUniqueId != null && pendingScenarios.isPending(connectionUniqueId);
    }

    private boolean isAllowedCommand(String commandLine) {
        String normalized = AliasNormalizer.normalize(commandLine);
        if (normalized.isEmpty()) return false;

        return AliasNormalizer.matchesAllowed(definitionCollector.getAllowedDuringAuthAliases(), normalized);
    }
}