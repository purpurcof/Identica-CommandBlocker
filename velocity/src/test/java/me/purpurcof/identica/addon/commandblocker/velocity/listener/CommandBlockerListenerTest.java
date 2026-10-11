package me.purpurcof.identica.addon.commandblocker.velocity.listener;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.whereareiam.identica.identity.IdentityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Velocity Command Blocker Listener")
class CommandBlockerListenerTest {

    private final CommandFilterService filter = mock(CommandFilterService.class);
    private final IdentityService identityService = mock(IdentityService.class);
    private final CommandBlockerConfiguration config = mock(CommandBlockerConfiguration.class);
    private final UUID playerId = UUID.randomUUID();
    private final Player player = playerWithId();
    private final CommandBlockerListener listener = new CommandBlockerListener(filter, identityService, config);

    private Player playerWithId() {
        Player mocked = mock(Player.class);
        when(mocked.getUniqueId()).thenReturn(playerId);
        return mocked;
    }

    private CommandExecuteEvent event(CommandSource source, String command) {
        CommandExecuteEvent event = mock(CommandExecuteEvent.class);
        when(event.getCommandSource()).thenReturn(source);
        when(event.getCommand()).thenReturn(command);
        return event;
    }

    private static void assertDenied(CommandExecuteEvent event) {
        ArgumentCaptor<CommandExecuteEvent.CommandResult> result = ArgumentCaptor.forClass(CommandExecuteEvent.CommandResult.class);
        verify(event).setResult(result.capture());
        assertFalse(result.getValue().isAllowed());
    }

    @DisplayName("Ignores command sources that are not players")
    @Test
    void ignoresConsole() {
        CommandExecuteEvent event = event(mock(CommandSource.class), "tp");

        listener.onEvent(event);

        verify(event, never()).setResult(any());
    }

    @DisplayName("Leaves commands of players that are not blocked alone")
    @Test
    void ignoresUnblockedPlayers() {
        when(filter.isBlocked(playerId)).thenReturn(false);
        CommandExecuteEvent event = event(player, "tp");

        listener.onEvent(event);

        verify(event, never()).setResult(any());
    }

    @DisplayName("Leaves allowed commands alone")
    @Test
    void allowsAllowedCommands() {
        when(filter.isBlocked(playerId)).thenReturn(true);
        when(filter.isAllowed(playerId, "login x")).thenReturn(true);
        CommandExecuteEvent event = event(player, "login x");

        listener.onEvent(event);

        verify(event, never()).setResult(any());
    }

    @DisplayName("Denies commands that are not allowed")
    @Test
    void deniesBlockedCommands() {
        when(filter.isBlocked(playerId)).thenReturn(true);
        when(filter.isAllowed(playerId, "tp")).thenReturn(false);
        CommandExecuteEvent event = event(player, "tp");

        listener.onEvent(event);

        assertDenied(event);
    }

    @DisplayName("Still denies the command when no identity can be found for the player")
    @Test
    void deniesWithoutIdentity() {
        when(filter.isBlocked(playerId)).thenReturn(true);
        when(filter.isAllowed(playerId, "tp")).thenReturn(false);
        when(config.getBlockedMessage()).thenReturn("<red>nope</red>");
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.empty());
        CommandExecuteEvent event = event(player, "tp");

        listener.onEvent(event);

        assertDenied(event);
    }

    @DisplayName("Fails closed when the filter throws")
    @Test
    void failsClosed() {
        when(filter.isBlocked(playerId)).thenReturn(true);
        when(filter.isAllowed(any(UUID.class), anyString())).thenThrow(new IllegalStateException("boom"));
        CommandExecuteEvent event = event(player, "tp");

        listener.onEvent(event);

        assertDenied(event);
    }
}
