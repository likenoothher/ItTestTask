package com.azierets.restapijwt.dto;

import com.azierets.restapijwt.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(target = "role", constant = "USER")
    User registerRequestDtoToUser(RegisterRequestDto dto);
}
