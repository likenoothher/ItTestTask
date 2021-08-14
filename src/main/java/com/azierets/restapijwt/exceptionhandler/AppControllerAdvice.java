package com.azierets.restapijwt.exceptionhandler;

import com.azierets.restapijwt.dto.ValidationViolationDto;
import com.azierets.restapijwt.exceptionhandler.exception.JwtAuthException;
import com.azierets.restapijwt.exceptionhandler.exception.UserIsAlreadyRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class AppControllerAdvice {

    @ExceptionHandler(UserIsAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    HashMap<String, List<ValidationViolationDto>> onUserIsAlreadyRegisteredException(UserIsAlreadyRegisteredException e) {
        return new HashMap<String, List<ValidationViolationDto>>() {{
            put("error", Collections.singletonList(new ValidationViolationDto("email", e.getMessage())));
        }};
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    HashMap<String, List<ValidationViolationDto>> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ValidationViolationDto> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> new ValidationViolationDto(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        return new HashMap<String, List<ValidationViolationDto>>() {{
            put("errors", errors);
        }};
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    HashMap<String, List<ValidationViolationDto>> onBadCredentialsException(BadCredentialsException e) {
        return new HashMap<String, List<ValidationViolationDto>>() {{
            put("error", Collections.singletonList(new ValidationViolationDto("email or password", e.getMessage())));
        }};
    }

    @ExceptionHandler(JwtAuthException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    HashMap<String, List<ValidationViolationDto>> onJwtAuthException(JwtAuthException e) {
        return new HashMap<String, List<ValidationViolationDto>>() {{
            put("error", Collections.singletonList(new ValidationViolationDto("token", e.getMessage())));
        }};
    }

}
