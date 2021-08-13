package com.azierets.restapijwt.security;

import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.security.jwt.JwtUser;
import com.azierets.restapijwt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtDetailsService implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        User user = userService.findByEmail(userEmail);
        if (user == null) {
            throw new UsernameNotFoundException("User with email " + userEmail + " not found");
        }
        return new JwtUser(user);
    }
}
