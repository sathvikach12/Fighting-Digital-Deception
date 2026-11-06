package io.truthchain.service;

import io.truthchain.util.Hashing;

/** Defines exactly which article fields are covered by an integrity fingerprint. */
public final class ArticleText {
    private ArticleText() { }
    public static String canonical(long categoryId, String title, String description, String publisher, String publishedAt) {
        return categoryId + "|" + clean(title) + "|" + clean(description) + "|" + clean(publisher) + "|" + publishedAt;
    }
    public static String hash(long categoryId, String title, String description, String publisher, String publishedAt) {
        return Hashing.sha256(canonical(categoryId, title, description, publisher, publishedAt));
    }
    private static String clean(String value) { return value == null ? "" : value.trim(); }
}
