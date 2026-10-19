<div align="center">

# Identica-CommandBlocker

Addon-plugin for [Identica](https://github.com/whereareiam/Identica) that blocks commands during authentication.

</div>

# Information

![version](https://img.shields.io/github/v/release/purpurcof/Identica-CommandBlocker)
![build](https://img.shields.io/github/actions/workflow/status/purpurcof/Identica-CommandBlocker/build.yml?branch=dev)

[![GitHub Downloads](https://img.shields.io/github/downloads/purpurcof/Identica-CommandBlocker/total?&label=GitHub%20Downloads)](https://github.com/purpurcof/Identica-CommandBlocker/releases)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/Aw2QcPSW?&label=Modrinth%20Downloads)](https://modrinth.com/plugin/Aw2QcPSW)

# Requirements

* **Required Plugin:** [Identica 3.0.x](https://github.com/whereareiam/Identica)
* **Tested Platform:** Velocity

> [!WARNING]
> **BungeeCord Support:** BungeeCord is currently **untested** and has technical limitations regarding command tab-completion. Use at your own risk.

# Allowed commands

A player who is authenticating may always run the commands Identica needs for it, such as `/login`, `/pass`, `/identica enroll` and `/2fa confirm`, and those of any provider that adds its own. The addon reads them from Identica, so commands an administrator renames, and those of providers loaded later, are followed without a change here.

`allowed-commands` lists extra commands a player may run while authenticating. Entries are matched by whole words, case-insensitively, without the leading slash:

* `rules` allows `/rules` and `/rules page 2`.
* `2fa status` allows `/2fa status` and `/2fa status all`, but **not** `/2fa disable` or a bare `/2fa`.
* Listing `2fa` allows every `/2fa ...` command, so sub-command entries next to it are redundant.

The list is empty on a new installation. `/identica admin reload` also reloads it.

# Tab completion

* **Velocity:** the command tree sent to the player is filtered.
* **BungeeCord:** only completion that BungeeCord routes through its events can be filtered (arguments, proxy-side completion). Clients on 1.13+ complete command *names* from the tree their backend server sent, which BungeeCord does not expose to plugins.
