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

import java.util.Locale;
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
        if (commandLine == null) return false;
        String normalized = commandLine.trim();
        if (normalized.startsWith("/"))
            normalized = normalized.substring(1);
        normalized = normalized.toLowerCase(Locale.ROOT);

        Set<String> allowedAliases = definitionCollector.getAllowedDuringAuthAliases();

        if (allowedAliases.contains(normalized))
            return true;

        int spaceIndex = normalized.indexOf(' ');
        String firstWord = spaceIndex > 0 ? normalized.substring(0, spaceIndex) : normalized;
        return allowedAliases.contains(firstWord);
    }
}