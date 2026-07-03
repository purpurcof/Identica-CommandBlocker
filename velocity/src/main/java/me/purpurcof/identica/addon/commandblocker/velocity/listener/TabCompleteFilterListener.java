package me.purpurcof.identica.addon.commandblocker.velocity.listener;

import com.mojang.brigadier.tree.CommandNode;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.whereareiam.identica.listener.DynamicListener;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class TabCompleteFilterListener implements DynamicListener<PlayerAvailableCommandsEvent> {

    private final CommandFilterService commandFilterService;
    private final CommandDefinitionCollector definitionCollector;

    public TabCompleteFilterListener(
            CommandFilterService commandFilterService,
            CommandDefinitionCollector definitionCollector
    ) {
        this.commandFilterService = commandFilterService;
        this.definitionCollector = definitionCollector;
    }

    @Override
    public void onEvent(@NotNull PlayerAvailableCommandsEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        if (!commandFilterService.isBlocked(player.getUniqueId())) return;

        Set<String> allowed = definitionCollector.getAllowedDuringAuthCommandNames();
        if (allowed.isEmpty()) return;

        Set<String> toRemove = event.getRootNode().getChildren().stream()
                .map(CommandNode::getName)
                .filter(name -> !allowed.contains(name))
                .collect(Collectors.toSet());

        for (String name : toRemove) {
            event.getRootNode().removeChildByName(name);
        }
    }
}
