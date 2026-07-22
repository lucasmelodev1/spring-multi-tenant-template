package com.example.demo.user.dto;

import com.example.demo.user.AuthToken;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyPasswordResetRequestRequest(
    @Email @NotBlank String email,

    @Size(min = AuthToken.TOKEN_SIZE, max = AuthToken.TOKEN_SIZE, message = "Token with invalid size")
    @NotBlank
    String token) {}
