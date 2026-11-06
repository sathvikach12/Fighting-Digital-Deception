package io.truthchain.model;

public record ModerationMatch(long articleId, String title, String matchedKeywords, String status,
                              java.time.Instant createdAt) { }
