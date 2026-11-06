package io.truthchain.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class Validation {
    private static final Pattern EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private Validation() { }

    public static boolean email(String value) { return value != null && EMAIL.matcher(value.trim()).matches() && value.length() <= 190; }
    public static boolean required(String value, int max) { return value != null && !value.isBlank() && value.trim().length() <= max; }
    public static boolean positiveId(String value) {
        try { return Long.parseLong(value) > 0; } catch (RuntimeException ex) { return false; }
    }
    public static boolean date(String value) {
        try { LocalDate.parse(value); return true; } catch (RuntimeException ex) { return false; }
    }
    public static boolean strongEnoughPassword(String value) { return value != null && value.length() >= 10 && value.length() <= 72; }
}
