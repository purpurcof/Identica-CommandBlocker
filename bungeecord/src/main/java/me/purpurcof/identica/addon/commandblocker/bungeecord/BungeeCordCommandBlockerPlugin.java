package me.purpurcof.identica.addon.commandblocker.bungeecord;

import me.purpurcof.identica.addon.commandblocker.collector.DefaultCommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.service.DefaultCommandFilterService;
import me.purpurcof.identica.addon.commandblocker.bungeecord.listener.CommandBlockerListener;
import me.purpurcof.identica.addon.commandblocker.bungeecord.listener.TabCompleteFilterListener;
import me.whereareiam.identica.IdenticaAPI;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Level;

public class BungeeCordCommandBlockerPlugin extends Plugin {

    @Override
    public void onEnable() {
        IdenticaAPI.whenReady(this::enable);
    }

    private void enable(IdenticaAPI identica) {
        CommandBlockerConfiguration config;
        try {
            config = new CommandBlockerConfiguration(getDataFolder().toPath(), identica.getConfigura());
        } catch (LinkageError | RuntimeException e) {
            getLogger().log(Level.SEVERE, "Could not set up the settings through Identica's Configura; the command blocker will NOT work. This addon must be able to see Identica's Configura classes.", e);
            return;
        }

        DefaultCommandDefinitionCollector definitionCollector = new DefaultCommandDefinitionCollector(config, identica.getCommandService());
        identica.getEventManager().register(definitionCollector);

        DefaultCommandFilterService filterService = new DefaultCommandFilterService(identica.getPendingScenarioService(), definitionCollector);

        CommandBlockerListener commandBlocker = new CommandBlockerListener(
                filterService, identica.getIdentityService(), config);
        TabCompleteFilterListener tabFilter = new TabCompleteFilterListener(
                filterService, definitionCollector);

        getProxy().getPluginManager().registerListener(this, commandBlocker);
        getProxy().getPluginManager().registerListener(this, tabFilter);
        getLogger().info("Identica-CommandBlocker enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Identica-CommandBlocker shutting down");
    }
}