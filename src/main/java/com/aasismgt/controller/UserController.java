package com.aasismgt.controller;

import com.aasismgt.reponse.ApiResponse;
import com.aasismgt.request.LoginRequest;
import com.aasismgt.request.UserRequest;
import com.aasismgt.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody UserRequest userRequest) {
        return userService.saveUser(userRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest loginRequest,
                                             HttpServletRequest request, HttpServletResponse response) {
        return userService.login(loginRequest, request, response);
    }
}
