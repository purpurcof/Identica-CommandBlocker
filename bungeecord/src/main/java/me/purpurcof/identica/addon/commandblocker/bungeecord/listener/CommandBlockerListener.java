package me.purpurcof.identica.addon.commandblocker.bungeecord.listener;

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

public class CommandBlockerListener implements Listener {

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(@NotNull ChatEvent event) {
        if (!event.isCommand())
            return;

        if (!(event.getSender() instanceof ProxiedPlayer player))
            return;

        String message = event.getMessage();
        if (message == null || message.isBlank())
            return;

        if (!commandFilterService.isBlocked(player.getUniqueId()))
            return;

        Identity identity = identityService.findByConnectionUniqueId(player.getUniqueId()).orElse(null);
        if (identity == null)
            return;

        if (commandFilterService.isAllowed(identity, message))
            return;

        event.setCancelled(true);

        if (blockedMessage != null && !blockedMessage.isBlank()) {
            String formatted = BlockedMessageFormatter.formatBlocked(prefix, blockedMessage);
            SerializerContent content = SerializerContent.builder()
                    .receiver(identity)
                    .message(formatted)
                    .build();
            identity.sendMessage(Serializer.serialize(content));
        }
    }
}