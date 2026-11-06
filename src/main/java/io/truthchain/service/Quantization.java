package io.truthchain.service;

import java.time.LocalDate;

public final class Quantization {
    private Quantization() { }
    /** The configured cutoff day is included in the current/trending period. */
    public static boolean isTrending(LocalDate publishedAt, LocalDate cutoff) {
        return publishedAt != null && cutoff != null && !publishedAt.isBefore(cutoff);
    }
}
