package com.azierets.restapijwt.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;


@Data
public class AuthRequestDto {
    @Email
    private String email;
    @NotEmpty
    private String password;
}