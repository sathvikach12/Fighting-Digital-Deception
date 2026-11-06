package io.truthchain.service;

import io.truthchain.util.Hashing;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArticleTextTest {
    @Test void canonicalTextCoversAllArticleFields(){String a=ArticleText.hash(1,"Title","Body","Publisher","2026-01-01");assertNotEquals(a,ArticleText.hash(2,"Title","Body","Publisher","2026-01-01"));assertNotEquals(a,ArticleText.hash(1,"Title","Changed","Publisher","2026-01-01"));}
    @Test void trimmingDoesNotCreateFalseContentChanges(){assertEquals(ArticleText.hash(1," Story "," Text "," Publisher ","2026-01-01"),ArticleText.hash(1,"Story","Text","Publisher","2026-01-01"));assertEquals(Hashing.sha256(ArticleText.canonical(1,"A","B","C","2026-01-01")),ArticleText.hash(1,"A","B","C","2026-01-01"));}
}
