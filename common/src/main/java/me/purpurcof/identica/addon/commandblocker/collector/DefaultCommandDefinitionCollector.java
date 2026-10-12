package me.purpurcof.identica.addon.commandblocker.collector;

import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.util.AliasNormalizer;
import me.whereareiam.identica.command.CommandService;
import me.whereareiam.identica.event.EventListener;
import me.whereareiam.identica.event.base.IdenticEvent;
import me.whereareiam.identica.event.lifecycle.IdenticaReloadedEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The commands a player may run while authenticating: the ones Identica marks as needed before login, as players
 * type them under the aliases the administrator configured, plus the extra entries of {@code allowed-commands}.
 *
 * <p>Identica's commands are read on every call, so renamed aliases and the commands of providers loaded later are
 * followed without a restart.</p>
 */
public class DefaultCommandDefinitionCollector implements CommandDefinitionCollector, EventListener {

    private volatile Set<String> configuredAliases = Collections.emptySet();

    private final CommandBlockerConfiguration config;
    private final CommandService commandService;

    public DefaultCommandDefinitionCollector(CommandBlockerConfiguration config, CommandService commandService) {
        this.config = config;
        this.commandService = commandService;
        // the configuration has just been loaded by its own constructor; no need to read it again
        rebuildCaches();
    }

    @Override
    public Set<String> getAllowedDuringAuthAliases() {
        Set<String> aliases = new HashSet<>(configuredAliases);
        for (String command : commandService.getCommandsAvailableWhilePending()) {
            String normalized = AliasNormalizer.normalize(command);
            if (!normalized.isEmpty()) aliases.add(normalized);
        }

        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public Set<String> getAllowedDuringAuthCommandNames() {
        return getAllowedDuringAuthAliases().stream()
                .map(AliasNormalizer::firstWord)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * {@code /identica admin reload} also reloads this addon's settings.
     */
    @IdenticEvent
    public void onReloaded(IdenticaReloadedEvent event) {
        config.reload();
        rebuildCaches();
    }

    private void rebuildCaches() {
        Collection<String> raw = config.getAllowedCommands();
        this.configuredAliases = raw == null ? Collections.emptySet() : raw.stream()
                .filter(Objects::nonNull)
                .map(AliasNormalizer::normalize)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
