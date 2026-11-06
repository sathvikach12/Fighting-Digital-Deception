package io.truthchain.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Deterministic SHA-256 helpers for article fingerprints and hash-chain blocks. */
public final class Hashing {
    private Hashing() { }

    public static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", impossible);
        }
    }

    /** Field delimiters and canonical ISO timestamps keep block serialization unambiguous. */
    public static String blockHash(long index, long articleId, int version, String timestamp,
                                   String contentHash, String previousHash) {
        return sha256(index + "|" + articleId + "|" + version + "|" + timestamp + "|" + contentHash + "|" + previousHash);
    }
}
