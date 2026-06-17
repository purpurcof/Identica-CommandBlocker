package me.purpurcof.identica.addon.commandblocker.velocity.listener;

import com.mojang.brigadier.tree.CommandNode;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.whereareiam.identica.listener.DynamicListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;

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
        if (player == null) return;

        if (!commandFilterService.isBlocked(player.getUniqueId())) return;

        if (CHILDREN_FIELD == null) {
            logger.warn("Unable to filter commands: reflection field not available");
            return;
        }

        Map<String, CommandNode<?>> rootChildren = getChildrenMap(event.getRootNode());
        if (rootChildren == null) return;

        rootChildren.keySet().removeIf(key -> !definitionCollector.getAllowedDuringAuthCommandNames().contains(key));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, CommandNode<?>> getChildrenMap(CommandNode<?> node) {
        if (CHILDREN_FIELD == null) return null;

        try {
            return (Map<String, CommandNode<?>>) CHILDREN_FIELD.get(node);
        } catch (IllegalAccessException e) {
            logger.debug("Failed to access children field via reflection", e);
            return null;
        }
    }

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
