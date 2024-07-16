package org.example.service;

import org.example.dao.PostDAO;
import org.example.dto.PostDTO;
import org.example.mapper.PostMapper;
import org.example.model.Post;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PostService {
    private final PostDAO postDAO;

    public PostService(PostDAO postDAO) {
        this.postDAO = postDAO;
    }

    public PostDTO getPostById(Long id) throws SQLException {
        Post post = postDAO.getById(id);
        return PostMapper.INSTANCE.postToPostDTO(post);
    }

    public List<PostDTO> getAllPosts() throws SQLException {
        List<Post> posts = postDAO.getAllPosts();
        return posts.stream().map(PostMapper.INSTANCE::postToPostDTO).collect(Collectors.toList());
    }

    public void savePost(PostDTO postDTO) throws SQLException {
        Post post = PostMapper.INSTANCE.postDTOToPost(postDTO);
        postDAO.save(post);
    }

    public void updatePost(PostDTO postDTO) throws SQLException {
        Post post = PostMapper.INSTANCE.postDTOToPost(postDTO);
        postDAO.update(post);
    }

    public void deletePost(Long id) throws SQLException {
        postDAO.delete(id);
    }
}
