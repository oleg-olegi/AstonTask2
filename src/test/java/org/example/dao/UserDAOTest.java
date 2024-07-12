package org.example.dao;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.dto.UserDTO;
import org.example.model.User;
import org.example.service.UserService;

import org.example.util.DataSourceUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class UserDAOTest {
    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private static DataSource dataSource;
    private static UserDAO userDAO;


    @BeforeAll
    public static void setUp() {

//        DataSourceUtil datasourceUtil = new DataSourceUtil();
//        System.setProperty("jdbc.url", postgresContainer.getJdbcUrl());
//        System.setProperty("jdbc.username", postgresContainer.getUsername());
//        System.setProperty("jdbc.password", postgresContainer.getPassword());
//        System.setProperty("jdbc.driverClassName", "org.postgresql.Driver");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresContainer.getJdbcUrl());
        config.setUsername(postgresContainer.getUsername());
        config.setPassword(postgresContainer.getPassword());
        config.setDriverClassName("org.postgresql.Driver");

        dataSource = new HikariDataSource(config);

//        dataSource = DataSourceUtil.getDataSource();
        userDAO = new UserDAO(dataSource);

        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users (id SERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255))");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void tearup() {
        postgresContainer.start();
    }


    @AfterAll
    public static void teardown() {
        postgresContainer.stop();
    }

    @Test
    public void testSaveAndGetById() throws SQLException {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        userDAO.save(user);

        User retrievedUser = userDAO.getById(user.getId());
        assertNotNull(retrievedUser);
        assertEquals("John Doe", retrievedUser.getName());
        assertEquals("john.doe@example.com", retrievedUser.getEmail());
    }

//    @Test
//    public void testUpdateUser() throws SQLException {
//        UserDTO userDTO = new UserDTO(null, "Jane Smith", "jane.smith@example.com");
//
//        userService.saveUser(userDTO);
//
//        List<UserDTO> users = userService.getAllUsers();
//        UserDTO savedUser = users.get(0);
//
//        savedUser.setName("Updated Name");
//        savedUser.setEmail("updated.email@example.com");
//
//        userService.updateUser(savedUser);
//
//        UserDTO updatedUser = userService.getUserById(savedUser.getId());
//        assertEquals(savedUser.getName(), updatedUser.getName());
//        assertEquals(savedUser.getEmail(), updatedUser.getEmail());
//    }
//
//    @Test
//    public void testDeleteUser() throws SQLException {
//        UserDTO userDTO = new UserDTO(null, "Alice Brown", "alice.brown@example.com");
//
//        userService.saveUser(userDTO);
//
//        List<UserDTO> usersBeforeDeletion = userService.getAllUsers();
//        assertEquals(1, usersBeforeDeletion.size());
//
//        UserDTO savedUser = usersBeforeDeletion.get(0);
//        Long userId = savedUser.getId();
//
//        userService.deleteUser(userId);
//
//        List<UserDTO> usersAfterDeletion = userService.getAllUsers();
//        assertTrue(usersAfterDeletion.isEmpty());
//    }
}
