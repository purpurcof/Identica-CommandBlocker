package me.purpurcof.identica.addon.commandblocker.bungeecord.listener;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.whereareiam.identica.listener.DynamicListener;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class TabCompleteFilterListener implements DynamicListener<TabCompleteEvent>, Listener {

    private final CommandFilterService commandFilterService;
    private final CommandDefinitionCollector definitionCollector;

    public TabCompleteFilterListener(
            CommandFilterService commandFilterService,
            CommandDefinitionCollector definitionCollector
    ) {
        this.commandFilterService = commandFilterService;
        this.definitionCollector = definitionCollector;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @Override
    public void onEvent(@NotNull TabCompleteEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer player))
            return;

        if (!commandFilterService.isBlocked(player.getUniqueId()))
            return;

        Set<String> allowedAliases = definitionCollector.getAllowedDuringAuthAliases();

        event.getSuggestions().removeIf(suggestion -> {
            if (suggestion == null || suggestion.isBlank())
                return true;

            String trimmed = suggestion.startsWith("/") ? suggestion.substring(1) : suggestion;
            int spaceIndex = trimmed.indexOf(' ');
            String firstWord = spaceIndex > 0 ? trimmed.substring(0, spaceIndex) : trimmed;

            if (allowedAliases.contains(firstWord)) {
                if (spaceIndex < 0)
                    return false;

                String remaining = trimmed.substring(firstWord.length()).trim();
                if (remaining.isEmpty())
                    return false;

                if (allowedAliases.contains(remaining) || allowedAliases.contains(trimmed))
                    return false;

                for (String alias : allowedAliases) {
                    if (alias.equals(remaining) || alias.startsWith(remaining + " ") || remaining.startsWith(alias + " "))
                        return true;
                }

                return false;
            }

            for (String alias : allowedAliases) {
                if (alias.startsWith(firstWord + " "))
                    return false;
            }

            return true;
        });
    }
}