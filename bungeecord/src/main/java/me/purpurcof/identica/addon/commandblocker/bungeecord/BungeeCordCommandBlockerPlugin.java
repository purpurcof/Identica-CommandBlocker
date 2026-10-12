package me.purpurcof.identica.addon.commandblocker.bungeecord;

import me.purpurcof.identica.addon.commandblocker.collector.DefaultCommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.service.DefaultCommandFilterService;
import me.purpurcof.identica.addon.commandblocker.bungeecord.listener.CommandBlockerListener;
import me.purpurcof.identica.addon.commandblocker.bungeecord.listener.TabCompleteFilterListener;
import me.whereareiam.identica.IdenticaAPI;
import me.whereareiam.identica.Registry;
import me.whereareiam.identica.Reloadable;
import me.whereareiam.identica.identity.IdentityService;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Level;

public class BungeeCordCommandBlockerPlugin extends Plugin {

    @Override
    public void onEnable() {
        if (!IdenticaAPI.isInitialized()) {
            getLogger().warning("IdenticaAPI not initialized, command blocker will not work");
            return;
        }

        CommandBlockerConfiguration config;
        try {
            config = new CommandBlockerConfiguration(getDataFolder().toPath(), IdenticaAPI.getConfigura());
        } catch (LinkageError | RuntimeException e) {
            getLogger().log(Level.SEVERE, "Could not set up the settings through Identica's Configura; the command blocker will NOT work. This addon needs an Identica build that provides IdenticaAPI.getConfigura() (dev 63f5499 or newer) and must be able to see Identica's Configura classes.", e);
            return;
        }

        IdentityService identityService = IdenticaAPI.getPresenceService();
        DefaultCommandDefinitionCollector definitionCollector = new DefaultCommandDefinitionCollector(config);

        DefaultCommandFilterService filterService = new DefaultCommandFilterService(definitionCollector);
        IdenticaAPI.getEventManager().register(filterService);

        IdenticaAPI.getService(Key.get(new TypeLiteral<Registry<Reloadable>>() {})).register(definitionCollector);

        CommandBlockerListener commandBlocker = new CommandBlockerListener(
                filterService, identityService, config);
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