package io.truthchain.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens short-lived JDBC connections from environment-configured settings. */
public final class Database {
    private Database() { }

    public static Connection getConnection() throws SQLException {
        String url = setting("DB_URL", "jdbc:mysql://localhost:3306/truthchain?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        String user = setting("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
