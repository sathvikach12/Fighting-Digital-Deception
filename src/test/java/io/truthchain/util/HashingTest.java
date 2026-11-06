package io.truthchain.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashingTest {
    @Test void sha256UsesUtf8AndExpectedDigest(){assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",Hashing.sha256("abc"));}
    @Test void blockHashIsDeterministicAndSensitiveToEveryField(){String first=Hashing.blockHash(1,7,2,"2026-01-02T03:04:05Z","content","previous");assertEquals(first,Hashing.blockHash(1,7,2,"2026-01-02T03:04:05Z","content","previous"));assertNotEquals(first,Hashing.blockHash(1,7,3,"2026-01-02T03:04:05Z","content","previous"));}
}
