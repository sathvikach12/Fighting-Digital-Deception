package io.truthchain.service;

import java.util.ArrayList;
import java.util.List;

/** Keyword matches create a human-review signal; they do not establish truth or falsity. */
public final class SuspiciousMatcher {
    private SuspiciousMatcher() { }
    public static List<String> matches(String text, List<String> configuredKeywords) {
        String normalized = text == null ? "" : text.toLowerCase(java.util.Locale.ROOT);
        List<String> hits = new ArrayList<>();
        for (String keyword : configuredKeywords) {
            if (keyword != null && !keyword.isBlank() && normalized.contains(keyword.toLowerCase(java.util.Locale.ROOT))) hits.add(keyword);
        }
        return List.copyOf(hits);
    }
}
