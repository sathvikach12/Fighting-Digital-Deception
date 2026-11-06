package io.truthchain.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class QuantizationTest {
    @Test void cutoffDateBelongsToCurrentPeriod(){LocalDate cutoff=LocalDate.parse("2026-11-01");assertTrue(Quantization.isTrending(cutoff,cutoff));assertFalse(Quantization.isTrending(cutoff.minusDays(1),cutoff));assertTrue(Quantization.isTrending(cutoff.plusDays(1),cutoff));}
    @Test void missingDatesAreNotCurrent(){assertFalse(Quantization.isTrending(null,LocalDate.now()));assertFalse(Quantization.isTrending(LocalDate.now(),null));}
}
