package me.purpurcof.identica.addon.commandblocker.service;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.util.AliasNormalizer;
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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultCommandFilterService implements CommandFilterService, EventListener {

    private static final long DEFAULT_TTL_MS = 300_000;
    private static final long CACHE_GET_TIMEOUT_MS = 250;
    private static final Logger LOGGER = Logger.getLogger(DefaultCommandFilterService.class.getName());

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
        try {
            Optional<UUID> result = blockedCache.get(connectionUniqueId.toString())
                    .completeOnTimeout(Optional.empty(), CACHE_GET_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> {
                        LOGGER.log(Level.FINE, "Cache read failed for " + connectionUniqueId, ex);
                        return Optional.empty();
                    })
                    .join();
            return result.isPresent();
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Unexpected error reading blocked state for " + connectionUniqueId, t);
            return false;
        }
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
        String normalized = AliasNormalizer.normalize(commandLine);
        if (normalized.isEmpty()) return false;

        Set<String> allowedAliases = definitionCollector.getAllowedDuringAuthAliases();

        if (allowedAliases.contains(normalized))
            return true;

        int spaceIndex = normalized.indexOf(' ');
        String firstWord = spaceIndex > 0 ? normalized.substring(0, spaceIndex) : normalized;
        return allowedAliases.contains(firstWord);
    }
}