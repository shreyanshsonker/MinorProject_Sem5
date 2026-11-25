package com.rbac.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnectionUtil — Singleton database connection utility.
 *
 * Design Pattern: Singleton (Architecture Doc §11)
 * Responsibility: Provides JDBC connections to the DAO layer.
 * Config source:  db.properties (Architecture Doc §12 — no hardcoded credentials)
 *
 * Per AI Rules §D2: Driver class loaded from properties, not assumed.
 */
public class DBConnectionUtil {

    private static final String PROPERTIES_FILE = "db.properties";

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    // Load properties once at class-load time
    static {
        try (InputStream input = DBConnectionUtil.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {

            if (input == null) {
                throw new RuntimeException("db.properties not found on classpath. "
                        + "Ensure it exists in src/main/resources/");
            }

            Properties props = new Properties();
            props.load(input);

            String driver = props.getProperty("db.driver");
            dbUrl = props.getProperty("db.url");
            dbUser = props.getProperty("db.user");
            dbPassword = props.getProperty("db.password");

            // Load JDBC driver class
            Class.forName(driver);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found. "
                    + "Check that mysql-connector-j is in the WAR's WEB-INF/lib.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load database configuration.", e);
        }
    }

    /**
     * Returns a new JDBC connection using the configured properties.
     * Callers are responsible for closing the connection.
     *
     * @return a live Connection to the rbac_system database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    // Prevent instantiation — utility class
    private DBConnectionUtil() {
    }
}
