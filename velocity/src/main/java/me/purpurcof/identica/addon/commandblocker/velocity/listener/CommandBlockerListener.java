package me.purpurcof.identica.addon.commandblocker.velocity.listener;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import me.purpurcof.identica.addon.commandblocker.service.CommandFilterService;
import me.purpurcof.identica.addon.commandblocker.util.BlockedMessageFormatter;
import me.whereareiam.identica.Serializer;
import me.whereareiam.identica.identity.IdentityService;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.keystone.model.SerializerContent;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandBlockerListener {

    private static final Logger LOGGER = Logger.getLogger(CommandBlockerListener.class.getName());

    private final CommandFilterService commandFilterService;
    private final IdentityService identityService;
    private final String prefix;
    private final String blockedMessage;

    public CommandBlockerListener(
            CommandFilterService commandFilterService,
            IdentityService identityService,
            String prefix,
            String blockedMessage
    ) {
        this.commandFilterService = commandFilterService;
        this.identityService = identityService;
        this.prefix = prefix;
        this.blockedMessage = blockedMessage;
    }

    public void onEvent(@NotNull CommandExecuteEvent event) {
        try {
            if (!(event.getCommandSource() instanceof Player player))
                return;

            String commandLine = event.getCommand();
            if (commandLine == null || commandLine.isBlank())
                return;

            if (!commandFilterService.isBlocked(player.getUniqueId()))
                return;

            Identity identity = identityService.findByConnectionUniqueId(player.getUniqueId()).orElse(null);
            if (identity == null)
                return;

            if (commandFilterService.isAllowed(identity, commandLine))
                return;

            event.setResult(CommandExecuteEvent.CommandResult.denied());

            if (blockedMessage != null && !blockedMessage.isBlank()) {
                String formatted = BlockedMessageFormatter.formatBlocked(prefix, blockedMessage);
                SerializerContent content = SerializerContent.builder()
                        .receiver(identity)
                        .message(formatted)
                        .build();
                identity.sendMessage(Serializer.serialize(content));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error while filtering command", e);
        }
    }
}