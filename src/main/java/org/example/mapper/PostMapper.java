package org.example.mapper;

import org.example.dto.PostDTO;
import org.example.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PostMapper {
    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    PostDTO postToPostDTO(Post post);

    Post postDTOToPost(PostDTO postDTO);
}
