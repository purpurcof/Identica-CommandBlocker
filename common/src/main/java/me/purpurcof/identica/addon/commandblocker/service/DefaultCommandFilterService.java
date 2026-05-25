package me.purpurcof.identica.addon.commandblocker.service;

import me.purpurcof.identica.addon.commandblocker.collector.CommandDefinitionCollector;
import me.whereareiam.identica.identity.actor.Identity;
import me.whereareiam.identica.identity.session.SessionService;
import me.whereareiam.keystone.Actor;
import org.jetbrains.annotations.NotNull;
import java.util.Set;
import java.util.UUID;

public class DefaultCommandFilterService implements CommandFilterService {

    private final SessionService sessionService;
    private final CommandDefinitionCollector definitionCollector;

    public DefaultCommandFilterService(SessionService sessionService, CommandDefinitionCollector definitionCollector) {
        this.sessionService = sessionService;
        this.definitionCollector = definitionCollector;
    }

    @Override
    public boolean isAllowed(@NotNull Actor actor, @NotNull String commandLine) {
        if (commandLine.isBlank())
            return false;

        if (!(actor instanceof Identity identity))
            return true;

        UUID accountUniqueId = identity.getAccountUniqueId();
        if (accountUniqueId == null)
            return isAllowedCommand(commandLine);

        return sessionService.findByUniqueId(accountUniqueId)
                .join()
                .map(session -> true)
                .orElseGet(() -> isAllowedCommand(commandLine));
    }

    private boolean isAllowedCommand(String commandLine) {
        String trimmed = commandLine.trim();
        if (trimmed.startsWith("/"))
            trimmed = trimmed.substring(1);

        Set<String> allowedAliases = definitionCollector.getAllowedDuringAuthAliases();

        if (allowedAliases.contains(trimmed))
            return true;

        int spaceIndex = trimmed.indexOf(' ');
        String firstWord = spaceIndex > 0 ? trimmed.substring(0, spaceIndex) : trimmed;
        return allowedAliases.contains(firstWord);
    }
}