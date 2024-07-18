package org.example.dao;

import org.example.dao.interfaces.PostDaoInterface;
import org.example.model.Post;
import org.example.model.Tag;
import org.example.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO implements PostDaoInterface {
    private final DataSource dataSource;
    private final TagDAO tagDAO = new TagDAO();

    public PostDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Post getById(Long id) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT p.id, p.title, p.content, p.user_id, u.name as user_name, u.email as user_email " +
                        "FROM posts p JOIN users u ON p.user_id = u.id WHERE p.id = ?");
        stmt.setLong(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            createPostFromResultSet(rs);
        }
        return null;
    }

    @Override
    public List<Post> getAllPosts() throws SQLException {
        List<Post> posts = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT p.id, p.title, p.content, p.user_id, u.name as user_name, u.email as user_email " +
                            "FROM posts p JOIN users u ON p.user_id = u.id");
            while (rs.next()) {
                posts.add(createPostFromResultSet(rs));
            }
        }
        return posts;
    }

    @Override
    public void save(Post post) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO posts (title, content, user_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getContent());
            stmt.setLong(3, post.getUser().getId());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                post.setId(rs.getLong(1));
            }
        }
    }

    @Override
    public void update(Post post) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE posts SET title = ?, content = ?, user_id = ? WHERE id = ?");
            stmt.setString(1, post.getTitle());
            stmt.setString(2, post.getContent());
            stmt.setLong(3, post.getUser().getId());
            stmt.setLong(4, post.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM posts WHERE id = ?");
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private Post createPostFromResultSet(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));

        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setName(rs.getString("user_name"));
        user.setEmail(rs.getString("user_email"));
        post.setUser(user);

        List<Tag> tags = tagDAO.getTagsByPostId(post.getId());
        post.setTags(tags);

        return post;
    }
}
