package io.truthchain.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SuspiciousMatcherTest {
    @Test void detectsConfiguredKeywordsWithoutCaseSensitivity(){assertEquals(List.of("miracle cure"),SuspiciousMatcher.matches("A MIRACLE CURE is promised",List.of("miracle cure","secret plan")));}
    @Test void ignoresBlankKeywordsAndDoesNotInferTruth(){assertTrue(SuspiciousMatcher.matches("Ordinary report",List.of(" ","miracle cure")).isEmpty());}
}
