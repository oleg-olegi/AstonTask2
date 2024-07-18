package org.example.service;

import org.example.dao.TagDAO;
import org.example.dto.PostDTO;
import org.example.dto.TagDTO;
import org.example.mapper.PostMapper;
import org.example.mapper.TagMapper;
import org.example.model.Post;
import org.example.model.Tag;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class TagService {
    private final TagDAO tagDAO;

    public TagService(TagDAO tagDAO) {
        this.tagDAO = tagDAO;
    }

    public List<TagDTO> getAllTags() throws SQLException {
        List<Tag> tags = tagDAO.getAllTags();
        return tags.stream().map(TagMapper.INSTANCE::tagToTagDTO).collect(Collectors.toList());
    }

    public TagDTO getTagById(Long id) throws SQLException {
        Tag tag = tagDAO.getById(id);
        return TagMapper.INSTANCE.tagToTagDTO(tag);
    }

    public void save(TagDTO tagDTO) throws SQLException {
        Tag tag = TagMapper.INSTANCE.tagDTOToTag(tagDTO);
        tagDAO.save(tag);
    }

    public void update(TagDTO tagDTO) throws SQLException {
        Tag tag = TagMapper.INSTANCE.tagDTOToTag(tagDTO);
        tagDAO.update(tag);
    }

    public void delete(Long id) throws SQLException {
        tagDAO.delete(id);
    }

    public void addTagToPost(Long tagId, Long postId) throws SQLException {
        tagDAO.addTagToPost(tagId, postId);
    }

    public void removeTagFromPost(Long tagId, Long postId) throws SQLException {
        tagDAO.removeTagFromPost(tagId, postId);
    }

    public List<TagDTO> getTagsByPostId(Long postId) throws SQLException {
        List<Tag> tags = tagDAO.getTagsByPostId(postId);
        return tags.stream().map(TagMapper.INSTANCE::tagToTagDTO).collect(Collectors.toList());
    }
}

