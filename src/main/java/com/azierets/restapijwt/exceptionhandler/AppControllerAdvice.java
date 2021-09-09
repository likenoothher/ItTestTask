package com.azierets.restapijwt.exceptionhandler;

import com.azierets.restapijwt.dto.ValidationViolationDto;
import com.azierets.restapijwt.exceptionhandler.exception.UserIsAlreadyRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class AppControllerAdvice implements AuthenticationEntryPoint {

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

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    HashMap<String, List<ValidationViolationDto>> onAuthenticationException(AuthenticationException e) {
        return new HashMap<String, List<ValidationViolationDto>>() {{
            put("error", Collections.singletonList(new ValidationViolationDto("email or password",
                    e.getMessage().toLowerCase())));
        }};
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        try (PrintWriter writer = response.getWriter()) {
            writer.print("Authentication is required");
        }
    }
}
