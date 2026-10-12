package me.purpurcof.identica.addon.commandblocker.velocity;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import me.purpurcof.identica.addon.commandblocker.collector.DefaultCommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.service.DefaultCommandFilterService;
import me.purpurcof.identica.addon.commandblocker.velocity.listener.CommandBlockerListener;
import me.purpurcof.identica.addon.commandblocker.velocity.listener.TabCompleteFilterListener;
import me.whereareiam.identica.IdenticaAPI;
import me.whereareiam.identica.Registry;
import me.whereareiam.identica.Reloadable;
import me.whereareiam.identica.identity.IdentityService;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "identica-commandblocker",
        name = "Identica-CommandBlocker",
        version = BuildConstants.VERSION,
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

        CommandBlockerConfiguration config;
        try {
            config = new CommandBlockerConfiguration(dataDirectory, IdenticaAPI.getConfigura());
        } catch (LinkageError | RuntimeException e) {
            logger.error("Could not set up the settings through Identica's Configura; the command blocker will NOT work. This addon needs an Identica build that provides IdenticaAPI.getConfigura() (dev 63f5499 or newer) and must be able to see Identica's Configura classes.", e);
            return;
        }
        IdentityService identityService = IdenticaAPI.getPresenceService();
        DefaultCommandDefinitionCollector definitionCollector = new DefaultCommandDefinitionCollector(config);

        DefaultCommandFilterService filterService = new DefaultCommandFilterService(definitionCollector);
        IdenticaAPI.getEventManager().register(filterService);

        IdenticaAPI.getService(Key.get(new TypeLiteral<Registry<Reloadable>>() {})).register(definitionCollector);

        CommandBlockerListener commandBlocker = new CommandBlockerListener(
                filterService, identityService, config);
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