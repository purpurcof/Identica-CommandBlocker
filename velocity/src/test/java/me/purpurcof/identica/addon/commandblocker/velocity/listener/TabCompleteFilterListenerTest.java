package me.purpurcof.identica.addon.commandblocker.velocity.listener;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.whereareiam.identica.identity.IdentityService;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.identica.identity.session.SessionService;
import me.whereareiam.identica.model.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Velocity Tab Complete Filter Listener")
class TabCompleteFilterListenerTest {
    private final IdentityService identityService = mock(IdentityService.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final CommandDefinitionCollector definitionCollector = mock(CommandDefinitionCollector.class);
    private final TabCompleteFilterListener listener = new TabCompleteFilterListener(identityService, sessionService, definitionCollector);

    @DisplayName("Does not filter commands for authenticated players")
    @Test
    void doesNotFilterForAuthenticatedPlayers() {
        UUID playerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(accountId);
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.of(identity));
        when(sessionService.findByUniqueId(accountId))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(mock(Session.class))));

        RootCommandNode<?> rootNode = root();
        addChild(rootNode, "tp");

        PlayerAvailableCommandsEvent event = mock(PlayerAvailableCommandsEvent.class);
        when(event.getPlayer()).thenReturn(player);
        doReturn(rootNode).when(event).getRootNode();

        listener.onEvent(event);

        assertEquals(1, rootNode.getChildren().size());
    }

    @DisplayName("Filters commands when identity is not found")
    @Test
    @SuppressWarnings("unchecked")
    void filtersCommandsWhenIdentityNotFound() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.empty());

        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login"));

        RootCommandNode<?> rootNode = root();
        addChild(rootNode, "login");
        addChild(rootNode, "tp");

        PlayerAvailableCommandsEvent event = mock(PlayerAvailableCommandsEvent.class);
        when(event.getPlayer()).thenReturn(player);
        doReturn(rootNode).when(event).getRootNode();

        listener.onEvent(event);

        Collection<CommandNode<?>> children = (Collection<CommandNode<?>>) (Collection<?>) rootNode.getChildren();
        Set<String> names = children.stream().map(CommandNode::getName).collect(Collectors.toSet());
        assertEquals(Set.of("login"), names);
    }

    @DisplayName("Removes non-whitelisted commands for unauthenticated players with null account")
    @Test
    @SuppressWarnings("unchecked")
    void removesNonWhitelistedCommandsForNullAccount() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(null);
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.of(identity));

        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("login", "auth"));

        RootCommandNode<?> rootNode = root();
        addChild(rootNode, "login");
        addChild(rootNode, "tp");
        addChild(rootNode, "gamemode");
        addChild(rootNode, "auth");

        PlayerAvailableCommandsEvent event = mock(PlayerAvailableCommandsEvent.class);
        when(event.getPlayer()).thenReturn(player);
        doReturn(rootNode).when(event).getRootNode();

        listener.onEvent(event);

        Collection<CommandNode<?>> children = (Collection<CommandNode<?>>) (Collection<?>) rootNode.getChildren();
        Set<String> names = children.stream().map(CommandNode::getName).collect(Collectors.toSet());
        assertEquals(Set.of("login", "auth"), names);
    }

    @DisplayName("Removes non-whitelisted commands for unauthenticated players without session")
    @Test
    @SuppressWarnings("unchecked")
    void removesNonWhitelistedCommandsWhenNoSession() {
        UUID playerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        Identity identity = mock(Identity.class);
        when(identity.getAccountUniqueId()).thenReturn(accountId);
        when(identityService.findByConnectionUniqueId(playerId)).thenReturn(Optional.of(identity));
        when(sessionService.findByUniqueId(accountId))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        when(definitionCollector.getAllowedDuringAuthAliases()).thenReturn(Set.of("2fa"));

        RootCommandNode<?> rootNode = root();
        addChild(rootNode, "2fa");
        addChild(rootNode, "ban");

        PlayerAvailableCommandsEvent event = mock(PlayerAvailableCommandsEvent.class);
        when(event.getPlayer()).thenReturn(player);
        doReturn(rootNode).when(event).getRootNode();

        listener.onEvent(event);

        Collection<CommandNode<?>> children = (Collection<CommandNode<?>>) (Collection<?>) rootNode.getChildren();
        assertTrue(children.stream().allMatch(n -> n.getName().equals("2fa")));
    }

    private static <S> RootCommandNode<S> root() {
        return new RootCommandNode<>();
    }

    private static <S> void addChild(RootCommandNode<S> root, String name) {
        root.addChild(LiteralArgumentBuilder.<S>literal(name).build());
    }
}