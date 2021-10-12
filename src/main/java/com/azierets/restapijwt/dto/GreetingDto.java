package com.azierets.restapijwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class GreetingDto implements Serializable {
    private String message;
}
