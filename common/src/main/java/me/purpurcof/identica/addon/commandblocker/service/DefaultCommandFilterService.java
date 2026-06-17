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
import me.whereareiam.identica.replication.cache.ReplicatedCache;
import me.whereareiam.keystone.Actor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class DefaultCommandFilterService implements CommandFilterService, EventListener {

    private static final long DEFAULT_TTL_MS = 300_000;

    private final CommandDefinitionCollector definitionCollector;
    private final ReplicatedCache<UUID> blockedCache;

    public DefaultCommandFilterService(CommandDefinitionCollector definitionCollector, ReplicatedCache<UUID> blockedCache) {
        this.definitionCollector = definitionCollector;
        this.blockedCache = blockedCache;
    }

    @Override
    public boolean isAllowed(@NotNull Actor actor, @NotNull String commandLine) {
        if (commandLine.isBlank())
            return false;

        if (!(actor instanceof Identity identity))
            return true;

        UUID connectionUniqueId = identity.getConnectionUniqueId();
        if (connectionUniqueId == null || !isBlocked(connectionUniqueId))
            return true;

        return isAllowedCommand(commandLine);
    }

    @Override
    public boolean isBlocked(@NotNull UUID connectionUniqueId) {
        return blockedCache.get(connectionUniqueId.toString()).join().isPresent();
    }

    @IdenticEvent
    public void onAuthenticationRequired(AuthenticationRequiredEvent event) {
        UUID connectionId = event.getConnectionUniqueId();
        blockedCache.put(connectionId.toString(), connectionId, DEFAULT_TTL_MS);
    }

    @IdenticEvent
    public void onAuthenticationResolved(AuthenticationResolvedEvent event) {
        UUID connectionId = event.getConnectionUniqueId();
        blockedCache.invalidate(connectionId.toString());
    }

    @IdenticEvent
    public void onRegistrationRequired(RegistrationRequiredEvent event) {
        UUID connectionId = event.getConnectionUniqueId();
        blockedCache.put(connectionId.toString(), connectionId, DEFAULT_TTL_MS);
    }

    @IdenticEvent
    public void onRegistrationResolved(RegistrationResolvedEvent event) {
        UUID connectionId = event.getConnectionUniqueId();
        blockedCache.invalidate(connectionId.toString());
    }

    @IdenticEvent
    public void onMigrationRequired(MigrationRequiredEvent event) {
        UUID connectionId = event.getConnectionUniqueId();
        blockedCache.put(connectionId.toString(), connectionId, DEFAULT_TTL_MS);
    }

    @IdenticEvent
    public void onMigrationResolved(MigrationResolvedEvent event) {
        UUID connectionId = event.getConnectionUniqueId();
        blockedCache.invalidate(connectionId.toString());
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