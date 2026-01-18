package com.exan.infra.util;

public final class TextNormalizeUtil {
    private TextNormalizeUtil() {
    }

    public static String normalizeForHash(String text) {
        if (text == null) {
            return "";
        }
        String t = text;
        t = t.replaceAll("\\s+", "");
        t = t.replaceAll("[\\u3000]", "");
        t = t.replaceAll("[，。！？；：、】【（）()\\[\\]{}<>《》]", "");
        return t.toLowerCase();
    }
}
