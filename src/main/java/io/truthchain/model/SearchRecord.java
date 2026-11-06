package io.truthchain.model;

public record SearchRecord(long id, long userId, String userName, String query, String categoryName,
                           int resultCount, java.time.Instant searchedAt) { }
