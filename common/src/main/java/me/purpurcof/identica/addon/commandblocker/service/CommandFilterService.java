package me.purpurcof.identica.addon.commandblocker.service;

import me.whereareiam.keystone.Actor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface CommandFilterService {

    boolean isAllowed(@NotNull Actor actor, @NotNull String commandLine);

    boolean isBlocked(@NotNull UUID connectionUniqueId);
}