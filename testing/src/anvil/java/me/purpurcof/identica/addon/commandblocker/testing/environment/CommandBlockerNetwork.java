package me.purpurcof.identica.addon.commandblocker.testing.environment;

import me.whereareiam.anvil.integration.junit.AnvilEnvironment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Starts a fresh network around a test: a Velocity proxy with Identica, Credential and the packaged CommandBlocker addon, an
 * {@code auth} server for authentication and registration steps, and a {@code lobby} that completed players reach.
 *
 * <pre>{@code
 * @Test
 * @CommandBlockerNetwork(allowedCommands = "server")
 * void letsThroughACommand(ScenarioContext anvil) {
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@AnvilEnvironment(CommandBlockerScenarios.class)
public @interface CommandBlockerNetwork {
	/**
	 * Commands listed under {@code allowedCommands} in the addon's settings, which a player may use while they
	 * authenticate. Empty leaves the settings to the addon, as on a new installation.
	 */
	String[] allowedCommands() default {};
}