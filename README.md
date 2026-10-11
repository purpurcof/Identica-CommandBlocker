<div align="center">

# Identica-CommandBlocker

Addon-plugin for [Identica](https://github.com/whereareiam/Identica) that blocks commands during authentication.

</div>

---

### Information

![build](https://img.shields.io/github/actions/workflow/status/purpurcof/Identica-CommandBlocker/build.yml?branch=dev)
![version](https://img.shields.io/github/v/release/purpurcof/Identica-CommandBlocker)

[![GitHub Downloads](https://img.shields.io/github/downloads/purpurcof/Identica-CommandBlocker/total?&label=GitHub%20Downloads)](https://github.com/purpurcof/Identica-CommandBlocker/releases)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/Aw2QcPSW?&label=Modrinth%20Downloads)](https://modrinth.com/plugin/Aw2QcPSW)

### Requirements

* **Dependencies:**
  * [Identica 3.0.0](https://github.com/whereareiam/Identica)
* **Platforms:**
  * Velocity
  * BungeeCord (untested, may work)

### Installation

1. Download the latest version of Identica-CommandBlocker from [Releases](https://github.com/purpurcof/Identica-CommandBlocker/releases).
2. Place the `.jar` file into the `plugins` folder.
3. Restart the server to generate the configuration and necessary files.

### Allowed commands

`allowed-commands` lists what a player may run while authenticating. Entries are matched by whole words, case-insensitively, without the leading slash:

* `login` allows `/login` and `/login hunter2`.
* `2fa status` allows `/2fa status` and `/2fa status all`, but **not** `/2fa disable` or a bare `/2fa`.
* Listing `2fa` allows every `/2fa ...` command, so sub-command entries next to it are redundant.

The default list contains the bare `identica`, which allows every `/identica ...` sub-command (they remain protected by their own permissions). To be stricter, replace it with the specific sub-commands players need, for example `identica enroll`.

### Tab completion

* **Velocity:** the command tree sent to the player is filtered.
* **BungeeCord:** only completion that BungeeCord routes through its events can be filtered (arguments, proxy-side completion). Clients on 1.13+ complete command *names* from the tree their backend server sent, which BungeeCord does not expose to plugins.

