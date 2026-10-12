package me.purpurcof.identica.addon.commandblocker;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.DefaultCommandFilterService;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.identica.pipeline.PendingScenarioService;
import me.whereareiam.keystone.Actor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Default Command Filter Service")
class DefaultCommandFilterServiceTest {

    private final PendingScenarioService pendingScenarios = mock(PendingScenarioService.class);
    private final CommandDefinitionCollector definitionCollector = mock(CommandDefinitionCollector.class);

    private final DefaultCommandFilterService service = new DefaultCommandFilterService(pendingScenarios, definitionCollector);

    private void pending(UUID connectionId) {
        when(pendingScenarios.isPending(connectionId)).thenReturn(true);
    }

    @DisplayName("Allows non-identity actors (console)")
    @Test
    void allowsNonIdentityActor() {
        Actor console = mock(Actor.class);

        assertTrue(service.isAllowed(console, "/somecommand"));
    }

    @DisplayName("isAllowed(UUID, String) allows command for blocked connection when in allow-list")
    @Test
    void uuidAllowsBlockedConnectionWithAllowedCommand() {
        UUID connectionId = UUID.randomUUID();
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login"));

        pending(connectionId);

        assertTrue(service.isAllowed(connectionId, "/login"));
    }

    @DisplayName("isAllowed(UUID, String) denies command for blocked connection when not in allow-list")
    @Test
    void uuidDeniesBlockedConnectionWithNotAllowedCommand() {
        UUID connectionId = UUID.randomUUID();
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of());

        pending(connectionId);

        assertFalse(service.isAllowed(connectionId, "/tp"));
    }

    @DisplayName("isAllowed(UUID, String) allows any command for unblocked connection")
    @Test
    void uuidAllowsUnblockedConnection() {
        UUID connectionId = UUID.randomUUID();

        assertTrue(service.isAllowed(connectionId, "/tp"));
    }

    @DisplayName("isAllowed(UUID, String) denies blank command for blocked connection")
    @Test
    void uuidDeniesBlankCommandForBlockedConnection() {
        UUID connectionId = UUID.randomUUID();

        pending(connectionId);

        assertFalse(service.isAllowed(connectionId, ""));
        assertFalse(service.isAllowed(connectionId, " "));
    }

    @DisplayName("Allows identity not in blocked set")
    @Test
    void allowsIdentityNotBlocked() {
        Identity identity = mock(Identity.class);
        when(identity.getConnectionUniqueId()).thenReturn(UUID.randomUUID());

        assertTrue(service.isAllowed(identity, "/somecommand"));
    }

    @DisplayName("Allows blocked identity when command is allowed")
    @Test
    void allowsBlockedIdentityWithAllowedCommand() {
        UUID connectionId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getConnectionUniqueId()).thenReturn(connectionId);
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login"));

        pending(connectionId);

        assertTrue(service.isAllowed(identity, "/login"));
    }

    @DisplayName("Denies blocked identity when command is not allowed")
    @Test
    void deniesBlockedIdentityWithNotAllowedCommand() {
        UUID connectionId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getConnectionUniqueId()).thenReturn(connectionId);
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of());

        pending(connectionId);

        assertFalse(service.isAllowed(identity, "/tp"));
    }

    @DisplayName("Allows identity after scenario is resolved")
    @Test
    void allowsIdentityAfterScenarioResolved() {
        UUID connectionId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getConnectionUniqueId()).thenReturn(connectionId);

        when(pendingScenarios.isPending(connectionId)).thenReturn(false);

        assertTrue(service.isAllowed(identity, "/tp"));
    }

    @DisplayName("Blocks a connection exactly while Identica reports a pending scenario for it")
    @Test
    void blocksWhilePending() {
        UUID connectionId = UUID.randomUUID();

        assertFalse(service.isBlocked(connectionId));

        pending(connectionId);
        assertTrue(service.isBlocked(connectionId));
    }

    @DisplayName("Returns false for blank command line")
    @Test
    void deniesBlankCommandLine() {
        UUID connectionId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getConnectionUniqueId()).thenReturn(connectionId);

        pending(connectionId);

        assertFalse(service.isAllowed(identity, ""));
        assertFalse(service.isAllowed(identity, " "));
    }

    @DisplayName("Identity with null connectionUniqueId is not blocked")
    @Test
    void allowsIdentityWithNullConnectionId() {
        Identity identity = mock(Identity.class);
        when(identity.getConnectionUniqueId()).thenReturn(null);

        assertTrue(service.isAllowed(identity, "/anycommand"));
    }

    @DisplayName("A multi-word entry allows exactly that sub-command, with or without further arguments")
    @Test
    void multiWordEntryAllowsOnlyThatSubCommand() {
        UUID connectionId = UUID.randomUUID();
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("2fa status"));
        pending(connectionId);

        assertTrue(service.isAllowed(connectionId, "/2fa status"));
        assertTrue(service.isAllowed(connectionId, "/2FA Status all"));
        assertFalse(service.isAllowed(connectionId, "/2fa disable"));
        assertFalse(service.isAllowed(connectionId, "/2fa"));
    }

    @DisplayName("Lookalike command lines are not allowed")
    @Test
    void lookalikesAreDenied() {
        UUID connectionId = UUID.randomUUID();
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login"));
        pending(connectionId);

        assertFalse(service.isAllowed(connectionId, "/loginx"));
        assertFalse(service.isAllowed(connectionId, "//login"));
        assertFalse(service.isAllowed(connectionId, "/ login"));
        assertFalse(service.isAllowed(connectionId, "/bungeecord:login"));
    }

    @SuppressWarnings("DataFlowIssue")
    @DisplayName("A null connection id is never blocked (and does not throw)")
    @Test
    void nullConnectionIdIsNotBlocked() {
        assertFalse(service.isBlocked(null));
    }
}