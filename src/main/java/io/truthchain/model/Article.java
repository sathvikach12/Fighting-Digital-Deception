package io.truthchain.model;

public record Article(long id, long categoryId, String categoryName, String title, String description,
                      String publisher, java.time.LocalDate publishedAt, long views, int version,
                      String moderationStatus, boolean deleted, boolean trending) { }
