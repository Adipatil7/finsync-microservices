package com.authservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authservice.Service.UserService;
import com.authservice.dto.*;
import com.authservice.entity.User;
import com.authservice.util.JwtUtil;
import com.authservice.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        
        if (authenticate.isAuthenticated()) {
            User user = userRepository.findByEmail(request.getEmail()).get();
            String token = jwtUtil.generateToken(user.getId().toString(), user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token, user.getId().toString(), user.getEmail(), user.getName()));
        } else {
            throw new RuntimeException("Invalid access");
        }
    }

}
