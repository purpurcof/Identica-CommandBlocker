package me.purpurcof.identica.addon.commandblocker.collector;

import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.whereareiam.identica.Reloadable;

import me.purpurcof.identica.addon.commandblocker.util.AliasNormalizer;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultCommandDefinitionCollector implements CommandDefinitionCollector, Reloadable {

    private volatile Set<String> cachedAliases = Collections.emptySet();
    private volatile Set<String> cachedCommandNames = Collections.emptySet();

    private final CommandBlockerConfiguration config;

    public DefaultCommandDefinitionCollector(CommandBlockerConfiguration config) {
        this.config = config;
        // the configuration has just been loaded by its own constructor; no need to read it again
        rebuildCaches();
    }

    @Override
    public Set<String> getAllowedDuringAuthAliases() {
        return cachedAliases;
    }

    @Override
    public Set<String> getAllowedDuringAuthCommandNames() {
        return cachedCommandNames;
    }

    @Override
    public void reload() {
        config.reload();
        rebuildCaches();
    }

    private void rebuildCaches() {
        Collection<String> raw = config.getAllowedCommands();
        this.cachedAliases = raw == null ? Collections.emptySet() : raw.stream()
                .filter(Objects::nonNull)
                .map(AliasNormalizer::normalize)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        this.cachedCommandNames = extractCommandNames(cachedAliases);
    }

    private static Set<String> extractCommandNames(Set<String> aliases) {
        return aliases.stream()
                .map(AliasNormalizer::firstWord)
                .collect(Collectors.toUnmodifiableSet());
    }
}