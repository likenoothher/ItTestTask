package com.azierets.restapijwt.service;

import com.azierets.restapijwt.dto.AuthRequestDto;
import com.azierets.restapijwt.dto.RegisterRequestDto;
import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.model.UserRole;
import com.azierets.restapijwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User user;
    private RegisterRequestDto registerRequestDto;
    private AuthRequestDto authRequestDto;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail("email@test.com");
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
    public void whenRegisterUserPassedDtoNotNull_thenReturnUserWithEncryptedPasswordAndRoleUser() {
        when(userRepository.findByEmail(registerRequestDto.getEmail())).thenReturn(null);
        user = userService.register(registerRequestDto);

        String bCryptRegexPattern = "^\\$2[ayb]\\$.{56}$";
        Pattern pattern = Pattern.compile(bCryptRegexPattern);

        assertEquals(UserRole.USER, user.getRole());
        assertTrue(pattern.matcher(user.getPassword()).find());
        Mockito.verify(userRepository, Mockito.times(1)).save(user);
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
    public void whenGenerateTokenRequestDtoNotNullUserExist_thenReturnToken() {
        when(userRepository.findByEmail(registerRequestDto.getEmail())).thenReturn(user);

        String token = userService.generateToken(authRequestDto);
        String tokenRegex = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        assertTrue(token.matches(tokenRegex));
    }

    @Test
    public void whenGenerateTokenRequestDtoNull_thenThrowNPException() {
        assertThrows(NullPointerException.class, () -> userService.generateToken(null));
    }

    @Test
    public void whenGenerateTokenRequestDtoNotNullUserDoesNotExist_thenThrowNPException() {
        when(userRepository.findByEmail(authRequestDto.getEmail())).thenReturn(null);
        assertThrows(NullPointerException.class, () -> userService.generateToken(authRequestDto));
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
