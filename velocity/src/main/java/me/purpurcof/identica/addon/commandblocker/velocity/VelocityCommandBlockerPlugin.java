package me.purpurcof.identica.addon.commandblocker.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.collector.DefaultCommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.service.DefaultCommandFilterService;
import me.purpurcof.identica.addon.commandblocker.velocity.listener.CommandBlockerListener;
import me.purpurcof.identica.addon.commandblocker.velocity.listener.TabCompleteFilterListener;
import me.whereareiam.identica.IdenticaAPI;
import me.whereareiam.identica.config.ConfigurationTypeResolver;
import me.whereareiam.identica.identity.IdentityService;
import me.whereareiam.identica.model.replication.ReplicationType;
import me.whereareiam.identica.replication.ReplicationSystem;
import me.whereareiam.identica.replication.cache.ReplicatedCache;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.UUID;

@Plugin(
        id = "identica-commandblocker",
        name = "Identica-CommandBlocker",
        version = "1.3.0",
        description = "Blocks commands during authentication",
        authors = {"purpurcof"},
        dependencies = {
                @Dependency(id = "identica")
        }
)
public class VelocityCommandBlockerPlugin {

    private final Logger logger;
    private final EventManager eventManager;
    private final Path dataDirectory;

    @Inject
    public VelocityCommandBlockerPlugin(
            Logger logger,
            @DataDirectory Path dataDirectory,
            EventManager eventManager
    ) {
        this.logger = logger;
        this.eventManager = eventManager;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        if (!IdenticaAPI.isInitialized()) {
            logger.warn("IdenticaAPI not initialized, command blocker will not work");
            return;
        }

        ConfigurationTypeResolver typeResolver = IdenticaAPI.getService(ConfigurationTypeResolver.class);
        CommandBlockerConfiguration config = new CommandBlockerConfiguration(dataDirectory, typeResolver);
        IdentityService identityService = IdenticaAPI.getPresenceService();
        CommandDefinitionCollector definitionCollector = new DefaultCommandDefinitionCollector(config);

        ReplicationSystem replicationSystem = IdenticaAPI.getReplicationSystem();
        ReplicatedCache<UUID> blockedCache = replicationSystem
                .cache("commandblocker:blocked")
                .defaultTtl(300_000)
                .replicated(ReplicationType.identity(UUID.class));
        DefaultCommandFilterService filterService = new DefaultCommandFilterService(definitionCollector, blockedCache);
        IdenticaAPI.getEventManager().register(filterService);

        CommandBlockerListener commandBlocker = new CommandBlockerListener(
                filterService, identityService, config.getPrefix(), config.getBlockedMessage());
        eventManager.register(this, CommandExecuteEvent.class, (short) 1, commandBlocker::onEvent);

        TabCompleteFilterListener tabFilter = new TabCompleteFilterListener(
                filterService, definitionCollector);
        eventManager.register(this, PlayerAvailableCommandsEvent.class, (short) 1, tabFilter::onEvent);

        logger.info("Identica-CommandBlocker initialized");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Identica-CommandBlocker shutting down");
    }
}