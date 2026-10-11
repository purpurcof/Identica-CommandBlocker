package me.purpurcof.identica.addon.commandblocker.util;

import java.util.Locale;
import java.util.Set;

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

    /**
     * Checks a normalized command line against normalized allow-list entries.
     *
     * <p>An entry allows the command line when it equals it or equals any leading run of whole words of it:
     * {@code login} allows {@code login hunter2}, {@code 2fa status} allows {@code 2fa status} and
     * {@code 2fa status all}, but not {@code 2fa disable}. Anything that does not match is not allowed, which
     * includes lines with unusual leading whitespace.
     */
    public static boolean matchesAllowed(Set<String> allowed, String normalizedLine) {
        if (allowed == null || allowed.isEmpty() || normalizedLine == null || normalizedLine.isEmpty()) return false;

        if (allowed.contains(normalizedLine)) return true;

        for (int space = normalizedLine.indexOf(' '); space > 0; space = normalizedLine.indexOf(' ', space + 1)) {
            if (allowed.contains(normalizedLine.substring(0, space))) return true;
        }

        return false;
    }
}