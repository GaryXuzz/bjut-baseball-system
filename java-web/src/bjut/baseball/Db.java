package bjut.baseball;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Db {
    private static final Properties PROPS = new Properties();

    static {
        Path config = Path.of("config.properties");
        if (Files.exists(config)) {
            try (InputStream in = Files.newInputStream(config)) {
                PROPS.load(in);
            } catch (IOException ex) {
                throw new ExceptionInInitializerError("Cannot read java-web/config.properties: " + ex.getMessage());
            }
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // DriverManager can still find the driver if Connector/J is on the classpath.
        }
    }

    private Db() {
    }

    public static String get(String key, String defaultValue) {
        String envValue = envForKey(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return PROPS.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)).trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static Connection connect() throws SQLException {
        String rawUrl = get("db.url", "");
        if (rawUrl.isBlank()) {
            throw new SQLException("Missing database URL. Set DB_URL or db.url in config.properties.");
        }
        String user = get("db.user", "");
        String password = get("db.password", "");
        DbUrl dbUrl = normalizeUrl(rawUrl);
        if (user.isBlank()) {
            user = dbUrl.user == null ? "" : dbUrl.user;
        }
        if (password.isBlank()) {
            password = dbUrl.password == null ? "" : dbUrl.password;
        }
        return DriverManager.getConnection(dbUrl.jdbcUrl, user, password);
    }

    private static String envForKey(String key) {
        return switch (key) {
            case "server.port" -> firstEnv("PORT", "SERVER_PORT");
            case "db.url" -> firstEnv("DB_URL", "MYSQL_URL", "DATABASE_URL");
            case "db.user" -> firstEnv("DB_USER", "MYSQLUSER", "MYSQL_USER");
            case "db.password" -> firstEnv("DB_PASSWORD", "MYSQLPASSWORD", "MYSQL_PASSWORD");
            case "auth.demo.username" -> firstEnv("AUTH_DEMO_USERNAME");
            case "auth.demo.password" -> firstEnv("AUTH_DEMO_PASSWORD");
            default -> firstEnv(key.toUpperCase().replace('.', '_'));
        };
    }

    private static String firstEnv(String... names) {
        for (String name : names) {
            String value = System.getenv(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static DbUrl normalizeUrl(String rawUrl) {
        if (rawUrl.startsWith("jdbc:mysql://")) {
            return new DbUrl(withSslMode(rawUrl), null, null);
        }
        if (rawUrl.startsWith("mysql://")) {
            URI uri = URI.create(rawUrl);
            String user = null;
            String password = null;
            if (uri.getRawUserInfo() != null) {
                String[] parts = uri.getRawUserInfo().split(":", 2);
                user = decode(parts[0]);
                if (parts.length > 1) {
                    password = decode(parts[1]);
                }
            }
            StringBuilder jdbc = new StringBuilder("jdbc:mysql://")
                    .append(uri.getHost());
            if (uri.getPort() > 0) {
                jdbc.append(":").append(uri.getPort());
            }
            String path = uri.getPath();
            jdbc.append(path == null || path.isBlank() ? "/" : path);
            String query = uri.getRawQuery();
            if (query == null || query.isBlank()) {
                jdbc.append("?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai");
            } else {
                jdbc.append("?").append(query.replace("ssl-mode=", "sslMode="));
            }
            return new DbUrl(withSslMode(jdbc.toString()), user, password);
        }
        return new DbUrl(withSslMode(rawUrl), null, null);
    }

    private static String withSslMode(String jdbcUrl) {
        String sslMode = firstEnv("DB_SSL_MODE", "MYSQL_SSL_MODE");
        if (sslMode == null || sslMode.isBlank() || jdbcUrl.contains("sslMode=")) {
            return jdbcUrl;
        }
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslMode=" + sslMode;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private record DbUrl(String jdbcUrl, String user, String password) {
    }
}
