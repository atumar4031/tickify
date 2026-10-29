package com.aasismgt.serviceImpl;

import com.aasismgt.entities.User;
import com.aasismgt.enums.GeneralStatus;
import com.aasismgt.enums.Roles;
import com.aasismgt.reponse.ApiResponse;
import com.aasismgt.reponse.UserResponse;
import com.aasismgt.repository.UserRepository;
import com.aasismgt.request.LoginRequest;
import com.aasismgt.request.UserRequest;
import com.aasismgt.security.UserSecurityContext;
import com.aasismgt.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserSecurityContext userSecurityContext;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           UserSecurityContext userSecurityContext
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userSecurityContext = userSecurityContext;
    }

    @Override
    public ResponseEntity<ApiResponse> saveUser(UserRequest userRequest) {
        if (isBlank(userRequest.email()) || isBlank(userRequest.password())) {
            return ResponseEntity.badRequest().body(new ApiResponse().error("Email and password are required"));
        }
        String email = userRequest.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse().error("Email already registered"));
        }

        Roles role = null;
        try {
            role = Roles.valueOf(userRequest.role().toUpperCase());
        }catch (IllegalArgumentException e) {
            log.error("Role exception {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse().error("Invalid role"));
        }

        User user = new User();
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        user.setRole(role);
        user.setStatus(GeneralStatus.ACTIVE);
        User saved = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse().success("User registered successfully", UserResponse.from(saved)));
    }

    @Override
    public ResponseEntity<ApiResponse> login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        if (isBlank(loginRequest.email()) || isBlank(loginRequest.password())) {
            return ResponseEntity.badRequest().body(new ApiResponse().error("Email and password are required"));
        }
        String email = loginRequest.email().trim().toLowerCase();

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, loginRequest.password()));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse().error("Account is not active"));
        } catch (AuthenticationException e) {
            log.warn("Failed login attempt for {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse().error("Invalid email or password"));
        }

        userSecurityContext.saveUserContext(authentication, request, response);

        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(new ApiResponse().success("Login successful", UserResponse.from(user)));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
