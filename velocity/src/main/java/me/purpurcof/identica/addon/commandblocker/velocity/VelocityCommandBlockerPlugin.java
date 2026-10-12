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
import me.purpurcof.identica.addon.commandblocker.collector.DefaultCommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.service.DefaultCommandFilterService;
import me.purpurcof.identica.addon.commandblocker.velocity.listener.CommandBlockerListener;
import me.purpurcof.identica.addon.commandblocker.velocity.listener.TabCompleteFilterListener;
import me.whereareiam.identica.IdenticaAPI;
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
        IdenticaAPI.whenReady(this::enable);
    }

    private void enable(IdenticaAPI identica) {
        CommandBlockerConfiguration config;
        try {
            config = new CommandBlockerConfiguration(dataDirectory, identica.getConfigura());
        } catch (LinkageError | RuntimeException e) {
            logger.error("Could not set up the settings through Identica's Configura; the command blocker will NOT work. This addon must be able to see Identica's Configura classes.", e);
            return;
        }

        DefaultCommandDefinitionCollector definitionCollector = new DefaultCommandDefinitionCollector(config, identica.getCommandService());
        identica.getEventManager().register(definitionCollector);

        DefaultCommandFilterService filterService = new DefaultCommandFilterService(identica.getPendingScenarioService(), definitionCollector);

        CommandBlockerListener commandBlocker = new CommandBlockerListener(
                filterService, identica.getIdentityService(), config);
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