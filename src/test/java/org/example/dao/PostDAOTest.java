package org.example.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.model.Post;
import org.example.model.User;
import org.junit.Ignore;
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
}