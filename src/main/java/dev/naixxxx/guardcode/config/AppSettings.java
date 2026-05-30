package dev.naixxxx.guardcode.config;

import java.io.InputStream;
import java.util.Properties;

public final class AppSettings {
    private final Properties props = new Properties();

    public AppSettings(String resource) {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (input == null) throw new IllegalStateException("Missing config: " + resource);
            props.load(input);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load config " + resource, e);
        }
    }

    public String get(String key) { return props.getProperty(key); }
    public String get(String key, String fallback) { return props.getProperty(key, fallback); }
    public int getInt(String key, int fallback) {
        String value = props.getProperty(key);
        return value == null || value.isBlank() ? fallback : Integer.parseInt(value.trim());
    }
    public long getLong(String key, long fallback) {
        String value = props.getProperty(key);
        return value == null || value.isBlank() ? fallback : Long.parseLong(value.trim());
    }
    public Properties asProperties() { return props; }
}
