package me.purpurcof.identica.addon.commandblocker.service;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.whereareiam.identica.event.EventListener;
import me.whereareiam.identica.event.base.IdenticEvent;
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

public class DefaultCommandFilterService implements CommandFilterService, EventListener {

    private final CommandDefinitionCollector definitionCollector;
    private final Set<UUID> blockedConnections = ConcurrentHashMap.newKeySet();

    public DefaultCommandFilterService(CommandDefinitionCollector definitionCollector) {
        this.definitionCollector = definitionCollector;
    }

    @Override
    public boolean isAllowed(@NotNull Actor actor, @NotNull String commandLine) {
        if (commandLine.isBlank())
            return false;

        if (!(actor instanceof Identity identity))
            return true;

        UUID connectionUniqueId = identity.getConnectionUniqueId();
        if (connectionUniqueId == null || !blockedConnections.contains(connectionUniqueId))
            return true;

        return isAllowedCommand(commandLine);
    }

    @Override
    public boolean isBlocked(@NotNull UUID connectionUniqueId) {
        return blockedConnections.contains(connectionUniqueId);
    }

    @IdenticEvent
    public void onAuthenticationRequired(AuthenticationRequiredEvent event) {
        blockedConnections.add(event.getConnectionUniqueId());
    }

    @IdenticEvent
    public void onAuthenticationResolved(AuthenticationResolvedEvent event) {
        blockedConnections.remove(event.getConnectionUniqueId());
    }

    @IdenticEvent
    public void onRegistrationRequired(RegistrationRequiredEvent event) {
        blockedConnections.add(event.getConnectionUniqueId());
    }

    @IdenticEvent
    public void onRegistrationResolved(RegistrationResolvedEvent event) {
        blockedConnections.remove(event.getConnectionUniqueId());
    }

    @IdenticEvent
    public void onMigrationRequired(MigrationRequiredEvent event) {
        blockedConnections.add(event.getConnectionUniqueId());
    }

    @IdenticEvent
    public void onMigrationResolved(MigrationResolvedEvent event) {
        blockedConnections.remove(event.getConnectionUniqueId());
    }

    private boolean isAllowedCommand(String commandLine) {
        String trimmed = commandLine.trim();
        if (trimmed.startsWith("/"))
            trimmed = trimmed.substring(1);

        Set<String> allowedAliases = definitionCollector.getAllowedDuringAuthAliases();

        if (allowedAliases.contains(trimmed))
            return true;

        int spaceIndex = trimmed.indexOf(' ');
        String firstWord = spaceIndex > 0 ? trimmed.substring(0, spaceIndex) : trimmed;
        return allowedAliases.contains(firstWord);
    }
}