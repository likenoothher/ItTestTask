package com.azierets.restapijwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CredentialsDto {
    private String email;
    private String token;
}
