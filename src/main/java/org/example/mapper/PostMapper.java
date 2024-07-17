package org.example.mapper;

import org.example.dto.PostDTO;
import org.example.model.Post;
import org.example.model.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PostMapper {
    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    @Mapping(source = "user.id", target = "userId")
    PostDTO postToPostDTO(Post post);

    @Mapping(source = "userId", target = "user.id")
    Post postDTOToPost(PostDTO postDTO);

    @AfterMapping
    default void setUser(PostDTO postDTO, @MappingTarget Post post) {
        if (postDTO.getUserId() != null) {
            User user = new User();
            user.setId(postDTO.getUserId());
            post.setUser(user);
        }
    }
}
