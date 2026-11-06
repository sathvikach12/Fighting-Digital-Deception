package io.truthchain.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

public final class Csrf {
    public static final String SESSION_KEY = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();
    private Csrf() { }

    public static String token(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_KEY);
        if (token == null) {
            byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_KEY, token);
        }
        return token;
    }

    public static boolean valid(HttpServletRequest request) {
        Object expected = request.getSession(false) == null ? null : request.getSession(false).getAttribute(SESSION_KEY);
        String supplied = request.getParameter("_csrf");
        return expected instanceof String && supplied != null && constantTimeEquals((String) expected, supplied);
    }

    private static boolean constantTimeEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(a.getBytes(java.nio.charset.StandardCharsets.UTF_8), b.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
