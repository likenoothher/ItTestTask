package com.azierets.restapijwt.dto;

import lombok.Data;

@Data
public class ValidationViolationDto {
    private final String fieldName;
    private final String message;
}
