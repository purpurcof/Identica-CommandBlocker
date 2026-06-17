package me.purpurcof.identica.addon.commandblocker.bungeecord.listener;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("BungeeCord Tab Complete Filter Listener")
class TabCompleteFilterListenerTest {
    private final CommandFilterService commandFilterService = mock(CommandFilterService.class);
    private final CommandDefinitionCollector definitionCollector = mock(CommandDefinitionCollector.class);
    private final TabCompleteFilterListener listener = new TabCompleteFilterListener(commandFilterService, definitionCollector);

    @DisplayName("Does not filter for unblocked players")
    @Test
    void doesNotFilterForUnblockedPlayers() {
        UUID playerId = UUID.randomUUID();
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(commandFilterService.isBlocked(playerId)).thenReturn(false);

        TabCompleteEvent event = mock(TabCompleteEvent.class);
        when(event.getSender()).thenReturn(player);
        List<String> suggestions = new ArrayList<>(List.of("/tp", "/gamemode"));
        when(event.getSuggestions()).thenReturn(suggestions);

        listener.onEvent(event);

        assertEquals(List.of("/tp", "/gamemode"), suggestions);
    }

    @DisplayName("Does nothing when sender is not a ProxiedPlayer")
    @Test
    void doesNothingWhenNotProxiedPlayer() {
        net.md_5.bungee.api.connection.Connection sender = mock(net.md_5.bungee.api.connection.Connection.class);

        TabCompleteEvent event = mock(TabCompleteEvent.class);
        doReturn(sender).when(event).getSender();

        listener.onEvent(event);
    }

    @DisplayName("Filters suggestions for blocked players when identity not found")
    @Test
    void filtersSuggestionsForBlockedPlayers() {
        UUID playerId = UUID.randomUUID();
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(commandFilterService.isBlocked(playerId)).thenReturn(true);

        when(definitionCollector.getAllowedDuringAuthCommandNames()).thenReturn(Set.of("login"));

        TabCompleteEvent event = mock(TabCompleteEvent.class);
        when(event.getSender()).thenReturn(player);
        List<String> suggestions = new ArrayList<>(List.of("/login", "/tp"));
        when(event.getSuggestions()).thenReturn(suggestions);

        listener.onEvent(event);

        assertEquals(List.of("/login"), suggestions);
    }

    @DisplayName("Filters suggestions by allowed commands")
    @Test
    void filtersSuggestionsByAllowedCommands() {
        UUID playerId = UUID.randomUUID();
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(commandFilterService.isBlocked(playerId)).thenReturn(true);

        when(definitionCollector.getAllowedDuringAuthCommandNames()).thenReturn(Set.of("login", "auth"));

        TabCompleteEvent event = mock(TabCompleteEvent.class);
        when(event.getSender()).thenReturn(player);
        List<String> suggestions = new ArrayList<>(List.of("/login", "/tp", "/gamemode", "/auth"));
        when(event.getSuggestions()).thenReturn(suggestions);

        listener.onEvent(event);

        assertEquals(List.of("/login", "/auth"), suggestions);
    }

    @DisplayName("Removes suggestions without leading slash")
    @Test
    void handlesSuggestionsWithoutLeadingSlash() {
        UUID playerId = UUID.randomUUID();
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(commandFilterService.isBlocked(playerId)).thenReturn(true);

        when(definitionCollector.getAllowedDuringAuthCommandNames()).thenReturn(Set.of("login"));

        TabCompleteEvent event = mock(TabCompleteEvent.class);
        when(event.getSender()).thenReturn(player);
        List<String> suggestions = new ArrayList<>(List.of("login", "tp"));
        when(event.getSuggestions()).thenReturn(suggestions);

        listener.onEvent(event);

        assertEquals(List.of("login"), suggestions);
    }

    @DisplayName("Filters multi-word aliases by first word")
    @Test
    void filtersMultiWordAliasesByFirstWord() {
        UUID playerId = UUID.randomUUID();
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(commandFilterService.isBlocked(playerId)).thenReturn(true);

        when(definitionCollector.getAllowedDuringAuthCommandNames()).thenReturn(Set.of("credential"));

        TabCompleteEvent event = mock(TabCompleteEvent.class);
        when(event.getSender()).thenReturn(player);
        List<String> suggestions = new ArrayList<>(List.of("/credential", "/tp"));
        when(event.getSuggestions()).thenReturn(suggestions);

        listener.onEvent(event);

        assertEquals(List.of("/credential"), suggestions);
    }

    @DisplayName("Removes null and blank suggestions")
    @Test
    void removesNullAndBlankSuggestions() {
        UUID playerId = UUID.randomUUID();
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(commandFilterService.isBlocked(playerId)).thenReturn(true);

        when(definitionCollector.getAllowedDuringAuthCommandNames()).thenReturn(Set.of("login"));

        TabCompleteEvent event = mock(TabCompleteEvent.class);
        when(event.getSender()).thenReturn(player);
        List<String> suggestions = new ArrayList<>();
        suggestions.add(null);
        suggestions.add(" ");
        suggestions.add("/login");
        suggestions.add("/tp");
        when(event.getSuggestions()).thenReturn(suggestions);

        listener.onEvent(event);

        assertEquals(List.of("/login"), suggestions);
    }
}