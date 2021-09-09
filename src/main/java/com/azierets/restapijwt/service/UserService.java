package com.azierets.restapijwt.service;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User register(RegisterRequestDto requestDto);

    User findByEmail(String email);

    String generateToken(AuthRequestDto requestDto);

}
