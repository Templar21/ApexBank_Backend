package com.etz.Service;

import com.etz.DTO.Request.RegisterRequest;
import com.etz.Entity.User;

public interface UserService {

    String register(RegisterRequest registerRequest);

    void login(User user);

}
