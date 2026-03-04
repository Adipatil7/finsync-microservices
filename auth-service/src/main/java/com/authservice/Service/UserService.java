package com.authservice.Service;

import com.authservice.dto.RegisterUserRequest;
import com.authservice.entity.User;

public interface UserService {
    
    User saveUser(RegisterUserRequest request);
    
}
