package me.purpurcof.identica.addon.commandblocker.bungeecord;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.collector.DefaultCommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.purpurcof.identica.addon.commandblocker.service.DefaultCommandFilterService;
import me.purpurcof.identica.addon.commandblocker.bungeecord.listener.CommandBlockerListener;
import me.purpurcof.identica.addon.commandblocker.bungeecord.listener.TabCompleteFilterListener;
import me.whereareiam.identica.IdenticaAPI;
import me.whereareiam.identica.config.ConfigurationTypeResolver;
import me.whereareiam.identica.identity.IdentityService;
import me.whereareiam.identica.model.replication.ReplicationType;
import me.whereareiam.identica.replication.ReplicationSystem;
import me.whereareiam.identica.replication.cache.ReplicatedCache;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCordCommandBlockerPlugin extends Plugin {

    @Override
    public void onEnable() {
        if (!IdenticaAPI.isInitialized()) {
            getLogger().warning("IdenticaAPI not initialized, command blocker will not work");
            return;
        }

        ConfigurationTypeResolver typeResolver = IdenticaAPI.getService(ConfigurationTypeResolver.class);
        CommandBlockerConfiguration config = new CommandBlockerConfiguration(getDataFolder().toPath(), typeResolver);

        IdentityService identityService = IdenticaAPI.getPresenceService();
        CommandDefinitionCollector definitionCollector = new DefaultCommandDefinitionCollector(config);

        ReplicationSystem replicationSystem = IdenticaAPI.getReplicationSystem();
        ReplicatedCache<String> blockedCache = replicationSystem
                .cache("commandblocker:blocked")
                .defaultTtl(300_000)
                .replicated(ReplicationType.identity(String.class));
        DefaultCommandFilterService filterService = new DefaultCommandFilterService(definitionCollector, blockedCache);
        IdenticaAPI.getEventManager().register(filterService);

        CommandBlockerListener commandBlocker = new CommandBlockerListener(
                filterService, identityService, config.getPrefix(), config.getBlockedMessage());
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
