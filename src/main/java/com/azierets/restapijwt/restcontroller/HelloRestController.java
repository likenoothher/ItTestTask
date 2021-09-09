package com.azierets.restapijwt.restcontroller;

import com.azierets.restapijwt.model.User;
import com.azierets.restapijwt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/hello")
@RequiredArgsConstructor
public class HelloRestController {

    private final UserService userService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public String hello(Principal principal) {
        User user = userService.findByEmail(principal.getName());
        return "Hello, " + user.getFirstName();
    }
}
