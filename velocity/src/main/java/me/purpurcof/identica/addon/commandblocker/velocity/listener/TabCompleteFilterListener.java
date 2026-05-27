package me.purpurcof.identica.addon.commandblocker.velocity.listener;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.whereareiam.identica.listener.DynamicListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TabCompleteFilterListener implements DynamicListener<PlayerAvailableCommandsEvent> {

    private static final Logger logger = LoggerFactory.getLogger(TabCompleteFilterListener.class);

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

        if (!commandFilterService.isBlocked(player.getUniqueId()))
            return;

        Set<String> allowedAliases = definitionCollector.getAllowedDuringAuthAliases();
        Set<String> allowedNames = new HashSet<>();
        for (String alias : allowedAliases) {
            int spaceIndex = alias.indexOf(' ');
            allowedNames.add(spaceIndex > 0 ? alias.substring(0, spaceIndex) : alias);
        }

        try {
            Field childrenField = CommandNode.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, CommandNode<?>> rootChildren = (Map<String, CommandNode<?>>) childrenField.get(event.getRootNode());
            rootChildren.entrySet().removeIf(entry -> !allowedNames.contains(entry.getKey()));
            for (Map.Entry<String, CommandNode<?>> entry : rootChildren.entrySet()) {
                filterChildNodes(entry.getValue(), childrenField, allowedNames, allowedAliases, entry.getKey());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.warn("Failed to filter tab complete commands", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void filterChildNodes(CommandNode<?> node, Field childrenField, Set<String> allowedNames, Set<String> allowedAliases, String prefix) throws IllegalAccessException {
        Map<String, CommandNode<?>> children = (Map<String, CommandNode<?>>) childrenField.get(node);
        if (children.isEmpty()) return;

        Set<String> allowedChildren = new HashSet<>(allowedNames);
        String prefixSpace = prefix + " ";
        for (String alias : allowedAliases) {
            if (alias.startsWith(prefixSpace) && alias.length() > prefixSpace.length()) {
                String rest = alias.substring(prefixSpace.length());
                int nextSpace = rest.indexOf(' ');
                allowedChildren.add(nextSpace > 0 ? rest.substring(0, nextSpace) : rest);
            }
        }

        children.entrySet().removeIf(entry -> {
            if (!(entry.getValue() instanceof LiteralCommandNode))
                return false;
            return !allowedChildren.contains(entry.getKey());
        });

        for (Map.Entry<String, CommandNode<?>> entry : children.entrySet()) {
            if (entry.getValue() instanceof LiteralCommandNode)
                filterChildNodes(entry.getValue(), childrenField, allowedNames, allowedAliases, prefix + " " + entry.getKey());
        }
    }
}