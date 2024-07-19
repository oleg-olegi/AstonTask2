package org.example.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DataSourceUtil {
    private static final HikariDataSource dataSource;

    static {
        try {
            Properties properties = loadProperties();
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("jdbc.url"));
            config.setUsername(properties.getProperty("jdbc.username"));
            config.setPassword(properties.getProperty("jdbc.password"));
            config.setDriverClassName(properties.getProperty("jdbc.driverClassName"));

            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize DataSourceUtil", e);
        }
    }

    private static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = DataSourceUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("db.properties file not found");
            }
            properties.load(input);
        }
        return properties;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
