package com.azierets.restapijwt.repository;

import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User registeredUser;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail("email@test.com");
        user.setPassword("$2a$12$TsnH5JA6BETSx.BDJMpoc.s2ya8D.Qplc2R.r73b3dYGw3h7DjmdW");
        user.setFirstName("testFirstName");
        user.setLastName("testLastName");
        user.setRole(UserRole.USER);

        registeredUser = new User();
        registeredUser.setId(1L);
        registeredUser.setEmail("email@test.com");
        registeredUser.setPassword("$2a$12$TsnH5JA6BETSx.BDJMpoc.s2ya8D.Qplc2R.r73b3dYGw3h7DjmdW");
        registeredUser.setFirstName("testFirstName");
        registeredUser.setLastName("testLastName");
        registeredUser.setRole(UserRole.USER);
    }

    @Test
    void whenFindByEmailAndUserExist_thenReturnUser() {
        userRepository.save(user);

        User found = userRepository.findByEmail("email@test.com");

        assertEquals(registeredUser.getId(), found.getId());
        assertEquals(registeredUser.getEmail(), found.getEmail());
        assertEquals(registeredUser.getPassword(), found.getPassword());
        assertEquals(registeredUser.getFirstName(), found.getFirstName());
        assertEquals(registeredUser.getLastName(), found.getLastName());
        assertEquals(registeredUser.getRole(), found.getRole());
    }

    @Test
    void whenFindByEmailAndUserDoesNotExist_thenReturnNull() {
        assertNull(userRepository.findByEmail("notExistedEmail@test.com"));
    }

    @Test
    void whenFindByEmailAndUserNull_thenReturnNull() {
        assertNull(userRepository.findByEmail(null));
    }
}
