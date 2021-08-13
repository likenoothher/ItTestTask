package com.azierets.restapijwt.config;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Data
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
    private final String message;
    private final HttpStatus httpStatus;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        response.setStatus(httpStatus.value());
        try (PrintWriter writer = response.getWriter()) {
            writer.print(message);
        }
    }
}
