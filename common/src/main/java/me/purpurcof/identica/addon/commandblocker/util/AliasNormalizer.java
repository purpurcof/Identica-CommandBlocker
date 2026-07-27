package me.purpurcof.identica.addon.commandblocker.util;

import java.util.Locale;

public final class AliasNormalizer {

    private AliasNormalizer() {}

    public static String normalize(String alias) {
        if (alias == null) return "";
        String s = alias.trim();
        if (s.startsWith("/")) s = s.substring(1);
        return s.toLowerCase(Locale.ROOT);
    }

    public static String firstWord(String s) {
        int spaceIndex = s.indexOf(' ');
        return spaceIndex > 0 ? s.substring(0, spaceIndex) : s;
    }
}