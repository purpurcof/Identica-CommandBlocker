package me.purpurcof.identica.addon.commandblocker;

import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfiguration;
import me.whereareiam.configura.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the configuration through a plain Configura instance, the same type of object
 * {@code IdenticaAPI.getConfigura()} hands to the plugins at runtime.
 */
@DisplayName("Command Blocker Configuration")
class CommandBlockerConfigurationTest {

    @TempDir
    Path dataDirectory;

    private CommandBlockerConfiguration create() {
        return new CommandBlockerConfiguration(dataDirectory, Config.configured());
    }

    private Path settingsFile() throws IOException {
        try (Stream<Path> files = Files.list(dataDirectory)) {
            return files.filter(path -> path.getFileName().toString().startsWith("settings")).findFirst().orElseThrow();
        }
    }

    @DisplayName("Creates the settings file and fills in the defaults")
    @Test
    void createsFileWithDefaults() throws IOException {
        CommandBlockerConfiguration config = create();

        assertTrue(Files.exists(settingsFile()));
        assertTrue(config.getAllowedCommands().contains("login"));
        assertNotNull(config.getPrefix());
        assertNotNull(config.getBlockedMessage());
    }

    @DisplayName("A second instance reads back what the first one wrote")
    @Test
    void roundTrips() {
        List<String> first = List.copyOf(create().getAllowedCommands());

        assertEquals(first, List.copyOf(create().getAllowedCommands()));
    }

    @DisplayName("Entries removed by the server owner are not brought back by the defaults")
    @Test
    void removedEntriesStayRemoved() throws IOException {
        CommandBlockerConfiguration config = create();
        Path file = settingsFile();
        Files.writeString(file, Files.readString(file).replaceAll("(?m)^\\s*-\\s*\"?login\"?\\s*\\R", ""));

        config.reload();

        assertFalse(config.getAllowedCommands().contains("login"));
        assertTrue(config.getAllowedCommands().contains("pass"));
    }
}
