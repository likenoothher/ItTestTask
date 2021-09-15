package com.azierets.restapijwt.service;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.CredentialsDto;
import com.azierets.restapijwt.dto.GreetingDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.model.UserRole;
import com.azierets.restapijwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

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
    }

    @Test
    public void whenCreateGreetingMessageEmailDoesNotExist_thenThrowNPException() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(null);
        assertThrows(NullPointerException.class, () -> userService.createGreetingMessage(null));
    }

    @Test
    public void whenCreateGreetingMessageEmailNull_thenThrowNPException() {
        when(userRepository.findByEmail(null)).thenReturn(null);
        assertThrows(NullPointerException.class, () -> userService.createGreetingMessage(null));
    }

    @Test
    public void whenRegisterUserPassedDtoNotNull_thenReturnUserWithEncryptedPasswordAndRoleUser() {
        when(userRepository.existsByEmail(registerRequestDto.getEmail())).thenReturn(false);
        CredentialsDto dto = userService.register(registerRequestDto);

        String tokenRegex = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        assertEquals(user.getEmail(), dto.getEmail());
        assertTrue(dto.getToken().matches(tokenRegex));
    }

    @Test
    public void whenRegisterUserPassedDtoPasswordFieldIsNull_thenThrowIAException() {
        registerRequestDto.setPassword(null);
        assertThrows(IllegalArgumentException.class, () -> userService.register(registerRequestDto));
    }

    @Test
    public void whenRegisterUserPassedDtoNull_thenThrowNPException() {
        assertThrows(NullPointerException.class, () -> userService.register(null));
    }

    @Test
    public void whenAuthenticateRequestDtoNotNullUserExist_thenReturnToken() {
        when(userRepository.findByEmail(registerRequestDto.getEmail())).thenReturn(user);
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(),
                authRequestDto.getPassword()))).thenReturn(null);

        CredentialsDto dto = userService.authenticate(authRequestDto);
        String tokenRegex = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        assertEquals(user.getEmail(), dto.getEmail());
        assertTrue(dto.getToken().matches(tokenRegex));
    }

    @Test
    public void whenAuthenticateRequestDtoNull_thenThrowNPException() {
        assertThrows(NullPointerException.class, () -> userService.authenticate(null));
    }

    @Test
    public void whenAuthenticateRequestDtoNotNullUserDoesNotExist_thenThrowNPException() {
        when(userRepository.findByEmail(authRequestDto.getEmail())).thenReturn(null);
        assertThrows(NullPointerException.class, () -> userService.authenticate(authRequestDto));
    }

    @Test
    public void whenAuthenticateRequestDtoNotNullBadCredentials_thenThrowAuthException() {
        BadCredentialsException badCredentialsException = new BadCredentialsException("invalid email or password");
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(),
                authRequestDto.getPassword()))).thenThrow(badCredentialsException);
        assertThrows(AuthenticationException.class, () -> userService.authenticate(authRequestDto));
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
    }

    @Test
    public void whenLoadByUserNameUserEmailDoesNotExist_thenThrowUsernameNotFoundException() {
        when(userRepository.findByEmail(authRequestDto.getEmail())).thenReturn(null);
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(authRequestDto.getEmail()));
    }

    @Test
    public void whenLoadByUserNameUserEmailNull_thenThrowNPException() {
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(null));
    }
}
