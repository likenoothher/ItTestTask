package com.azierets.restapijwt.restcontroller;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.AuthResponseDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.dto.RegisterResponseDto;
import com.azierets.restapijwt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody @Valid AuthRequestDto requestDto) {
        String userEmail = requestDto.getEmail();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userEmail,
                requestDto.getPassword()));
        String token = userService.generateToken(requestDto);
        return new AuthResponseDto(userEmail, token);
    }

    @PostMapping("/register")
    public RegisterResponseDto register(@RequestBody @Valid RegisterRequestDto requestDto) {
        userService.register(requestDto);
        return new RegisterResponseDto("registered");
    }
}
