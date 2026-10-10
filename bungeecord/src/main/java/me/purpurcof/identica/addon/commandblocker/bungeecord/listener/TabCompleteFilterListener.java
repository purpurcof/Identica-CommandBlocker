package me.purpurcof.identica.addon.commandblocker.bungeecord.listener;

import lombok.RequiredArgsConstructor;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.purpurcof.identica.addon.commandblocker.util.AliasNormalizer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@RequiredArgsConstructor
public class TabCompleteFilterListener implements Listener {

    private final CommandFilterService commandFilterService;
    private final CommandDefinitionCollector definitionCollector;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(@NotNull TabCompleteEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer player)) return;

        if (!commandFilterService.isBlocked(player.getUniqueId())) return;

        Set<String> allowedNames = definitionCollector.getAllowedDuringAuthCommandNames();
        String cursor = event.getCursor() != null ? event.getCursor() : "";
        String normalized = AliasNormalizer.normalize(cursor);
        String firstWord = AliasNormalizer.firstWord(normalized);

        if (!normalized.isEmpty() && !allowedNames.contains(firstWord)) {
            event.getSuggestions().clear();
            event.setCancelled(true);
            return;
        }

        event.getSuggestions().removeIf(suggestion -> {
            if (suggestion == null || suggestion.isBlank()) return true;

            String suggestionNormalized = AliasNormalizer.normalize(suggestion);
            return !allowedNames.contains(AliasNormalizer.firstWord(suggestionNormalized));
        });

        if (event.getSuggestions().isEmpty())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onResponse(@NotNull TabCompleteResponseEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer player)) return;

        if (!commandFilterService.isBlocked(player.getUniqueId())) return;

        Set<String> allowedNames = definitionCollector.getAllowedDuringAuthCommandNames();

        event.getSuggestions().removeIf(suggestion -> {
            if (suggestion == null || suggestion.isBlank()) return true;

            String normalized = AliasNormalizer.normalize(suggestion);
            return !allowedNames.contains(AliasNormalizer.firstWord(normalized));
        });

        if (event.getSuggestions().isEmpty())
            event.setCancelled(true);
    }
}