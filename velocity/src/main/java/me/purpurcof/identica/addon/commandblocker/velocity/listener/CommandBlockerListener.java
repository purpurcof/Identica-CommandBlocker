package me.purpurcof.identica.addon.commandblocker.velocity.listener;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.purpurcof.identica.addon.commandblocker.util.BlockedMessageFormatter;
import me.whereareiam.identica.Serializer;
import me.whereareiam.identica.identity.IdentityService;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.keystone.model.SerializerContent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CommandBlockerListener {

    private static final Logger LOGGER = Logger.getLogger(CommandBlockerListener.class.getName());

    private final CommandFilterService commandFilterService;
    private final IdentityService identityService;
    private final CommandBlockerConfiguration config;

    public void onEvent(@NotNull CommandExecuteEvent event) {
        try {
            if (!(event.getCommandSource() instanceof Player player)) return;

            String commandLine = event.getCommand();
            if (commandLine == null || commandLine.isBlank()) return;

            UUID playerUUID = player.getUniqueId();
            if (!commandFilterService.isBlocked(playerUUID)) return;

            if (commandFilterService.isAllowed(playerUUID, commandLine)) return;

            event.setResult(CommandExecuteEvent.CommandResult.denied());

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
            event.setResult(CommandExecuteEvent.CommandResult.denied());
        }
    }
}