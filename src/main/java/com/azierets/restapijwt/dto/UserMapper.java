package com.azierets.restapijwt.dto;

import com.azierets.restapijwt.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User registerRequestDtoToUser(RegisterRequestDto dto);
}
