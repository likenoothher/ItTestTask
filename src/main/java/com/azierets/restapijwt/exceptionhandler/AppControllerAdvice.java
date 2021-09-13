package com.azierets.restapijwt.exceptionhandler;

import com.azierets.restapijwt.dto.CredentialsViolationDto;
import com.azierets.restapijwt.dto.ErrorDto;
import com.azierets.restapijwt.exceptionhandler.exception.UserIsAlreadyRegisteredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class AppControllerAdvice implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @ExceptionHandler(UserIsAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ErrorDto<CredentialsViolationDto> onUserIsAlreadyRegisteredException(UserIsAlreadyRegisteredException e) {
        return new ErrorDto<>(new CredentialsViolationDto("email", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ErrorDto<CredentialsViolationDto> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<CredentialsViolationDto> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> new CredentialsViolationDto(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());
        return new ErrorDto<>(errors);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    ErrorDto<CredentialsViolationDto> onAuthenticationException(AuthenticationException e) {
        return new ErrorDto<>(new CredentialsViolationDto("email or password",
                e.getMessage().toLowerCase()));
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        ErrorDto<CredentialsViolationDto> errorDto = new ErrorDto<>(new CredentialsViolationDto("token",
                e.getMessage().toLowerCase()));
        try (PrintWriter writer = response.getWriter()) {
            writer.print(mapper.writeValueAsString(errorDto));
        }
    }
}
