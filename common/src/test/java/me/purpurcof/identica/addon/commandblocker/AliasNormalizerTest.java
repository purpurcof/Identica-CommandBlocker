package me.purpurcof.identica.addon.commandblocker;

import me.purpurcof.identica.addon.commandblocker.util.AliasNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Alias Normalizer")
class AliasNormalizerTest {

    @DisplayName("Normalizes slash, case and surrounding whitespace")
    @Test
    void normalizes() {
        assertEquals("login x", AliasNormalizer.normalize("  /LOGIN x "));
        assertEquals("", AliasNormalizer.normalize(null));
    }

    @DisplayName("Only one leading slash is removed")
    @Test
    void stripsOneSlash() {
        assertEquals("/login", AliasNormalizer.normalize("//login"));
    }

    @DisplayName("Allows exact matches and leading whole-word runs")
    @Test
    void matchesAllowed() {
        Set<String> allowed = Set.of("login", "2fa status");

        assertTrue(AliasNormalizer.matchesAllowed(allowed, "login"));
        assertTrue(AliasNormalizer.matchesAllowed(allowed, "login hunter2"));
        assertTrue(AliasNormalizer.matchesAllowed(allowed, "2fa status"));
        assertTrue(AliasNormalizer.matchesAllowed(allowed, "2fa status all"));
    }

    @DisplayName("Does not allow partial words or other sub-commands")
    @Test
    void doesNotMatchPartialWords() {
        Set<String> allowed = Set.of("login", "2fa status");

        assertFalse(AliasNormalizer.matchesAllowed(allowed, "loginx"));
        assertFalse(AliasNormalizer.matchesAllowed(allowed, "2fa"));
        assertFalse(AliasNormalizer.matchesAllowed(allowed, "2fa disable"));
        assertFalse(AliasNormalizer.matchesAllowed(allowed, " login"));
    }

    @DisplayName("Allows nothing for empty input or an empty allow-list")
    @Test
    void emptyInputs() {
        assertFalse(AliasNormalizer.matchesAllowed(Set.of(), "login"));
        assertFalse(AliasNormalizer.matchesAllowed(Set.of("login"), ""));
        assertFalse(AliasNormalizer.matchesAllowed(null, "login"));
    }
}