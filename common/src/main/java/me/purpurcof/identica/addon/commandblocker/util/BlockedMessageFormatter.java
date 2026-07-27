package me.purpurcof.identica.addon.commandblocker.util;

public final class BlockedMessageFormatter {

    private BlockedMessageFormatter() {}

    public static String safePrefix(String prefix) {
        return prefix != null ? prefix : "";
    }

    public static String safeBlockedMessage(String blockedMessage) {
        return blockedMessage != null ? blockedMessage : "";
    }

    public static String formatBlocked(String prefix, String blockedMessage) {
        String p = safePrefix(prefix);
        String m = safeBlockedMessage(blockedMessage);
        if (p.isEmpty()) return m;
        return p + m;
    }
}