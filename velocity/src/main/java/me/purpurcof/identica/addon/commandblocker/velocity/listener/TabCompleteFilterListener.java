package me.purpurcof.identica.addon.commandblocker.velocity.listener;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.whereareiam.identica.listener.DynamicListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TabCompleteFilterListener implements DynamicListener<PlayerAvailableCommandsEvent> {

    private static final Logger logger = LoggerFactory.getLogger(TabCompleteFilterListener.class);
    private static final Field CHILDREN_FIELD = getChildrenField();

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
        if (player == null) {
            return;
        }

        if (!commandFilterService.isBlocked(player.getUniqueId())) {
            return;
        }

        if (CHILDREN_FIELD == null) {
            logger.warn("Unable to filter commands: reflection field not available");
            return;
        }

        try {
            Set<String> allowedAliases = definitionCollector.getAllowedDuringAuthAliases();
            Set<String> allowedNames = extractCommandNames(allowedAliases);
            
            Map<String, CommandNode<?>> rootChildren = getChildrenMap(event.getRootNode());
            if (rootChildren == null) {
                logger.warn("Unable to get root children: reflection failed");
                return;
            }
            
            rootChildren.entrySet().removeIf(entry -> !allowedNames.contains(entry.getKey()));
            
            for (Map.Entry<String, CommandNode<?>> entry : rootChildren.entrySet()) {
                filterChildNodes(entry.getValue(), allowedNames, allowedAliases, entry.getKey());
            }
            
            logger.debug("Filtered commands for blocked player: {} allowed", allowedNames.size());
        } catch (Exception e) {
            logger.warn("Failed to filter tab complete commands", e);
        }
    }

    /**
     * Extract command names from aliases (first word only)
     */
    private Set<String> extractCommandNames(@NotNull Set<String> allowedAliases) {
        Set<String> allowedNames = new HashSet<>();
        for (String alias : allowedAliases) {
            int spaceIndex = alias.indexOf(' ');
            String commandName = spaceIndex > 0 ? alias.substring(0, spaceIndex) : alias;
            allowedNames.add(commandName);
        }
        return allowedNames;
    }

    /**
     * Safely get children map from a CommandNode using reflection
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private static Map<String, CommandNode<?>> getChildrenMap(@NotNull CommandNode<?> node) {
        if (CHILDREN_FIELD == null) return null;

        try {
            Object childrenObj = CHILDREN_FIELD.get(node);
            return (Map<String, CommandNode<?>>) childrenObj;
        } catch (IllegalAccessException e) {
            logger.debug("Failed to access children field via reflection", e);
            return null;
        }
    }

    /**
     * Filter child nodes recursively based on allowed aliases
     */
    private void filterChildNodes(
            @NotNull CommandNode<?> node,
            @NotNull Set<String> allowedNames,
            @NotNull Set<String> allowedAliases,
            @NotNull String prefix
    ) {
        Map<String, CommandNode<?>> children = getChildrenMap(node);
        if (children == null || children.isEmpty()) {
            return;
        }

        Set<String> allowedChildren = collectAllowedChildren(allowedNames, allowedAliases, prefix);

        children.entrySet().removeIf(entry -> {
            if (!(entry.getValue() instanceof LiteralCommandNode)) {
                return false;
            }
            return !allowedChildren.contains(entry.getKey());
        });

        for (Map.Entry<String, CommandNode<?>> entry : children.entrySet()) {
            if (entry.getValue() instanceof LiteralCommandNode) {
                filterChildNodes(entry.getValue(), allowedNames, allowedAliases, prefix + " " + entry.getKey());
            }
        }
    }

    private static Set<String> collectAllowedChildren(
            @NotNull Set<String> allowedNames,
            @NotNull Set<String> allowedAliases,
            @NotNull String prefix
    ) {
        Set<String> allowedChildren = new HashSet<>(allowedNames);
        String prefixSpace = prefix + " ";
        for (String alias : allowedAliases) {
            if (alias.startsWith(prefixSpace) && alias.length() > prefixSpace.length()) {
                String rest = alias.substring(prefixSpace.length());
                int nextSpace = rest.indexOf(' ');
                String childName = nextSpace > 0 ? rest.substring(0, nextSpace) : rest;
                allowedChildren.add(childName);
            }
        }
        return allowedChildren;
    }

    /**
     * Get the children field from CommandNode class (cached for performance)
     */
    @Nullable
    private static Field getChildrenField() {
        try {
            Field field = CommandNode.class.getDeclaredField("children");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            logger.warn("Could not find 'children' field in CommandNode class", e);
            return null;
        }
    }
}
