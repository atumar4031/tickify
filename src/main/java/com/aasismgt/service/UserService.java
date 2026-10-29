package com.aasismgt.service;

import com.aasismgt.reponse.ApiResponse;
import com.aasismgt.request.LoginRequest;
import com.aasismgt.request.UserRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<ApiResponse> saveUser(UserRequest userRequest);
    ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response);
}
