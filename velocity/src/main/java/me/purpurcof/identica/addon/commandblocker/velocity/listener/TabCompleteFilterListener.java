package me.purpurcof.identica.addon.commandblocker.velocity.listener;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.whereareiam.identica.identity.IdentityService;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.identica.identity.session.SessionService;
import me.whereareiam.identica.listener.DynamicListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TabCompleteFilterListener implements DynamicListener<PlayerAvailableCommandsEvent> {

    private final IdentityService identityService;
    private final SessionService sessionService;
    private final CommandDefinitionCollector definitionCollector;

    public TabCompleteFilterListener(
            IdentityService identityService,
            SessionService sessionService,
            CommandDefinitionCollector definitionCollector
    ) {
        this.identityService = identityService;
        this.sessionService = sessionService;
        this.definitionCollector = definitionCollector;
    }

    @Override
    public void onEvent(@NotNull PlayerAvailableCommandsEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        Identity identity = identityService.findByConnectionUniqueId(player.getUniqueId()).orElse(null);
        if (identity != null && isAuthenticated(identity))
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
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
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

    private boolean isAuthenticated(Identity identity) {
        UUID accountUniqueId = identity.getAccountUniqueId();
        if (accountUniqueId == null) return false;
        return sessionService.findByUniqueId(accountUniqueId).join().isPresent();
    }
}