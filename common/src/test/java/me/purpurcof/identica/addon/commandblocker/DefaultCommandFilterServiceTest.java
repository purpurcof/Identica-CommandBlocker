package me.purpurcof.identica.addon.commandblocker;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.DefaultCommandFilterService;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.identica.identity.session.SessionService;
import me.whereareiam.identica.model.Session;
import me.whereareiam.keystone.Actor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Default Command Filter Service")
class DefaultCommandFilterServiceTest {
    private final SessionService sessionService = mock(SessionService.class);
    private final CommandDefinitionCollector definitionCollector = mock(CommandDefinitionCollector.class);
    private final DefaultCommandFilterService service = new DefaultCommandFilterService(sessionService, definitionCollector);

    @DisplayName("Allows non-identity actors (console)")
    @Test
    void allowsNonIdentityActor() {
        Actor console = mock(Actor.class);

        assertTrue(service.isAllowed(console, "/somecommand"));
    }

    @DisplayName("Allows identity with active session")
    @Test
    void allowsIdentityWithActiveSession() {
        UUID accountId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(accountId);
        when(sessionService.findByUniqueId(accountId))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(mock(Session.class))));

        assertTrue(service.isAllowed(identity, "/somecommand"));
    }

    @DisplayName("Allows identity with null accountUniqueId when command is allowed")
    @Test
    void allowsIdentityWithNullAccountAndAllowedCommand() {
        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(null);
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login"));

        assertTrue(service.isAllowed(identity, "/login"));
    }

    @DisplayName("Denies identity with null accountUniqueId when command is not allowed")
    @Test
    void deniesIdentityWithNullAccountAndNotAllowedCommand() {
        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(null);
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of());

        assertFalse(service.isAllowed(identity, "/tp"));
    }

    @DisplayName("Denies identity without session when command is not allowed")
    @Test
    void deniesIdentityWithoutSessionAndNotAllowedCommand() {
        UUID accountId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(accountId);
        when(sessionService.findByUniqueId(accountId))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of());

        assertFalse(service.isAllowed(identity, "/tp"));
    }

    @DisplayName("Returns false for blank command line")
    @Test
    void deniesBlankCommandLine() {
        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(null);

        assertFalse(service.isAllowed(identity, ""));
        assertFalse(service.isAllowed(identity, " "));
    }

    @DisplayName("Checks session only when accountUniqueId is not null")
    @Test
    void doesNotCheckSessionWhenAccountUniqueIdIsNull() {
        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(null);
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login"));

        assertTrue(service.isAllowed(identity, "/login"));
    }

    @DisplayName("Denies when session missing and command is not allowed")
    @Test
    void deniesWhenSessionMissingAndCommandNotAllowed() {
        UUID accountId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(accountId);
        when(sessionService.findByUniqueId(accountId))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of());

        assertFalse(service.isAllowed(identity, "/anycommand"));
    }

    @DisplayName("Allows when session exists even if command is not allowed")
    @Test
    void allowsWhenSessionExistsEvenIfCommandNotAllowed() {
        UUID accountId = UUID.randomUUID();
        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(accountId);
        when(sessionService.findByUniqueId(accountId))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(mock(Session.class))));

        assertTrue(service.isAllowed(identity, "/anycommand"));
    }
}