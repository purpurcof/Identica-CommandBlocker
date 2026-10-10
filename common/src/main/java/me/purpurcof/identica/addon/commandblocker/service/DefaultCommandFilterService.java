package me.purpurcof.identica.addon.commandblocker.service;

import lombok.RequiredArgsConstructor;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.util.AliasNormalizer;
import me.whereareiam.identica.event.EventListener;
import me.whereareiam.identica.event.base.IdenticEvent;
import me.whereareiam.identica.type.event.EventOrder;
import me.whereareiam.identica.event.scenario.authentication.AuthenticationRequiredEvent;
import me.whereareiam.identica.event.scenario.authentication.AuthenticationResolvedEvent;
import me.whereareiam.identica.event.scenario.registration.RegistrationRequiredEvent;
import me.whereareiam.identica.event.scenario.registration.RegistrationResolvedEvent;
import me.whereareiam.identica.event.scenario.migration.MigrationRequiredEvent;
import me.whereareiam.identica.event.scenario.migration.MigrationResolvedEvent;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.keystone.Actor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class DefaultCommandFilterService implements CommandFilterService, EventListener {

    private final Set<UUID> blockedConnections = ConcurrentHashMap.newKeySet();

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
        return blockedConnections.contains(connectionUniqueId);
    }

    @IdenticEvent(EventOrder.HIGH)
    public void onAuthenticationRequired(AuthenticationRequiredEvent event) {
        blockedConnections.add(event.getConnectionUniqueId());
    }

    @IdenticEvent(EventOrder.HIGH)
    public void onAuthenticationResolved(AuthenticationResolvedEvent event) {
        blockedConnections.remove(event.getConnectionUniqueId());
    }

    @IdenticEvent(EventOrder.HIGH)
    public void onRegistrationRequired(RegistrationRequiredEvent event) {
        blockedConnections.add(event.getConnectionUniqueId());
    }

    @IdenticEvent(EventOrder.HIGH)
    public void onRegistrationResolved(RegistrationResolvedEvent event) {
        blockedConnections.remove(event.getConnectionUniqueId());
    }

    @IdenticEvent(EventOrder.HIGH)
    public void onMigrationRequired(MigrationRequiredEvent event) {
        blockedConnections.add(event.getConnectionUniqueId());
    }

    @IdenticEvent(EventOrder.HIGH)
    public void onMigrationResolved(MigrationResolvedEvent event) {
        blockedConnections.remove(event.getConnectionUniqueId());
    }

    private boolean isAllowedCommand(String commandLine) {
        String normalized = AliasNormalizer.normalize(commandLine);
        if (normalized.isEmpty()) return false;

        Set<String> allowedAliases = definitionCollector.getAllowedDuringAuthAliases();

        if (allowedAliases.contains(normalized)) return true;

        return allowedAliases.contains(AliasNormalizer.firstWord(normalized));
    }
}