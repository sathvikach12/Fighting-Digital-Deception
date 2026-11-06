package io.truthchain.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {
    @Test void validatesEmailAndLength(){assertTrue(Validation.email("writer@example.test"));assertFalse(Validation.email("not-an-email"));assertTrue(Validation.required("Name",120));assertFalse(Validation.required(" ",120));}
    @Test void validatesDateIdsAndPasswordBounds(){assertTrue(Validation.date("2026-06-30"));assertFalse(Validation.date("tomorrow"));assertTrue(Validation.positiveId("42"));assertFalse(Validation.positiveId("0"));assertTrue(Validation.strongEnoughPassword("safe-demo-pass"));assertFalse(Validation.strongEnoughPassword("short"));assertFalse(Validation.strongEnoughPassword("a".repeat(73)));}
}
