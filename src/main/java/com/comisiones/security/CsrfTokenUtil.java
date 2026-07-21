package com.comisiones.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

public final class CsrfTokenUtil {
    public static final String SESSION_ATTRIBUTE = "csrfToken";
    public static final String REQUEST_ATTRIBUTE = "csrfToken";
    public static final String PARAMETER_NAME = "csrfToken";
    public static final String HEADER_NAME = "X-CSRF-Token";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CsrfTokenUtil() {
    }

    public static String ensureToken(HttpSession session) {
        Object current = session.getAttribute(SESSION_ATTRIBUTE);
        if (current instanceof String && !((String) current).isBlank()) {
            return (String) current;
        }
        String generated = generateToken();
        session.setAttribute(SESSION_ATTRIBUTE, generated);
        return generated;
    }

    public static void exposeToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        request.setAttribute(REQUEST_ATTRIBUTE, ensureToken(session));
    }

    public static boolean isRequestTokenValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object sessionToken = session.getAttribute(SESSION_ATTRIBUTE);
        if (!(sessionToken instanceof String) || ((String) sessionToken).isBlank()) {
            return false;
        }
        String requestToken = request.getHeader(HEADER_NAME);
        if (requestToken == null || requestToken.isBlank()) {
            requestToken = request.getParameter(PARAMETER_NAME);
        }
        return requestToken != null && Objects.equals(sessionToken, requestToken);
    }

    static String generateToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
