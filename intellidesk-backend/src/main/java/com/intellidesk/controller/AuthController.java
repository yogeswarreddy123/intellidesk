package com.intellidesk.controller;

import com.intellidesk.dto.ApiResponse;
import com.intellidesk.dto.AuthRequest;
import com.intellidesk.dto.AuthResponse;
import com.intellidesk.dto.RegisterRequest;
import com.intellidesk.dto.UserDto;
import com.intellidesk.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = userService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest request) {
        UserDto userDto = userService.register(request);
        return new ResponseEntity<>(ApiResponse.success("Registration successful", userDto), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        UserDto currentUser = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("User profile fetched", currentUser));
    }
}
