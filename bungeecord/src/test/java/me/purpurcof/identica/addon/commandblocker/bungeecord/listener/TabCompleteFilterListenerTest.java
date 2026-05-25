package me.purpurcof.identica.addon.commandblocker.bungeecord.listener;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.whereareiam.identica.command.CommandService;
import me.whereareiam.identica.identity.IdentityService;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.identica.identity.session.SessionService;
import me.whereareiam.identica.model.Session;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("BungeeCord Tab Complete Filter Listener")
class TabCompleteFilterListenerTest {
    private final IdentityService identityService = mock(IdentityService.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final CommandDefinitionCollector definitionCollector = mock(CommandDefinitionCollector.class);
    private final CommandService commandService = mock(CommandService.class);
    private final TabCompleteFilterListener listener;

    TabCompleteFilterListenerTest() {
        when(commandService.getRegisteredDefinitions()).thenReturn(Collections.emptyMap());
        listener = new TabCompleteFilterListener(
                identityService, sessionService, definitionCollector, commandService);
    }

    @DisplayName("Does not filter for authenticated players")
    @Test
    void doesNotFilterForAuthenticatedPlayers() {
        UUID playerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);

        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(accountId);
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.of(identity));
        when(sessionService.findByUniqueId(accountId))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(mock(Session.class))));

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

    @DisplayName("Filters suggestions when identity is not found")
    @Test
    void filtersSuggestionsWhenIdentityNotFound() {
        UUID playerId = UUID.randomUUID();
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);

        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.empty());
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login"));

        TabCompleteEvent event = mock(TabCompleteEvent.class);
        when(event.getSender()).thenReturn(player);
        List<String> suggestions = new ArrayList<>(List.of("/login", "/tp"));
        when(event.getSuggestions()).thenReturn(suggestions);

        listener.onEvent(event);

        assertEquals(List.of("/login"), suggestions);
    }

    @DisplayName("Filters suggestions for unauthenticated players")
    @Test
    void filtersSuggestionsForUnauthenticatedPlayers() {
        UUID playerId = UUID.randomUUID();
        ProxiedPlayer player = mock(ProxiedPlayer.class);
        when(player.getUniqueId()).thenReturn(playerId);

        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(null);
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.of(identity));

        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login", "auth"));

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

        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(null);
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.of(identity));

        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login"));

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

        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(null);
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.of(identity));

        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("credential confirm", "credential cancel"));

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

        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(null);
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.of(identity));

        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login"));

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