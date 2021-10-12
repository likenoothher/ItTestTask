package com.azierets.restapijwt.service;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.CredentialsDto;
import com.azierets.restapijwt.dto.GreetingDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.dto.UserMapper;
import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.model.UserRole;
import com.azierets.restapijwt.rabbit.RabbitMessageSender;
import com.azierets.restapijwt.rabbit.messagedto.LoggingMessageDto;
import com.azierets.restapijwt.repository.UserRepository;
import com.azierets.restapijwt.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RabbitMessageSender rabbitMessageSender;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private RegisterRequestDto registerRequestDto;
    private AuthRequestDto authRequestDto;

    @BeforeEach
    public void setUp() {

        user = new User();
        user.setEmail("test@email.com");
        user.setPassword("password");
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setRole(UserRole.USER);

        registerRequestDto = new RegisterRequestDto();
        registerRequestDto.setEmail("test@email.com");
        registerRequestDto.setPassword("password");
        registerRequestDto.setFirstName("firstName");
        registerRequestDto.setLastName("lastName");

        authRequestDto = new AuthRequestDto();
        authRequestDto.setEmail("test@email.com");
        authRequestDto.setPassword("password");
    }

    @Test
    public void whenCreateGreetingMessageEmailExists_thenReturnGreetingDto() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        GreetingDto dto = userService.createGreetingMessage(user.getEmail());

        assertEquals("Hello, testFirstName", dto.getMessage());

        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    public void whenCreateGreetingMessageEmailDoesNotExist_thenThrowNPException() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);

        assertThrows(NullPointerException.class, () -> userService.createGreetingMessage(user.getEmail()));

        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    public void whenCreateGreetingMessageEmailNull_thenThrowNPException() {
        when(userRepository.findByEmail(null)).thenReturn(null);
        assertThrows(NullPointerException.class, () -> userService.createGreetingMessage(null));
    }

    @Test
    public void whenRegisterUserPassedDtoNotNull_thenReturnUserWithEncryptedPasswordAndRoleUser() {
        when(userRepository.existsByEmail(registerRequestDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequestDto.getPassword())).thenReturn("encodedPassword");
        when(userMapper.registerRequestDtoToUser(registerRequestDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(rabbitMessageSender).sendMessage(any());
        when(jwtService.generateToken(user)).thenReturn("generatedToken");

        String generatedToken = "generatedToken";
        CredentialsDto dto = userService.register(registerRequestDto);

        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(generatedToken, dto.getToken());

        verify(userRepository).existsByEmail(registerRequestDto.getEmail());
        verify(passwordEncoder).encode("password");
        verify(userMapper).registerRequestDtoToUser(registerRequestDto);
        verify(userRepository).save(user);
        verify(rabbitMessageSender).sendMessage(any(LoggingMessageDto.class));
        verify(jwtService).generateToken(user);
    }

    @Test
    public void whenRegisterUserPassedDtoPasswordFieldIsNull_thenThrowNPException() {
        registerRequestDto.setPassword(null);
        when(userRepository.existsByEmail(registerRequestDto.getEmail())).thenReturn(false);

        assertThrows(NullPointerException.class, () -> userService.register(registerRequestDto));

        verify(userRepository).existsByEmail(registerRequestDto.getEmail());
    }

    @Test
    public void whenRegisterUserPassedDtoNull_thenThrowNPException() {
        assertThrows(NullPointerException.class, () -> userService.register(null));
    }

    @Test
    public void whenAuthenticateRequestDtoNotNullUserExist_thenReturnToken() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(any());
        when(userRepository.findByEmail(registerRequestDto.getEmail())).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("generatedToken");
        doNothing().when(rabbitMessageSender).sendMessage(any());

        String generatedToken = "generatedToken";
        CredentialsDto dto = userService.authenticate(authRequestDto);

        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(generatedToken, dto.getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(registerRequestDto.getEmail());
        verify(rabbitMessageSender).sendMessage(any(LoggingMessageDto.class));
        verify(jwtService).generateToken(user);
    }

    @Test
    public void whenAuthenticateRequestDtoNull_thenThrowNPException() {
        assertThrows(NullPointerException.class, () -> userService.authenticate(null));
    }

    @Test
    public void whenAuthenticateRequestDtoNotNullUserDoesNotExist_thenThrowNPException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(any());
        when(userRepository.findByEmail(authRequestDto.getEmail())).thenReturn(null);
        doNothing().when(rabbitMessageSender).sendMessage(any());
        when(jwtService.generateToken(null)).thenThrow(NullPointerException.class);

        assertThrows(NullPointerException.class, () -> userService.authenticate(authRequestDto));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(registerRequestDto.getEmail());
        verify(rabbitMessageSender).sendMessage(any(LoggingMessageDto.class));
        verify(jwtService).generateToken(null);
    }

    @Test
    public void whenAuthenticateRequestDtoNotNullBadCredentials_thenThrowAuthException() {
        BadCredentialsException badCredentialsException = new BadCredentialsException("invalid email or password");
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(),
                authRequestDto.getPassword()))).thenThrow(badCredentialsException);

        assertThrows(AuthenticationException.class, () -> userService.authenticate(authRequestDto));

        verify(userRepository, times(0)).findByEmail(authRequestDto.getEmail());
        verify(rabbitMessageSender, times(0)).sendMessage(any());
        verify(jwtService, times(0)).generateToken(null);
    }

    @Test
    public void whenLoadByUserNameUserEmailExist_thenReturnJwtUserOfCurrentUser() {
        when(userRepository.findByEmail(authRequestDto.getEmail())).thenReturn(user);
        UserDetails jwtUser = userService.loadUserByUsername(authRequestDto.getEmail());

        assertEquals(user.getEmail(), jwtUser.getUsername());
        assertEquals(user.getPassword(), jwtUser.getPassword());
        assertTrue(jwtUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(jwtUser.isAccountNonExpired());
        assertTrue(jwtUser.isAccountNonLocked());
        assertTrue(jwtUser.isCredentialsNonExpired());
        assertTrue(jwtUser.isEnabled());

        verify(userRepository).findByEmail(authRequestDto.getEmail());
    }

    @Test
    public void whenLoadByUserNameUserEmailDoesNotExist_thenThrowUsernameNotFoundException() {
        when(userRepository.findByEmail(authRequestDto.getEmail())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(authRequestDto.getEmail()));

        verify(userRepository).findByEmail(authRequestDto.getEmail());
    }

    @Test
    public void whenLoadByUserNameUserEmailNull_thenThrowNPException() {
        when(userRepository.findByEmail(null)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(null));

        verify(userRepository).findByEmail(null);
    }
}
