package com.example.demo.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.user.dto.CreateUserRequest;
import com.example.demo.user.dto.LoginRequest;
import com.example.demo.user.dto.LoginResponse;
import com.example.demo.user.dto.RequestEmailVerificationRequest;
import com.example.demo.user.dto.VerifyEmailRequest;
import com.example.demo.user.dto.VerifyEmailResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  @Autowired
  private AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest dto) {
    return ResponseEntity.ok(authService.login(dto));
  }

  @PostMapping("/register-email-verification")
  public ResponseEntity<String> registerEmailVefirication(@Valid @RequestBody RequestEmailVerificationRequest dto) {
    authService.requestEmailVerification(dto);
    return ResponseEntity.ok("Verification token sent via email");
  }

  @PostMapping("/register-verify-email")
  public ResponseEntity<VerifyEmailResponse> registerVerifyEmail(@Valid @RequestBody VerifyEmailRequest dto) {
    var response = authService.verifyEmail(dto);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/register-create-user")
  public ResponseEntity<?> registerCreateUser(@Valid @RequestBody CreateUserRequest dto) {
    authService.createUser(dto);
    return ResponseEntity.ok("User created successfully");
  }
}
