package com.example.demo.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestEmailVerificationRequest(
    @Email @NotBlank String email) {}
