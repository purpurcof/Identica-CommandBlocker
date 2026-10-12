package me.purpurcof.identica.addon.commandblocker;

import me.purpurcof.identica.addon.commandblocker.collector.DefaultCommandDefinitionCollector;
import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.whereareiam.identica.command.CommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Default Command Definition Collector")
class DefaultCommandDefinitionCollectorTest {

    private final CommandBlockerConfiguration config = mock(CommandBlockerConfiguration.class);
    private final CommandService commandService = mock(CommandService.class);

    @DisplayName("Allows Identica's login commands as players type them, next to the configured extra entries")
    @Test
    void combinesIdenticaCommandsWithConfiguredEntries() {
        when(config.getAllowedCommands()).thenReturn(List.of("/Rules"));
        when(commandService.getCommandsAvailableWhilePending()).thenReturn(Set.of("login", "l", "identica enroll", "id enroll"));

        DefaultCommandDefinitionCollector collector = new DefaultCommandDefinitionCollector(config, commandService);

        assertEquals(Set.of("rules", "login", "l", "identica enroll", "id enroll"), collector.getAllowedDuringAuthAliases());
        assertEquals(Set.of("rules", "login", "l", "identica", "id"), collector.getAllowedDuringAuthCommandNames());
    }

    @DisplayName("Follows Identica's commands as they change, such as a renamed alias or a provider loaded later")
    @Test
    void readsIdenticaCommandsOnEveryCall() {
        when(config.getAllowedCommands()).thenReturn(List.of());
        when(commandService.getCommandsAvailableWhilePending()).thenReturn(Set.of("login"));
        DefaultCommandDefinitionCollector collector = new DefaultCommandDefinitionCollector(config, commandService);

        when(commandService.getCommandsAvailableWhilePending()).thenReturn(Set.of("signin", "passphrase"));

        assertEquals(Set.of("signin", "passphrase"), collector.getAllowedDuringAuthAliases());
    }

    @DisplayName("Reloads the settings when Identica reloads")
    @Test
    void reloadsSettingsWithIdentica() {
        when(config.getAllowedCommands()).thenReturn(List.of("rules"));
        when(commandService.getCommandsAvailableWhilePending()).thenReturn(Set.of());
        DefaultCommandDefinitionCollector collector = new DefaultCommandDefinitionCollector(config, commandService);

        when(config.getAllowedCommands()).thenReturn(List.of("spawn"));
        collector.onReloaded(null);

        verify(config).reload();
        assertEquals(Set.of("spawn"), collector.getAllowedDuringAuthAliases());
    }
}
