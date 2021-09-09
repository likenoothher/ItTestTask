package com.azierets.restapijwt.service;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.dto.UserMapper;
import com.azierets.restapijwt.exceptionhandler.exception.UserIsAlreadyRegisteredException;
import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.repository.UserRepository;
import com.azierets.restapijwt.security.jwt.JwtService;
import com.azierets.restapijwt.security.jwt.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public User register(RegisterRequestDto requestDto) {
        String userEmail = requestDto.getEmail();
        if (userRepository.findByEmail(userEmail) != null) {
            throw new UserIsAlreadyRegisteredException("user with email " + userEmail + " is already registered");
        }
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        User newUser = userMapper.registerRequestDtoToUser(requestDto);
        userRepository.save(newUser);
        return newUser;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public String generateToken(AuthRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail());
        return jwtService.generateToken(user);
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new UsernameNotFoundException("User with email " + userEmail + " not found");
        }
        return new JwtUser(user);
    }
}
