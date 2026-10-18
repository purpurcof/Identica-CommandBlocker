package me.purpurcof.identica.addon.commandblocker.testing.journey;

import me.purpurcof.identica.addon.commandblocker.config.CommandBlockerConfig;
import me.whereareiam.configura.Config;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.type.Format;
import me.whereareiam.identica.model.config.Messages;
import me.whereareiam.identica.provider.credential.config.CredentialMessages;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The configuration a running proxy uses, read from the files it generated into the models the plugins read them
 * into. A journey expectation names a message through these models' getters, so it follows the configured text and
 * a renamed message no longer compiles. Test code never copies message text.
 */
public final class IdenticaMessages {

	/** Read as the plugins read it. */
	private static final Configura CONFIGURA = Config.builder().format(Format.YAML).module(new DateTimePatternModule()).build();

	private final Path plugins;
	private final Map<String, Object> documents = new ConcurrentHashMap<>();

	/**
	 * @param plugins the proxy's {@code plugins} directory
	 */
	IdenticaMessages(Path plugins) {
		this.plugins = plugins;
	}

	/**
	 * Returns Identica's own messages.
	 */
	public Messages identica() {
		return read("identica/messages.yml", Messages.class);
	}

	/**
	 * Returns the Credential provider's messages, which its registration steps prompt with; the network must have
	 * the provider enabled.
	 */
	public CredentialMessages credential() {
		return read("identica/providers/Credential/messages.yml", CredentialMessages.class);
	}

	/**
	 * Returns the addon's own settings, which hold the message it answers a held back command with.
	 */
	public CommandBlockerConfig commandBlocker() {
		return read("identica-commandblocker/settings.yml", CommandBlockerConfig.class);
	}

	private <T> T read(String file, Class<T> type) {
		return type.cast(documents.computeIfAbsent(file, ignored -> {
			Path path = plugins.resolve(file);
			if (!Files.isRegularFile(path))
				throw new IllegalStateException("The proxy generated no " + file);

			return CONFIGURA.read(path, type);
		}));
	}
}