package org.example.dao;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletException;
import org.example.model.Post;
import org.example.model.User;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    public void testGetUserWithPosts() throws SQLException {
        // Сохраняем пользователя
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        userDAO.save(user);

        // Сохраняем посты для пользователя
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO posts (user_id, title, content) VALUES (" + user.getId() + ", 'Post 1', 'Content 1')");
            stmt.execute("INSERT INTO posts (user_id, title, content) VALUES (" + user.getId() + ", 'Post 2', 'Content 2')");
        }

        // Получаем пользователя с постами
        User retrievedUser = userDAO.getById(user.getId());
        assertNotNull(retrievedUser);
        assertEquals("John Doe", retrievedUser.getName());
        assertEquals("john.doe@example.com", retrievedUser.getEmail());
        assertNotNull(retrievedUser.getPosts());
        assertEquals(2, retrievedUser.getPosts().size());

        // Проверяем посты
        Post post1 = retrievedUser.getPosts().get(0);
        assertEquals("Post 1", post1.getTitle());
        assertEquals("Content 1", post1.getContent());

        Post post2 = retrievedUser.getPosts().get(1);
        assertEquals("Post 2", post2.getTitle());
        assertEquals("Content 2", post2.getContent());
    }

    @Test
    public void testGetAllUsersWithPosts() throws SQLException {
        // Сохраняем пользователей и посты
        User user1 = new User();
        user1.setName("John Doe");
        user1.setEmail("john.doe@example.com");
        userDAO.save(user1);

        User user2 = new User();
        user2.setName("Sarah Connor");
        user2.setEmail("sarah.connor@example.com");
        userDAO.save(user2);

        // Сохраняем посты для пользователей
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO posts (user_id, title, content) VALUES (" + user1.getId() + ", 'Post 1', 'Content 1')");
            stmt.execute("INSERT INTO posts (user_id, title, content) VALUES (" + user1.getId() + ", 'Post 2', 'Content 2')");
            stmt.execute("INSERT INTO posts (user_id, title, content) VALUES (" + user2.getId() + ", 'Post A', 'Content A')");
        }

        // Получаем всех пользователей с постами
        List<User> users = userDAO.getAllUsers();
        assertNotNull(users);
        assertEquals(2, users.size());

        // Проверяем первого пользователя и его посты
        User retrievedUser1 = users.get(0);
        assertEquals("John Doe", retrievedUser1.getName());
        assertEquals("john.doe@example.com", retrievedUser1.getEmail());
        assertNotNull(retrievedUser1.getPosts());
        assertEquals(2, retrievedUser1.getPosts().size());

        Post post1 = retrievedUser1.getPosts().get(0);
        assertEquals("Post 1", post1.getTitle());
        assertEquals("Content 1", post1.getContent());

        Post post2 = retrievedUser1.getPosts().get(1);
        assertEquals("Post 2", post2.getTitle());
        assertEquals("Content 2", post2.getContent());

        // Проверяем второго пользователя и его посты
        User retrievedUser2 = users.get(1);
        assertEquals("Sarah Connor", retrievedUser2.getName());
        assertEquals("sarah.connor@example.com", retrievedUser2.getEmail());
        assertNotNull(retrievedUser2.getPosts());
        assertEquals(1, retrievedUser2.getPosts().size());

        Post postA = retrievedUser2.getPosts().get(0);
        assertEquals("Post A", postA.getTitle());
        assertEquals("Content A", postA.getContent());
    }

    @Test
    public void testSaveWithGeneratedKeys() throws SQLException {
        User user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        userDAO.save(user);

        assertNotEquals(0, user.getId(), "User ID should be generated and not zero");
    }

    @Test
    public void testSaveWithoutGeneratedKeys() throws SQLException {
        // Создаем мок UserDAO и ResultSet для проверки случая без сгенерированного ключа
        DataSource mockDataSource = mock(DataSource.class);
        UserDAO mockUserDAO = new UserDAO(mockDataSource);

        try (Connection mockConnection = mock(Connection.class);
             PreparedStatement mockStmt = mock(PreparedStatement.class);
             ResultSet mockRs = mock(ResultSet.class)) {

            when(mockDataSource.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
            when(mockStmt.getGeneratedKeys()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            User mockUser = new User();
            mockUser.setName("Bob");
            mockUser.setEmail("bob@example.com");

            mockUserDAO.save(mockUser);

            assertNull(mockUser.getId(), "User ID should remain null if no keys were generated");
        }
    }
}

