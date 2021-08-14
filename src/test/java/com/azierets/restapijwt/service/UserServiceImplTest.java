package com.azierets.restapijwt.service;

import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.model.UserRole;
import com.azierets.restapijwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail("email@test.com");
        user.setPassword("password");
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
    }

    @Test
    public void whenRegisterNotNullUser_thenReturnUserWithEncryptedPasswordAndRuleUser() {
        user = userService.register(user);

        String bCryptRegexPattern = "^\\$2[ayb]\\$.{56}$";
        Pattern pattern = Pattern.compile(bCryptRegexPattern);

        assertEquals(UserRole.USER, user.getRole());
        assertTrue(pattern.matcher(user.getPassword()).find());

        Mockito.verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    public void whenRegisterNotNullUserPasswordFieldIsNull_thenThrowIAException() {
        user.setPassword(null);
        assertThrows(IllegalArgumentException.class, () -> userService.register(user));

    }

    @Test
    public void whenRegisterNullUser_thenThrowNPException() {
        assertThrows(NullPointerException.class, () -> userService.register(null));
    }
}