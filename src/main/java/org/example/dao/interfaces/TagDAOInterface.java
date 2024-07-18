package org.example.dao.interfaces;

import org.example.model.Post;
import org.example.model.Tag;

import java.sql.SQLException;
import java.util.List;

public interface TagDAOInterface {
    Tag getById(Long id) throws SQLException;

    List<Tag> getAllTags() throws SQLException;

    void save(Tag tag) throws SQLException;

    void update(Tag tag) throws SQLException;

    void delete(Long id) throws SQLException;

    List<Tag> getTagsByPostId(Long id) throws SQLException;

    void removeTagFromPost(Long tagId, Long postId) throws SQLException;

    void addTagToPost(Long tagId, Long postId) throws SQLException;

    List<Post> getPostsByTagId(Long id) throws SQLException;
}
