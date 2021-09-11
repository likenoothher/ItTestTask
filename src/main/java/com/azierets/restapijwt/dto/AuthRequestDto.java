package com.azierets.restapijwt.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class AuthRequestDto {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
