package org.example.mapper;

import org.example.dto.UserDTO;
import org.example.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "posts", target = "posts")
    UserDTO userToUserDTO(User user);

    @Mapping(source = "posts", target = "posts")
    User userDTOToUser(UserDTO userDTO);
}
