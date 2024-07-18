package org.example.dao;

import org.example.dao.interfaces.UserDaoInterface;
import org.example.model.Post;
import org.example.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserDAO implements UserDaoInterface {

    private final DataSource dataSource;

    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User getById(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT u.id, u.name, u.email, p.id as post_id, p.title as post_title, p.content as post_content " +
                            "FROM users u LEFT JOIN posts p ON u.id = p.user_id WHERE u.id = ?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            User user = null;
            List<Post> posts = new ArrayList<>();
            while (rs.next()) {
                if (user == null) {
                    user = new User();
                    user.setId(rs.getLong("id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                }
                if (rs.getLong("post_id") != 0) {
                    Post post = new Post();
                    post.setId(rs.getLong("post_id"));
                    post.setTitle(rs.getString("post_title"));
                    post.setContent(rs.getString("post_content"));
                    post.setUser(user); // Связываем пост с пользователем
                    posts.add(post);
                }
            }
            if (user != null) {
                user.setPosts(posts);
            }
            return user;
        }
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT u.id, u.name, u.email, p.id as post_id, p.title as post_title, p.content as post_content " +
                            "FROM users u LEFT JOIN posts p ON u.id = p.user_id");
            ResultSet rs = stmt.executeQuery();
            List<User> users = new ArrayList<>();
            User currentUser = null;
            while (rs.next()) {
                Long userId = rs.getLong("id");
                if (currentUser == null || !currentUser.getId().equals(userId)) {
                    currentUser = new User();
                    currentUser.setId(userId);
                    currentUser.setName(rs.getString("name"));
                    currentUser.setEmail(rs.getString("email"));
                    currentUser.setPosts(new ArrayList<>());
                    users.add(currentUser);
                }
                if (rs.getLong("post_id") != 0) {
                    Post post = new Post();
                    post.setId(rs.getLong("post_id"));
                    post.setTitle(rs.getString("post_title"));
                    post.setContent(rs.getString("post_content"));
                    post.setUser(currentUser);
                    currentUser.getPosts().add(post);
                }
            }
            return users;
        }
    }

    @Override
    public void save(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getLong(1));
            }
        }
    }

    @Override
    public void update(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET name = ?, email = ? WHERE id = ?");
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setLong(3, user.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM users WHERE id = ?");
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
}
