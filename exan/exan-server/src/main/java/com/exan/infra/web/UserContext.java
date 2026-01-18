package com.exan.infra.web;

import jakarta.servlet.http.HttpServletRequest;

public final class UserContext {
    private static final String HEADER_USER_ID = "X-User-Id";

    private UserContext() {
    }

    public static long currentUserId(HttpServletRequest request) {
        String v = request.getHeader(HEADER_USER_ID);
        if (v == null || v.isBlank()) {
            return 1L;
        }
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            return 1L;
        }
    }
}
