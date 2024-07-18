package org.example.dao.interfaces;

import org.example.model.Post;

import java.sql.SQLException;
import java.util.List;

public interface PostDaoInterface {
    Post getById(Long id) throws SQLException;

    List<Post> getAllPosts() throws SQLException;

    void save(Post post) throws SQLException;

    void update(Post post) throws SQLException;

    void delete(Long id) throws SQLException;
}

