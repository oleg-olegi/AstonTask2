package org.example.mapper;

import org.example.dto.TagDTO;
import org.example.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TagMapper {
    TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

    TagDTO tagToTagDTO(Tag tag);

    Tag tagDTOToTag(TagDTO tagDTO);
}
