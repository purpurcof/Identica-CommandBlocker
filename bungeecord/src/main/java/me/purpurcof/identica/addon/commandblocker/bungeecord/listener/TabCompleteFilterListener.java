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

/**
 * Filters tab completion for players who are still authenticating.
 *
 * <p>Note that clients on 1.13+ complete command <em>names</em> from the command tree the backend server
 * sent them, which BungeeCord does not expose to plugins; this listener covers what BungeeCord does route
 * through its events (command arguments, proxy-side completion).
 */
@RequiredArgsConstructor
public class TabCompleteFilterListener implements Listener {

    private final CommandFilterService commandFilterService;
    private final CommandDefinitionCollector definitionCollector;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(@NotNull TabCompleteEvent event) {
        // for this event the sender is the player and the receiver is the server
        if (!(event.getSender() instanceof ProxiedPlayer player)) return;

        if (!commandFilterService.isBlocked(player.getUniqueId())) return;

        Set<String> allowedNames = definitionCollector.getAllowedDuringAuthCommandNames();
        String cursor = event.getCursor();

        if (cursor != null && !cursor.isBlank() && !cursor.startsWith("/")) {
            // plain chat completion would reveal online player names
            event.getSuggestions().clear();
        } else if (cursor != null && isCompletingArguments(cursor)) {
            // the suggestions are arguments, not command names: keep them only for an allowed command
            event.getSuggestions().removeIf(suggestion -> suggestion == null || suggestion.isBlank());
            if (!allowedNames.contains(AliasNormalizer.firstWord(AliasNormalizer.normalize(cursor)))) {
                event.getSuggestions().clear();
            }
        } else {
            // completing a command name (or the cursor is unknown): keep only allowed commands. The text typed
            // so far is a prefix of the name, so it must not be compared with the allow-list itself.
            event.getSuggestions().removeIf(suggestion -> {
                if (suggestion == null || suggestion.isBlank()) return true;

                String normalized = AliasNormalizer.normalize(suggestion);
                return !allowedNames.contains(AliasNormalizer.firstWord(normalized));
            });
        }

        if (event.getSuggestions().isEmpty())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onResponse(@NotNull TabCompleteResponseEvent event) {
        // for this event the sender is the backend server and the player is the receiver
        if (!(event.getReceiver() instanceof ProxiedPlayer player)) return;

        if (!commandFilterService.isBlocked(player.getUniqueId())) return;

        // requests of blocked players are answered (or dropped) by onEvent and never reach the backend, so
        // anything arriving here would expose backend commands or player names
        event.getSuggestions().clear();
        event.setCancelled(true);
    }

    private static boolean isCompletingArguments(String cursor) {
        // "/2fa" is still completing the command name, "/2fa " and "/2fa st" are completing arguments
        return cursor.indexOf(' ') > 0;
    }
}