package me.purpurcof.identica.addon.commandblocker.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
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
import me.whereareiam.identica.identity.session.SessionService;
import org.slf4j.Logger;

import java.nio.file.Path;

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
        SessionService sessionService = IdenticaAPI.getSessionService();
        CommandDefinitionCollector definitionCollector = new DefaultCommandDefinitionCollector(config);

        DefaultCommandFilterService filterService = new DefaultCommandFilterService(sessionService, definitionCollector);

        CommandBlockerListener commandBlocker = new CommandBlockerListener(
                filterService, identityService, config.getPrefix(), config.getBlockedMessage());
        eventManager.register(this, CommandExecuteEvent.class, PostOrder.LATE, commandBlocker::onEvent);

        TabCompleteFilterListener tabFilter = new TabCompleteFilterListener(
                identityService, sessionService, definitionCollector);
        eventManager.register(this, PlayerAvailableCommandsEvent.class, PostOrder.LATE, tabFilter::onEvent);

        logger.info("Identica-CommandBlocker initialized");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Identica-CommandBlocker shutting down");
    }
}