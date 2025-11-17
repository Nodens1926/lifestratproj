package com.lifestratproj.app.controller;

import com.lifestratproj.app.dto.RegisterRequest;
import com.lifestratproj.app.dto.LoginRequest;
import com.lifestratproj.app.dto.AuthResponse;
import com.lifestratproj.app.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse response = authService.register(registerRequest);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                new AuthResponse(e.getMessage(), null, null, null, null),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new AuthResponse("Registration failed", null, null, null, null),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = authService.login(loginRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                new AuthResponse(e.getMessage(), null, null, null, null),
                HttpStatus.UNAUTHORIZED
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new AuthResponse("Login failed", null, null, null, null),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}