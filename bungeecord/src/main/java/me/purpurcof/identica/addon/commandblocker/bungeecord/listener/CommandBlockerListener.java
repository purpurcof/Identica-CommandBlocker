package me.purpurcof.identica.addon.commandblocker.bungeecord.listener;

import lombok.RequiredArgsConstructor;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.purpurcof.identica.addon.commandblocker.util.BlockedMessageFormatter;
import me.whereareiam.identica.Serializer;
import me.whereareiam.identica.identity.IdentityService;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.keystone.model.SerializerContent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CommandBlockerListener implements Listener {

    private static final Logger LOGGER = Logger.getLogger(CommandBlockerListener.class.getName());

    private final CommandFilterService commandFilterService;
    private final IdentityService identityService;
    private final CommandBlockerConfiguration config;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(@NotNull ChatEvent event) {
        try {
            if (!event.isCommand()) return;

            if (!(event.getSender() instanceof ProxiedPlayer player)) return;

            String message = event.getMessage();
            if (message == null || message.isBlank()) return;

            UUID playerUUID = player.getUniqueId();
            if (!commandFilterService.isBlocked(playerUUID)) return;

            if (commandFilterService.isAllowed(playerUUID, message)) return;

            event.setCancelled(true);

            String blockedMessage = config.getBlockedMessage();
            if (blockedMessage != null && !blockedMessage.isBlank()) {
                Identity identity = identityService.findByConnectionUniqueId(playerUUID).orElse(null);
                if (identity != null) {
                    String formatted = BlockedMessageFormatter.formatBlocked(config.getPrefix(), blockedMessage);
                    SerializerContent content = SerializerContent.builder()
                            .receiver(identity)
                            .message(formatted)
                            .build();
                    identity.sendMessage(Serializer.serialize(content));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while filtering command", e);
            event.setCancelled(true);
        }
    }
}