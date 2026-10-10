package me.purpurcof.identica.addon.commandblocker;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.DefaultCommandFilterService;
import me.whereareiam.identica.event.scenario.authentication.AuthenticationRequiredEvent;
import me.whereareiam.identica.event.scenario.authentication.AuthenticationResolvedEvent;
import me.whereareiam.identica.event.scenario.registration.RegistrationRequiredEvent;
import me.whereareiam.identica.event.scenario.registration.RegistrationResolvedEvent;
import me.whereareiam.identica.event.scenario.migration.MigrationRequiredEvent;
import me.whereareiam.identica.event.scenario.migration.MigrationResolvedEvent;
import me.whereareiam.identica.identity.actor.Identity;
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

    private final CommandDefinitionCollector definitionCollector = mock(CommandDefinitionCollector.class);

    private final DefaultCommandFilterService service = new DefaultCommandFilterService(definitionCollector);

    private static AuthenticationRequiredEvent authRequired(UUID connectionId) {
        AuthenticationRequiredEvent event = mock(AuthenticationRequiredEvent.class);
        when(event.getConnectionUniqueId()).thenReturn(connectionId);
        return event;
    }

    private static AuthenticationResolvedEvent authResolved(UUID connectionId) {
        AuthenticationResolvedEvent event = mock(AuthenticationResolvedEvent.class);
        when(event.getConnectionUniqueId()).thenReturn(connectionId);
        return event;
    }

    private static RegistrationRequiredEvent regRequired(UUID connectionId) {
        RegistrationRequiredEvent event = mock(RegistrationRequiredEvent.class);
        when(event.getConnectionUniqueId()).thenReturn(connectionId);
        return event;
    }

    private static RegistrationResolvedEvent regResolved(UUID connectionId) {
        RegistrationResolvedEvent event = mock(RegistrationResolvedEvent.class);
        when(event.getConnectionUniqueId()).thenReturn(connectionId);
        return event;
    }

    private static MigrationRequiredEvent migRequired(UUID connectionId) {
        MigrationRequiredEvent event = mock(MigrationRequiredEvent.class);
        when(event.getConnectionUniqueId()).thenReturn(connectionId);
        return event;
    }

    private static MigrationResolvedEvent migResolved(UUID connectionId) {
        MigrationResolvedEvent event = mock(MigrationResolvedEvent.class);
        when(event.getConnectionUniqueId()).thenReturn(connectionId);
        return event;
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

        service.onAuthenticationRequired(authRequired(connectionId));

        assertTrue(service.isAllowed(connectionId, "/login"));
    }

    @DisplayName("isAllowed(UUID, String) denies command for blocked connection when not in allow-list")
    @Test
    void uuidDeniesBlockedConnectionWithNotAllowedCommand() {
        UUID connectionId = UUID.randomUUID();
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of());

        service.onAuthenticationRequired(authRequired(connectionId));

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

        service.onAuthenticationRequired(authRequired(connectionId));

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

        service.onAuthenticationRequired(authRequired(connectionId));

        assertTrue(service.isAllowed(identity, "/login"));
    }

    @DisplayName("Denies blocked identity when command is not allowed")
    @Test
    void deniesBlockedIdentityWithNotAllowedCommand() {
        UUID connectionId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getConnectionUniqueId()).thenReturn(connectionId);
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of());

        service.onAuthenticationRequired(authRequired(connectionId));

        assertFalse(service.isAllowed(identity, "/tp"));
    }

    @DisplayName("Allows identity after scenario is resolved")
    @Test
    void allowsIdentityAfterScenarioResolved() {
        UUID connectionId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getConnectionUniqueId()).thenReturn(connectionId);

        service.onAuthenticationRequired(authRequired(connectionId));
        service.onAuthenticationResolved(authResolved(connectionId));

        assertTrue(service.isAllowed(identity, "/tp"));
    }

    @DisplayName("Tracks blocked state via isBlocked method")
    @Test
    void tracksBlockedState() {
        UUID connectionId = UUID.randomUUID();

        assertFalse(service.isBlocked(connectionId));

        service.onAuthenticationRequired(authRequired(connectionId));
        assertTrue(service.isBlocked(connectionId));

        service.onAuthenticationResolved(authResolved(connectionId));
        assertFalse(service.isBlocked(connectionId));
    }

    @DisplayName("Returns false for blank command line")
    @Test
    void deniesBlankCommandLine() {
        UUID connectionId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getConnectionUniqueId()).thenReturn(connectionId);

        service.onAuthenticationRequired(authRequired(connectionId));

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

    @DisplayName("Adds to local set on AuthenticationRequired")
    @Test
    void addsLocalOnAuthenticationRequired() {
        UUID connectionId = UUID.randomUUID();

        service.onAuthenticationRequired(authRequired(connectionId));

        assertTrue(service.isBlocked(connectionId));
    }

    @DisplayName("Adds to local set on RegistrationRequired")
    @Test
    void addsLocalOnRegistrationRequired() {
        UUID connectionId = UUID.randomUUID();

        service.onRegistrationRequired(regRequired(connectionId));

        assertTrue(service.isBlocked(connectionId));
    }

    @DisplayName("Removes from local set on RegistrationResolved")
    @Test
    void removesLocalOnRegistrationResolved() {
        UUID connectionId = UUID.randomUUID();
        service.onRegistrationRequired(regRequired(connectionId));

        service.onRegistrationResolved(regResolved(connectionId));

        assertFalse(service.isBlocked(connectionId));
    }

    @DisplayName("Adds to local set on MigrationRequired")
    @Test
    void addsLocalOnMigrationRequired() {
        UUID connectionId = UUID.randomUUID();

        service.onMigrationRequired(migRequired(connectionId));

        assertTrue(service.isBlocked(connectionId));
    }

    @DisplayName("Removes from local set on MigrationResolved")
    @Test
    void removesLocalOnMigrationResolved() {
        UUID connectionId = UUID.randomUUID();
        service.onMigrationRequired(migRequired(connectionId));

        service.onMigrationResolved(migResolved(connectionId));

        assertFalse(service.isBlocked(connectionId));
    }
}