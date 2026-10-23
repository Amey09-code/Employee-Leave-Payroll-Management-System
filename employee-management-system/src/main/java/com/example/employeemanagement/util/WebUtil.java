package com.example.employeemanagement.util;

import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

public final class WebUtil {

    private WebUtil() {
    }

    /** Builds an encoded query-string suffix ("&k=v&k2=v2") from key/value pairs, skipping blank values. */
    public static String qs(String... keyValues) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            String v = keyValues[i + 1];
            if (v != null && !v.isBlank()) {
                sb.append('&').append(keyValues[i]).append('=').append(UriUtils.encode(v, StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }
}
