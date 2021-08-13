package com.azierets.restapijwt.service;

import com.azierets.restapijwt.model.User;

public interface UserService {
    User register(User user);

    User findByEmail(String email);
}
