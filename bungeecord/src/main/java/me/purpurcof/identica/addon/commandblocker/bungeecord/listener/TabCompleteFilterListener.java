package me.purpurcof.identica.addon.commandblocker.bungeecord.listener;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.whereareiam.identica.command.CommandService;
import me.whereareiam.identica.identity.IdentityService;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.identica.identity.session.SessionService;
import me.whereareiam.identica.listener.DynamicListener;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TabCompleteFilterListener implements DynamicListener<TabCompleteEvent>, Listener {

    private final IdentityService identityService;
    private final SessionService sessionService;
    private final CommandDefinitionCollector definitionCollector;
    private final CommandService commandService;

    public TabCompleteFilterListener(
            IdentityService identityService,
            SessionService sessionService,
            CommandDefinitionCollector definitionCollector,
            CommandService commandService
    ) {
        this.identityService = identityService;
        this.sessionService = sessionService;
        this.definitionCollector = definitionCollector;
        this.commandService = commandService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @Override
    public void onEvent(@NotNull TabCompleteEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer player))
            return;

        Identity identity = identityService.findByConnectionUniqueId(player.getUniqueId()).orElse(null);
        if (identity != null && isAuthenticated(identity))
            return;

        Set<String> allowedAliases = definitionCollector.getAllowedDuringAuthAliases();
        Set<String> allAliases = new HashSet<>();
        commandService.getRegisteredDefinitions().values().forEach(def -> {
            if (def.getAliases() != null)
                allAliases.addAll(def.getAliases());
        });

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

                for (String alias : allAliases) {
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

    private boolean isAuthenticated(Identity identity) {
        UUID accountUniqueId = identity.getAccountUniqueId();
        if (accountUniqueId == null) return false;
        return sessionService.findByUniqueId(accountUniqueId).join().isPresent();
    }
}