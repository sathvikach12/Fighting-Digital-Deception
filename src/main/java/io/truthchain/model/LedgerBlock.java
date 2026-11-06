package io.truthchain.model;

public record LedgerBlock(long id, long blockIndex, long articleId, int version, String timestamp,
                          String contentHash, String previousHash, String blockHash, String title) { }
