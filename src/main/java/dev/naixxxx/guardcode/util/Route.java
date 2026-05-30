package dev.naixxxx.guardcode.util;

import com.sun.net.httpserver.HttpExchange;

public final class Route {
    private Route() {}
    public static boolean is(HttpExchange ex, String method, String path) {
        return ex.getRequestMethod().equalsIgnoreCase(method) && ex.getRequestURI().getPath().equals(path);
    }
    public static boolean starts(HttpExchange ex, String method, String prefix) {
        return ex.getRequestMethod().equalsIgnoreCase(method) && ex.getRequestURI().getPath().startsWith(prefix);
    }
}
