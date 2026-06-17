package me.purpurcof.identica.addon.commandblocker.velocity.listener;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Velocity Tab Complete Filter Listener")
class TabCompleteFilterListenerTest {
    private final CommandFilterService commandFilterService = mock(CommandFilterService.class);
    private final CommandDefinitionCollector definitionCollector = mock(CommandDefinitionCollector.class);
    private final TabCompleteFilterListener listener = new TabCompleteFilterListener(commandFilterService, definitionCollector);

    @DisplayName("Does not filter commands for unblocked players")
    @Test
    void doesNotFilterForUnblockedPlayers() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(commandFilterService.isBlocked(playerId)).thenReturn(false);

        RootCommandNode<?> rootNode = root();
        addChild(rootNode, "tp");

        PlayerAvailableCommandsEvent event = mock(PlayerAvailableCommandsEvent.class);
        when(event.getPlayer()).thenReturn(player);
        doReturn(rootNode).when(event).getRootNode();

        listener.onEvent(event);

        assertEquals(1, rootNode.getChildren().size());
    }

    @DisplayName("Filters commands for blocked players")
    @Test
    @SuppressWarnings("unchecked")
    void filtersCommandsForBlockedPlayers() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(commandFilterService.isBlocked(playerId)).thenReturn(true);

        when(definitionCollector.getAllowedDuringAuthCommandNames()).thenReturn(Set.of("login"));

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

    @DisplayName("Removes non-whitelisted commands for blocked players")
    @Test
    @SuppressWarnings("unchecked")
    void removesNonWhitelistedCommandsForBlockedPlayers() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(commandFilterService.isBlocked(playerId)).thenReturn(true);

        when(definitionCollector.getAllowedDuringAuthCommandNames()).thenReturn(Set.of("login", "auth"));

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

    private static <S> RootCommandNode<S> root() {
        return new RootCommandNode<>();
    }

    private static <S> void addChild(RootCommandNode<S> root, String name) {
        root.addChild(LiteralArgumentBuilder.<S>literal(name).build());
    }
}