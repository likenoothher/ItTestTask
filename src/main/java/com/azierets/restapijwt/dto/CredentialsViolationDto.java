package com.azierets.restapijwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CredentialsViolationDto {
    private String fieldName;
    private String message;
}
