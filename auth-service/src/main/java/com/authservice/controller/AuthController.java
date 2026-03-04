package com.authservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authservice.Service.UserService;
import com.authservice.dto.RegisterUserRequest;
import com.authservice.dto.RegisterUserResponse;
import com.authservice.entity.User;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> registerUser(
            @RequestBody RegisterUserRequest request) {

        User savedUser = this.userService.saveUser(request);

        RegisterUserResponse response = new RegisterUserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail());

        return ResponseEntity.ok(response);
    }

}
