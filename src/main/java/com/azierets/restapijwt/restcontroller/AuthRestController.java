package com.azierets.restapijwt.restcontroller;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.CredentialsDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AuthRestController {

    private final UserService userService;

    @PostMapping("/login")
    public CredentialsDto login(@RequestBody @Valid AuthRequestDto requestDto) {
        return userService.authenticate(requestDto);
    }

    @PostMapping("/register")
    public CredentialsDto register(@RequestBody @Valid RegisterRequestDto requestDto) {
        return userService.register(requestDto);
    }
}
