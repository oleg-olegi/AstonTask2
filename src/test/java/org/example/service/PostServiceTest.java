package org.example.service;

import org.example.dao.PostDAO;
import org.example.dto.PostDTO;
import org.example.mapper.PostMapper;
import org.example.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PostServiceTest {

    @Mock
    private PostDAO postDAO;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetPostById() throws SQLException {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Title");
        post.setContent("Test Content");

        PostDTO postDTO = new PostDTO();
        postDTO.setId(1L);
        postDTO.setTitle("Test Title");
        postDTO.setContent("Test Content");

        when(postDAO.getById(1L)).thenReturn(post);
        when(postMapper.postToPostDTO(post)).thenReturn(postDTO);

        PostDTO result = postService.getPostById(1L);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getContent());
    }

    @Test
    public void testGetAllPosts() throws SQLException {
        Post post1 = new Post();
        post1.setId(1L);
        post1.setTitle("Test Title 1");
        post1.setContent("Test Content 1");

        Post post2 = new Post();
        post2.setId(2L);
        post2.setTitle("Test Title 2");
        post2.setContent("Test Content 2");

        List<Post> posts = Arrays.asList(post1, post2);

        PostDTO postDTO1 = new PostDTO();
        postDTO1.setId(1L);
        postDTO1.setTitle("Test Title 1");
        postDTO1.setContent("Test Content 1");

        PostDTO postDTO2 = new PostDTO();
        postDTO2.setId(2L);
        postDTO2.setTitle("Test Title 2");
        postDTO2.setContent("Test Content 2");

        when(postDAO.getAllPosts()).thenReturn(posts);
        when(postMapper.postToPostDTO(post1)).thenReturn(postDTO1);
        when(postMapper.postToPostDTO(post2)).thenReturn(postDTO2);

        List<PostDTO> result = postService.getAllPosts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Title 1", result.get(0).getTitle());
        assertEquals("Test Title 2", result.get(1).getTitle());
    }

    @Test
    public void testSavePost() throws SQLException {
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("Test Title");
        postDTO.setContent("Test Content");

        Post post = new Post();
        post.setTitle("Test Title");
        post.setContent("Test Content");

        when(postMapper.postDTOToPost(postDTO)).thenReturn(post);

        postService.savePost(postDTO);

        verify(postDAO, times(1)).save(post);
    }

    @Test
    public void testUpdatePost() throws SQLException {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(1L);
        postDTO.setTitle("Updated Title");
        postDTO.setContent("Updated Content");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Updated Title");
        post.setContent("Updated Content");

        when(postMapper.postDTOToPost(postDTO)).thenReturn(post);

        postService.updatePost(postDTO);

        verify(postDAO, times(1)).update(post);
    }

    @Test
    public void testDeletePost() throws SQLException {
        Long postId = 1L;

        postService.deletePost(postId);

        verify(postDAO, times(1)).delete(postId);
    }
}
