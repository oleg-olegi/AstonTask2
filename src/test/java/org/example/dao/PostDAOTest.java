package org.example.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class PostDAOTest {
    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private static DataSource dataSource;
    private static PostDAO postDAO;
    private static UserDAO userDAO;


    @BeforeAll
    public static void setUp() {
        HikariConfig config = new HikariConfig();
        System.out.println(postgresContainer.getJdbcUrl());
        config.setJdbcUrl(postgresContainer.getJdbcUrl());
        config.setUsername(postgresContainer.getUsername());
        config.setPassword(postgresContainer.getPassword());
        config.setDriverClassName("org.postgresql.Driver");

        dataSource = new HikariDataSource(config);
        postDAO = new PostDAO(dataSource);
        userDAO = new UserDAO(dataSource);
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE users (id SERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255))");
            stmt.execute("CREATE TABLE posts (id SERIAL PRIMARY KEY, title VARCHAR(255), content VARCHAR(255), user_id INTEGER REFERENCES users(id))");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void cleanData() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresContainer.getJdbcUrl());
        config.setUsername(postgresContainer.getUsername());
        config.setPassword(postgresContainer.getPassword());
        config.setDriverClassName("org.postgresql.Driver");

        dataSource = new HikariDataSource(config);
        postDAO = new PostDAO(dataSource);
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
        user.setId(1L);
        user.setName("Ivan");
        user.setEmail("ivan@gmail.com");
        userDAO.save(user);

        Post post = new Post();
        post.setTitle("Sample Title");
        post.setContent("Sample Content");
        post.setUser(user);
        postDAO.save(post);

        Post retrievedPost = postDAO.getById(post.getId());
        assertNotNull(retrievedPost);
        assertEquals("Sample Title", retrievedPost.getTitle());
        assertEquals("Sample Content", retrievedPost.getContent());
        assertTrue(post.getId() > 0);
    }

    @Test
    public void testGetById() throws SQLException {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        userDAO.save(user);

        Post post1 = new Post();
        post1.setTitle("First Post");
        post1.setContent("First Content");
        post1.setUser(user);
        postDAO.save(post1);

        Post post2 = new Post();
        post2.setTitle("Second Post");
        post2.setContent("Second Content");
        post2.setUser(user);
        postDAO.save(post2);

        Post retrievedPost1 = postDAO.getById(post1.getId());
        assertNotNull(retrievedPost1);
        assertEquals("First Post", retrievedPost1.getTitle());
        assertEquals("First Content", retrievedPost1.getContent());

        Post retrievedPost2 = postDAO.getById(post2.getId());
        assertNotNull(retrievedPost2);
        assertEquals("Second Post", retrievedPost2.getTitle());
        assertEquals("Second Content", retrievedPost2.getContent());
    }

    @Test
    public void testGetByIdReturnNull() throws SQLException {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        userDAO.save(user);

        Post post = new Post();
        post.setTitle("Sample Title");
        post.setContent("Sample Content");
        post.setUser(user);
        postDAO.save(post);

        Post retrievedPost = postDAO.getById(post.getId());
        assertNotNull(retrievedPost);
        assertEquals("Sample Title", retrievedPost.getTitle());
        assertEquals("Sample Content", retrievedPost.getContent());

        Post retrievedNullPost = postDAO.getById(10L);
        assertNull(retrievedNullPost);
    }

    @Test
    public void testUpdatePost() throws SQLException {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        userDAO.save(user);

        Post post = new Post();
        post.setTitle("Original Title");
        post.setContent("Original Content");
        post.setUser(user);
        postDAO.save(post);

        List<Post> posts = postDAO.getAllPosts();
        Post savedPost = posts.get(0);

        savedPost.setTitle("Updated Title");
        savedPost.setContent("Updated Content");

        postDAO.update(savedPost);

        Post updatedPost = postDAO.getById(savedPost.getId());
        assertEquals("Updated Title", updatedPost.getTitle());
        assertEquals("Updated Content", updatedPost.getContent());
    }

    @Test
    public void testDeletePost() throws SQLException {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        userDAO.save(user);

        Post post = new Post();
        post.setTitle("Title to Delete");
        post.setContent("Content to Delete");
        post.setUser(user);
        postDAO.save(post);

        List<Post> postsBeforeDeletion = postDAO.getAllPosts();
        assertEquals(1, postsBeforeDeletion.size());

        Post savedPost = postsBeforeDeletion.get(0);
        Long postId = savedPost.getId();

        postDAO.delete(postId);

        List<Post> postsAfterDeletion = postDAO.getAllPosts();
        assertTrue(postsAfterDeletion.isEmpty());
    }

    @Test
    public void testSaveWithGeneratedKeys() throws SQLException {
        User user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        userDAO.save(user);

        Post post = new Post();
        post.setTitle("Generated Keys Title");
        post.setContent("Generated Keys Content");
        post.setUser(user);
        postDAO.save(post);

        assertNotEquals(0, post.getId(), "Post ID should be generated and not zero");
    }

    @Test
    public void testSaveWithoutGeneratedKeys() throws SQLException {
        User user = new User();
        user.setName("Bob");
        user.setEmail("bob@example.com");
        userDAO.save(user);
        // Создаем мок PostDAO и ResultSet для проверки случая без сгенерированного ключа
        DataSource mockDataSource = mock(DataSource.class);
        PostDAO mockPostDAO = new PostDAO(mockDataSource);

        try (Connection mockConnection = mock(Connection.class);
             PreparedStatement mockStmt = mock(PreparedStatement.class);
             ResultSet mockRs = mock(ResultSet.class)) {

            when(mockDataSource.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString(), eq(PreparedStatement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
            when(mockStmt.getGeneratedKeys()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            Post mockPost = new Post();
            mockPost.setTitle("No Generated Keys Title");
            mockPost.setContent("No Generated Keys Content");
            mockPost.setUser(user);

            mockPostDAO.save(mockPost);

            assertNull(mockPost.getId(), "Post ID should remain null if no keys were generated");
        }
    }
}