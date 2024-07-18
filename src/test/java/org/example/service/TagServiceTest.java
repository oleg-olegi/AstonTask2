package org.example.service;

import org.example.dao.TagDAO;
import org.example.dto.TagDTO;
import org.example.mapper.TagMapper;
import org.example.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class TagServiceTest {
    @Mock
    private TagDAO tagDAO;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetTagById() throws SQLException {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");

        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(1L);
        tagDTO.setName("Test Tag");

        when(tagDAO.getById(1L)).thenReturn(tag);
        when(tagMapper.tagToTagDTO(tag)).thenReturn(tagDTO);

        TagDTO result = tagService.getTagById(1L);

        assertNotNull(result);
        assertEquals("Test Tag", result.getName());
    }

    @Test
    public void testGetAllTags() throws SQLException {
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Test Tag 1");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Test Tag 2");

        List<Tag> tags = Arrays.asList(tag1, tag2);

        TagDTO tagDTO1 = new TagDTO();
        tagDTO1.setId(1L);
        tagDTO1.setName("Test Tag 1");
        TagDTO tagDTO2 = new TagDTO();
        tagDTO2.setId(2L);
        tagDTO2.setName("Test Tag 2");

        when(tagDAO.getAllTags()).thenReturn(tags);
        when(tagMapper.tagToTagDTO(tag1)).thenReturn(tagDTO1);
        when(tagMapper.tagToTagDTO(tag2)).thenReturn(tagDTO2);

        List<TagDTO> result = tagService.getAllTags();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Tag 1", result.get(0).getName());
        assertEquals("Test Tag 2", result.get(1).getName());
    }

    @Test
    public void testSaveTag() throws SQLException {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName("Test Tag");

        Tag tag = new Tag();
        tag.setName("Test Tag");
        when(tagMapper.tagDTOToTag(tagDTO)).thenReturn(tag);

        tagService.save(tagDTO);

        verify(tagDAO, times(1)).save(tag);
    }

    @Test
    public void testUpdateTag() throws SQLException {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(1L);
        tagDTO.setName("Test Tag");

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Test Tag");

        when(tagMapper.tagDTOToTag(tagDTO)).thenReturn(tag);

        tagService.update(tagDTO);

        verify(tagDAO, times(1)).update(tag);
    }

    @Test
    public void testDeleteTag() throws SQLException {
        Long tagId = 1L;

        tagService.delete(tagId);

        verify(tagDAO, times(1)).delete(tagId);
    }
    @Test
    public void testGetTagsByPostId() throws SQLException {
        Long postId = 1L;
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Tag1");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Tag2");

        List<Tag> tags = Arrays.asList(tag1, tag2);

        TagDTO tagDTO1 = new TagDTO();
        tagDTO1.setId(1L);
        tagDTO1.setName("Tag1");

        TagDTO tagDTO2 = new TagDTO();
        tagDTO2.setId(2L);
        tagDTO2.setName("Tag2");

        when(tagDAO.getTagsByPostId(postId)).thenReturn(tags);
        when(tagMapper.tagToTagDTO(tag1)).thenReturn(tagDTO1);
        when(tagMapper.tagToTagDTO(tag2)).thenReturn(tagDTO2);

        List<TagDTO> result = tagService.getTagsByPostId(postId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tag1", result.get(0).getName());
        assertEquals("Tag2", result.get(1).getName());
    }
    @Test
    public void testAddTagToPost() throws SQLException {
        Long postId = 1L;
        Long tagId = 1L;

        tagService.addTagToPost(postId, tagId);

        verify(tagDAO, times(1)).addTagToPost(postId, tagId);
    }
    @Test
    public void testRemoveTagFromPost() throws SQLException {
        Long postId = 1L;
        Long tagId = 1L;

        tagService.removeTagFromPost(postId, tagId);

        verify(tagDAO, times(1)).removeTagFromPost(postId, tagId);
    }
}
