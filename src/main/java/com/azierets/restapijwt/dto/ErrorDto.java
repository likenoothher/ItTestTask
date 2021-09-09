package com.azierets.restapijwt.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class ErrorDto<T> {
    private List<T> errors;

    public ErrorDto(List<T> errors) {
        this.errors = new ArrayList<>(errors);
    }

    public ErrorDto(T error) {
        this.errors = Collections.singletonList(error);
    }
}
