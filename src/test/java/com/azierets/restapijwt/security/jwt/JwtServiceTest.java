package com.azierets.restapijwt.security.jwt;

import com.azierets.restapijwt.exceptionhandler.exception.JwtAuthException;
import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.model.UserRole;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtServiceTest {

    private JwtService service = new JwtService();

    private User user;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(service, "expirationTimeInMs", 25000L, Long.class);
        ReflectionTestUtils.setField(service, "tokenSecret", "secret", String.class);

        user = new User();
        user.setEmail("test@email.com");
        user.setPassword("password");
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setRole(UserRole.USER);
    }

    @Test
    public void whenGenerateToken_thenReturnToken() {
        String tokenRegex = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        String token = service.generateToken(user);

        assertNotNull(token);
        assertTrue(Pattern.compile(tokenRegex).matcher(token).matches());
    }

    @Test
    public void whenGetUserEmailTokenValid_thenReturnUserEmail() {
        String token = service.generateToken(user);

        String result = service.getUserEmail(token);
        String expected = "test@email.com";

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void whenGetUserEmailTokenInvalid_thenThrowMalformedJwtException() {
        String invalidToken = "invalidToken";

        assertThrows(MalformedJwtException.class, () -> service.getUserEmail(invalidToken));
    }

    @Test
    public void whenExtractRepresentedValidTokenFromRequest_thenReturnToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer_1234");

        String token = service.extractTokenFromRequest(request);
        String expected = "1234";

        assertEquals(expected, token);
    }

    @Test
    public void whenExtractRepresentedInvalidTokenFromRequest_thenReturnNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bea_1234");

        assertNull(service.extractTokenFromRequest(request));
    }

    @Test
    public void whenTokenValidAndNotExpired_thenReturnTrue() {
        String token = service.generateToken(user);
        assertTrue(service.isTokenValid(token));
    }

    @Test
    public void whenTokenValidAndExpired_thenThrowJwtAuthException() {
        ReflectionTestUtils.setField(service, "expirationTimeInMs", -25000L, Long.class);
        String token = service.generateToken(user);
        assertThrows(JwtAuthException.class, () -> service.isTokenValid(token));
    }

    @Test
    public void whenTokenInvalid_thenThrowJwtAuthException() {
        String invalidToken = " ";
        assertThrows(JwtAuthException.class, () -> service.isTokenValid(invalidToken));
    }
}
