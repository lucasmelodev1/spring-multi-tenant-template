package com.example.demo.user.dto;

import com.example.demo.user.AuthToken;
import com.example.demo.user.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @Email @NotBlank String email,
    @Size(min = AuthToken.TOKEN_SIZE, max = AuthToken.TOKEN_SIZE) @NotBlank String token,
    @Size(min = User.PASSWORD_MIN_SIZE, max = User.PASSWORD_MAX_SIZE) @NotBlank String password) {}
