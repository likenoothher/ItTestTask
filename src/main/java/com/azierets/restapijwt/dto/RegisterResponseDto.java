package com.azierets.restapijwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class RegisterResponseDto {
    @NotBlank
    private String status;
}
