package com.azierets.restapijwt.restcontroller;

import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.model.UserRole;
import com.azierets.restapijwt.security.jwt.JwtUser;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Arrays;

@TestConfiguration
public class AuthTestConfig {

    @Bean
    @Primary
    public UserDetailsService userTestDetailsService() {
        User basicUser = new User(1L, "test@email.com", "userFirstName",
                "userLastName", "password", UserRole.USER);

        return new InMemoryUserDetailsManager(Arrays.asList(
                new JwtUser(basicUser)));
    }
}
