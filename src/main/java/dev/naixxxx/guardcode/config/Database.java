package dev.naixxxx.guardcode.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);
    private final HikariDataSource dataSource;

    public Database(AppSettings settings) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(settings.get("jdbc.url"));
        cfg.setUsername(settings.get("jdbc.user"));
        cfg.setPassword(settings.get("jdbc.password"));
        cfg.setMaximumPoolSize(settings.getInt("jdbc.poolSize", 8));
        this.dataSource = new HikariDataSource(cfg);
    }

    public DataSource dataSource() { return dataSource; }

    public void initSchema() {
        try (var in = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) throw new IllegalStateException("schema.sql not found");
            String sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            try (Connection con = dataSource.getConnection(); Statement st = con.createStatement()) {
                st.execute(sql);
            }
            log.info("Database schema checked");
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize database schema", e);
        }
    }

    public void close() { dataSource.close(); }
}
