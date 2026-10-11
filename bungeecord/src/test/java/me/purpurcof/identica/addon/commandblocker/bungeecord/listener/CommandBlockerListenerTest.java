package me.purpurcof.identica.addon.commandblocker.bungeecord.listener;

import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.whereareiam.identica.identity.IdentityService;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("BungeeCord Command Blocker Listener")
class CommandBlockerListenerTest {

    private final CommandFilterService filter = mock(CommandFilterService.class);
    private final IdentityService identityService = mock(IdentityService.class);
    private final CommandBlockerConfiguration config = mock(CommandBlockerConfiguration.class);
    private final UUID playerId = UUID.randomUUID();
    private final ProxiedPlayer player = playerWithId();
    private final CommandBlockerListener listener = new CommandBlockerListener(filter, identityService, config);

    private ProxiedPlayer playerWithId() {
        ProxiedPlayer mocked = mock(ProxiedPlayer.class);
        when(mocked.getUniqueId()).thenReturn(playerId);
        return mocked;
    }

    private ChatEvent event(Connection sender, boolean command, String message) {
        ChatEvent event = mock(ChatEvent.class);
        when(event.isCommand()).thenReturn(command);
        when(event.getSender()).thenReturn(sender);
        when(event.getMessage()).thenReturn(message);
        return event;
    }

    @DisplayName("Ignores plain chat")
    @Test
    void ignoresPlainChat() {
        ChatEvent event = event(player, false, "hello");

        listener.onEvent(event);

        verify(event, never()).setCancelled(true);
    }

    @DisplayName("Ignores senders that are not players")
    @Test
    void ignoresNonPlayers() {
        ChatEvent event = event(mock(Connection.class), true, "/tp");

        listener.onEvent(event);

        verify(event, never()).setCancelled(true);
    }

    @DisplayName("Leaves allowed commands alone")
    @Test
    void allowsAllowedCommands() {
        when(filter.isBlocked(playerId)).thenReturn(true);
        when(filter.isAllowed(playerId, "/login x")).thenReturn(true);
        ChatEvent event = event(player, true, "/login x");

        listener.onEvent(event);

        verify(event, never()).setCancelled(true);
    }

    @DisplayName("Cancels commands that are not allowed")
    @Test
    void cancelsBlockedCommands() {
        when(filter.isBlocked(playerId)).thenReturn(true);
        when(filter.isAllowed(playerId, "/tp")).thenReturn(false);
        ChatEvent event = event(player, true, "/tp");

        listener.onEvent(event);

        verify(event).setCancelled(true);
    }

    @DisplayName("Still cancels the command when no identity can be found for the player")
    @Test
    void cancelsWithoutIdentity() {
        when(filter.isBlocked(playerId)).thenReturn(true);
        when(filter.isAllowed(playerId, "/tp")).thenReturn(false);
        when(config.getBlockedMessage()).thenReturn("<red>nope</red>");
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.empty());
        ChatEvent event = event(player, true, "/tp");

        listener.onEvent(event);

        verify(event).setCancelled(true);
    }

    @DisplayName("Fails closed when the filter throws")
    @Test
    void failsClosed() {
        when(filter.isBlocked(playerId)).thenReturn(true);
        when(filter.isAllowed(any(UUID.class), anyString())).thenThrow(new IllegalStateException("boom"));
        ChatEvent event = event(player, true, "/tp");

        listener.onEvent(event);

        verify(event).setCancelled(true);
    }
}
