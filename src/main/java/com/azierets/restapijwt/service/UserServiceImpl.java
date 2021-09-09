package com.azierets.restapijwt.service;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.CredentialsDto;
import com.azierets.restapijwt.dto.GreetingDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.dto.UserMapper;
import com.azierets.restapijwt.exceptionhandler.exception.UserIsAlreadyRegisteredException;
import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.repository.UserRepository;
import com.azierets.restapijwt.security.jwt.JwtService;
import com.azierets.restapijwt.security.jwt.JwtUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final AuthenticationManager authenticationManager;

    @Override
    public GreetingDto createGreetingMessage(String email) {
        User user = userRepository.findByEmail(email);
        return new GreetingDto("Hello, " + user.getFirstName());
    }

    @Override
    public CredentialsDto register(RegisterRequestDto requestDto) {
        String userEmail = requestDto.getEmail();
        if (userRepository.existsByEmail(userEmail)) {
            throw new UserIsAlreadyRegisteredException("пользователь с email " + userEmail + " уже зарегестрирован");
        }
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        requestDto.setRole("USER");
        User newUser = userMapper.registerRequestDtoToUser(requestDto);
        userRepository.save(newUser);
        return new CredentialsDto(newUser.getEmail(), jwtService.generateToken(newUser));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public CredentialsDto authenticate(AuthRequestDto requestDto) {
        String userEmail = requestDto.getEmail();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userEmail,
                requestDto.getPassword()));
        User user = userRepository.findByEmail(userEmail);
        String token = jwtService.generateToken(user);
        return new CredentialsDto(userEmail, token);
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
