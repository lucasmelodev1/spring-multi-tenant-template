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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User login, registration and credential recovery")
public class AuthController {
  @Autowired
  private AuthService authService;

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Authenticate a user and return a cookie with the session id")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest dto) {
    return ResponseEntity.ok(authService.login(dto));
  }

  @PostMapping("/register-email-verification")
  @Operation(summary = "Request email verification", description = "The first step in the user registration flow. The user requests a verification token to be sent to their email.")
  public ResponseEntity<String> registerRequestEmailVerification(@Valid @RequestBody RequestEmailVerificationRequest dto) {
    authService.requestEmailVerification(dto);
    return ResponseEntity.ok("Verification token sent via email");
  }

  @PostMapping("/register-verify-email")
  @Operation(summary = "Verify email", description = "The second step in the user registration flow. The user verifies their email with the sent token and receives a second one to be used to create the account.")
  public ResponseEntity<VerifyEmailResponse> registerVerifyEmail(@Valid @RequestBody VerifyEmailRequest dto) {
    var response = authService.verifyEmail(dto);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/register-create-user")
  @Operation(summary = "Create user", description = "The final step in the user registration flow. The user consumes their verified email token to create and account with the set password and name.")
  public ResponseEntity<?> registerCreateUser(@Valid @RequestBody CreateUserRequest dto) {
    authService.createUser(dto);
    return ResponseEntity.ok("User created successfully");
  }
}
