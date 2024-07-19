package org.example.dao;

import org.example.dao.interfaces.TagDAOInterface;
import org.example.model.Post;
import org.example.model.Tag;
import org.example.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagDAO implements TagDAOInterface {
    private final DataSource dataSource;

    public TagDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Tag tag) throws SQLException {
        String query = "INSERT INTO tags (name) VALUES (?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, tag.getName());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tag.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    // Read Tag by ID
    @Override
    public Tag getById(Long id) throws SQLException {
        String query = "SELECT * FROM tags WHERE id = ?";
        Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Tag tag = new Tag();
                tag.setId(resultSet.getLong("id"));
                tag.setName(resultSet.getString("name"));
                return tag;
            }
        return null;
    }

    // Read all Tags
    @Override
    public List<Tag> getAllTags() throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String query = "SELECT * FROM tags";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Tag tag = new Tag();
                tag.setId(resultSet.getLong("id"));
                tag.setName(resultSet.getString("name"));
                tags.add(tag);
            }
        }
        return tags;
    }

    // Update Tag
    @Override
    public void update(Tag tag) throws SQLException {
        String query = "UPDATE tags SET name = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, tag.getName());
            statement.setLong(2, tag.getId());
            statement.executeUpdate();
        }
    }

    // Delete Tag
    @Override
    public void delete(Long id) throws SQLException {
        String query = "DELETE FROM tags WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    // Add Tag to Post
    @Override
    public void addTagToPost(Long tagId, Long postId) throws SQLException {
        String query = "INSERT INTO tag_post_relationship (post_id, tag_id) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, postId);
            statement.setLong(2, tagId);
            statement.executeUpdate();
        }
    }

    // Remove Tag from Post
    @Override
    public void removeTagFromPost(Long tagId, Long postId) throws SQLException {
        String query = "DELETE FROM tag_post_relationship WHERE post_id = ? AND tag_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, postId);
            statement.setLong(2, tagId);
            statement.executeUpdate();
        }
    }

    // Get Tags by Post ID
    @Override
    public List<Tag> getTagsByPostId(Long postId) throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String query = "SELECT t.* FROM tags t INNER JOIN tag_post_relationship pt ON t.id = pt.tag_id WHERE pt.post_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, postId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Tag tag = new Tag();
                    tag.setId(resultSet.getLong("id"));
                    tag.setName(resultSet.getString("name"));
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    // Get Posts by Tag ID
    @Override
    public List<Post> getPostsByTagId(Long id) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT p.* FROM posts p INNER JOIN tag_post_relationship pt ON p.id = pt.post_id WHERE pt.tag_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Post post = new Post();
                    post.setId(resultSet.getLong("id"));
                    post.setTitle(resultSet.getString("title"));
                    post.setContent(resultSet.getString("content"));
                    posts.add(post);
                }
            }
        }
        return posts;
    }
}
