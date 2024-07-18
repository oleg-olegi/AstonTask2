package org.example.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.model.Post;
import org.example.model.Tag;
import org.example.model.User;
import org.junit.jupiter.api.*;
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
public class TagDAOTest {
    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private static DataSource dataSource;
    private static TagDAO tagDAO;
    private static PostDAO postDAO;


    @BeforeAll
    public static void setUp() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        config.setUsername(postgreSQLContainer.getUsername());
        config.setPassword(postgreSQLContainer.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        dataSource = new HikariDataSource(config);
        tagDAO = new TagDAO(dataSource);
        postDAO = new PostDAO(dataSource);
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS tags (id SERIAL PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS posts (id SERIAL PRIMARY KEY, title VARCHAR(255)," +
                    " content TEXT, user_id INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS tag_post_relationship (tag_id INTEGER REFERENCES tags(id)," +
                    " post_id INTEGER REFERENCES posts(id), PRIMARY KEY(tag_id, post_id))");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void cleanData() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        config.setUsername(postgreSQLContainer.getUsername());
        config.setPassword(postgreSQLContainer.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        dataSource = new HikariDataSource(config);
        tagDAO = new TagDAO(dataSource);
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM tag_post_relationship");
            stmt.execute("DELETE FROM tags");
            stmt.execute("DELETE FROM posts");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void start() {
        postgreSQLContainer.start();
    }


    @AfterAll
    public static void teardown() {
        postgreSQLContainer.stop();
    }

    @Test
    public void testSave() throws SQLException {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");

        tagDAO.save(tag);

        Tag retrievedTag = tagDAO.getById(tag.getId());
        assertNotNull(retrievedTag);
        assertEquals("Test Tag", retrievedTag.getName());
        assertTrue(tag.getId() > 0);
    }

    @Test
    public void testGetById() throws SQLException {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");
        tagDAO.save(tag);

        Tag tag1 = new Tag();
        tag1.setId(2L);
        tag1.setName("Test 1 Tag 1");
        tagDAO.save(tag1);

        Tag retrievedTag1 = tagDAO.getById(tag.getId());
        assertNotNull(retrievedTag1);
        assertEquals("Test Tag", retrievedTag1.getName());

        Tag retrievedTag2 = tagDAO.getById(tag1.getId());
        assertNotNull(retrievedTag2);
        assertEquals("Test 1 Tag 1", retrievedTag2.getName());
    }

    @Test
    public void testGetByIdReturnNull() throws SQLException {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");

        tagDAO.save(tag);

        Tag retrievedTag = tagDAO.getById(tag.getId());
        assertNotNull(retrievedTag);
        assertEquals("Test Tag", retrievedTag.getName());

        Tag retrievedNullTag = tagDAO.getById(10L);
        assertNull(retrievedNullTag);
    }

    @Test
    public void testUpdateTag() throws SQLException {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");

        tagDAO.save(tag);

        List<Tag> tags = tagDAO.getAllTags();
        Tag savedTag = tags.get(0);

        savedTag.setName("Updated Tag");

        tagDAO.update(savedTag);

        Tag updatedTag = tagDAO.getById(savedTag.getId());
        assertEquals("Updated Tag", updatedTag.getName());
    }

    @Test
    public void testDeleteTag() throws SQLException {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");

        tagDAO.save(tag);

        List<Tag> tagsBeforeDeletion = tagDAO.getAllTags();
        assertEquals(1, tagsBeforeDeletion.size());

        Tag savedTag = tagsBeforeDeletion.get(0);
        Long tagId = savedTag.getId();

        tagDAO.delete(tagId);

        List<Tag> tagsAfterDeletion = tagDAO.getAllTags();
        assertTrue(tagsAfterDeletion.isEmpty());
    }

    @Test
    public void testSaveWithGeneratedKeys() throws SQLException {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");

        tagDAO.save(tag);

        assertNotEquals(0, tag.getId(), "Tag ID should be generated and not zero");
    }

    @Test
    public void testSaveWithoutGeneratedKeys() throws SQLException {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");

        tagDAO.save(tag);
        // Создаем мок PostDAO и ResultSet для проверки случая без сгенерированного ключа
        DataSource mockDataSource = mock(DataSource.class);
        TagDAO mockTagDAO = new TagDAO(mockDataSource);

        try (Connection mockConnection = mock(Connection.class);
             PreparedStatement mockStmt = mock(PreparedStatement.class);
             ResultSet mockRs = mock(ResultSet.class)) {

            when(mockDataSource.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString(), eq(PreparedStatement.RETURN_GENERATED_KEYS))).thenReturn(mockStmt);
            when(mockStmt.getGeneratedKeys()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            Tag mockTag = new Tag();
            mockTag.setName("No Generated Keys Name");

            mockTagDAO.save(mockTag);

            assertNull(mockTag.getId(), "Tag ID should remain null if no keys were generated");
        }
    }

    @Test
    public void testAddTagToPost() throws SQLException {
        // Создание тега и поста
        Tag tag = new Tag(1L, "TestTag");
        tagDAO.save(tag);

        User user = new User(1L, "TestUser", "Test@email");

        Post post = new Post(1L, "TestTitle", "TestContent", user);
        postDAO.save(post);

        // Связывание тега с постом
        tagDAO.addTagToPost(tag.getId(), post.getId());

        // Проверка, что тег успешно связан с постом
        List<Tag> tagsByPostId = tagDAO.getTagsByPostId(post.getId());
        Assertions.assertEquals(1, tagsByPostId.size());
        Assertions.assertEquals(tag.getId(), tagsByPostId.get(0).getId());

        List<Post> postsByTagId = tagDAO.getPostsByTagId(tag.getId());
        Assertions.assertEquals(1, postsByTagId.size());
        Assertions.assertEquals(post.getId(), postsByTagId.get(0).getId());
    }

    @Test
    public void testRemoveTagFromPost() throws SQLException {
        // Создание тега и поста
        Tag tag = new Tag(1L, "TestTag");
        tagDAO.save(tag);

        User user = new User(1L, "TestUser", "Test@email");

        Post post = new Post(1L, "TestTitle", "TestContent", user);
        postDAO.save(post);
        // Связывание тега с постом
        tagDAO.addTagToPost(tag.getId(), post.getId());

        // Удаление связи между тегом и постом
        tagDAO.removeTagFromPost(tag.getId(), post.getId());

        // Проверка, что связь успешно удалена
        List<Tag> tagsByPostId = tagDAO.getTagsByPostId(post.getId());
        Assertions.assertEquals(0, tagsByPostId.size());

        List<Post> postsByTagId = tagDAO.getPostsByTagId(tag.getId());
        Assertions.assertEquals(0, postsByTagId.size());
    }

}
