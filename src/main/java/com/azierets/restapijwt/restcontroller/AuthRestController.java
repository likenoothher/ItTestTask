package com.azierets.restapijwt.restcontroller;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.exceptionhandler.exception.UserIsAlreadyRegisteredException;
import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.security.jwt.JwtService;
import com.azierets.restapijwt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequestDto requestDto) {
        try {
            String userEmail = requestDto.getEmail();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userEmail,
                    requestDto.getPassword()));
            User user = userService.findByEmail(userEmail);
            String token = jwtService.generateToken(user);
            return new ResponseEntity<>(new HashMap<String, String>() {{
                put("email", userEmail);
                put("token", token);
            }}, HttpStatus.OK);

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("invalid email or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDto requestDto) {
        String userEmail = requestDto.getEmail();
        User user = userService.findByEmail(userEmail);
        if (user != null) {
            throw new UserIsAlreadyRegisteredException("user with email " + userEmail + " is already registered");
        }

        User newUser = new User();
        newUser.setPassword(requestDto.getPassword());
        newUser.setEmail(userEmail);
        newUser.setFirstName(requestDto.getFirstName());
        newUser.setLastName(requestDto.getLastName());
        userService.register(newUser);
        return new ResponseEntity<>(new HashMap<String, String>() {{
            put("status", "registered");
        }}, HttpStatus.OK);
    }
}
