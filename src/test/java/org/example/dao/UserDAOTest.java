package org.example.dao;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.model.User;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresContainer.getJdbcUrl());
        config.setUsername(postgresContainer.getUsername());
        config.setPassword(postgresContainer.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        dataSource = new HikariDataSource(config);
        userDAO = new UserDAO(dataSource);
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users (id SERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS posts (id SERIAL PRIMARY KEY, user_id " +
                    "INTEGER REFERENCES users(id), title VARCHAR(255), content TEXT)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void cleanData() {
//        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl(postgresContainer.getJdbcUrl());
//        config.setUsername(postgresContainer.getUsername());
//        config.setPassword(postgresContainer.getPassword());
//        config.setDriverClassName("org.postgresql.Driver");
//        dataSource = new HikariDataSource(config);
//        userDAO = new UserDAO(dataSource);
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM posts");
            stmt.execute("DELETE FROM users");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void start() {
        postgresContainer.start();
    }


    @AfterAll
    public static void teardown() {
        postgresContainer.stop();
    }

    @Test
    public void testSave() throws SQLException {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        userDAO.save(user);

        User retrievedUser = userDAO.getById(user.getId());
        assertNotNull(retrievedUser);
        assertEquals("John Doe", retrievedUser.getName());
        assertEquals("john.doe@example.com", retrievedUser.getEmail());
    }

    @Test
    public void testGetById() throws SQLException {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        userDAO.save(user);

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Sarah Connor");
        user2.setEmail("sarah@example.com");
        userDAO.save(user2);

        User retrievedUser = userDAO.getById(user.getId());
        assertNotNull(retrievedUser);
        assertEquals("John Doe", retrievedUser.getName());
        assertEquals("john.doe@example.com", retrievedUser.getEmail());

        User retrievedUser2 = userDAO.getById(user2.getId());
        assertNotNull(retrievedUser2);
        assertEquals("Sarah Connor", retrievedUser2.getName());
        assertEquals("sarah@example.com", retrievedUser2.getEmail());
    }

    @Test
    public void GetByIdReturnNull() throws SQLException {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        userDAO.save(user);

        User retrievedUser = userDAO.getById(user.getId());
        assertNotNull(retrievedUser);
        assertEquals("John Doe", retrievedUser.getName());
        assertEquals("john.doe@example.com", retrievedUser.getEmail());

        User retrievedNullUser = userDAO.getById(10L);
        assertNull(retrievedNullUser);
    }

    @Test
    public void testUpdateUser() throws SQLException {
        User userDTO = new User(1L, "Jane Smith", "jane.smith@example.com");

        userDAO.save(userDTO);

        List<User> users = userDAO.getAllUsers();
        User savedUser = users.get(0);

        savedUser.setName("Updated Name");
        savedUser.setEmail("updated.email@example.com");

        userDAO.update(savedUser);

        User updatedUser = userDAO.getById(savedUser.getId());
        assertEquals(savedUser.getName(), updatedUser.getName());
        assertEquals(savedUser.getEmail(), updatedUser.getEmail());
    }

    @Test
    public void testDeleteUser() throws SQLException {
        User userDTO = new User(1L, "Alice Brown", "alice.brown@example.com");

        userDAO.save(userDTO);

        List<User> usersBeforeDeletion = userDAO.getAllUsers();
        assertEquals(1, usersBeforeDeletion.size());

        User savedUser = usersBeforeDeletion.get(0);
        Long userId = savedUser.getId();

        userDAO.delete(userId);

        List<User> usersAfterDeletion = userDAO.getAllUsers();
        assertTrue(usersAfterDeletion.isEmpty());
    }
}
