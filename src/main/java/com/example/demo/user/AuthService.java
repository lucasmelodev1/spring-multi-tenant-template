package com.example.demo.user;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.user.dto.CreateUserRequest;
import com.example.demo.user.dto.RequestEmailVerificationRequest;
import com.example.demo.user.dto.RequestResetPasswordRequest;
import com.example.demo.user.dto.ResetPasswordRequest;
import com.example.demo.user.dto.VerifyEmailRequest;
import com.example.demo.user.dto.VerifyPasswordResetRequestRequest;
import com.example.demo.utils.EnvUtils;
import com.example.demo.utils.TokenUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AuthService {
  @Autowired
  private EnvUtils envUtils;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AuthTokenRepository authTokenRepository;

  private String generateAndSaveToken(String email, AuthTokenType type) {
    var token = TokenUtils.generateNumeric(AuthToken.TOKEN_SIZE);
    var tokenHash = TokenUtils.hash(token);

    AuthToken req = new AuthToken(email, tokenHash, type);
    authTokenRepository.save(req);

    return token;
  }

  private AuthToken consumeToken(String token, AuthTokenType type) {
    var notFoundException = new EntityNotFoundException("Token not found");

    var foundToken = authTokenRepository
        .findByTokenHashAndType(TokenUtils.hash(token), type)
        .orElseThrow(() -> notFoundException);

    if (!foundToken.isValid()) {
      throw notFoundException;
    }

    foundToken.consume();
    authTokenRepository.save(foundToken);

    return foundToken;
  }

  private void logTokenInDev(String label, String token) {
    // TODO: send email
    if (envUtils.isDevEnvironment()) {
      System.out.println(label + ": " + token);
    }
  }

  public void requestEmailVerification(RequestEmailVerificationRequest dto) {
    var type = AuthTokenType.EMAIL_VERIFICATION;
    authTokenRepository.revokeActiveByEmailAndType(dto.email(), type, Instant.now());

    var token = generateAndSaveToken(dto.email(), type);
    logTokenInDev("Email Verification Token", token);
  }

  public String verifyEmail(VerifyEmailRequest dto) throws EntityNotFoundException {
    consumeToken(dto.token(), AuthTokenType.EMAIL_VERIFICATION);
    return generateAndSaveToken(dto.email(), AuthTokenType.USER_CREATION);
  }

  public void createUser(CreateUserRequest dto) throws EntityNotFoundException {
    consumeToken(dto.token(), AuthTokenType.USER_CREATION);

    var passwordHash = passwordEncoder.encode(dto.password());
    var user = new User(dto.name(), dto.email(), passwordHash);
    userRepository.save(user);
  }

  public void requestResetPassword(RequestResetPasswordRequest dto) {
    var user = userRepository.findByEmail(dto.email());

    if (user.isPresent()) {
      var type = AuthTokenType.PASSWORD_RESET_REQUEST;
      authTokenRepository.revokeActiveByEmailAndType(dto.email(), type, Instant.now());

      var token = generateAndSaveToken(dto.email(), type);
      logTokenInDev("Reset Password Token", token);
    }
  }

  public String verifyPasswordResetToken(VerifyPasswordResetRequestRequest dto) throws EntityNotFoundException {
    consumeToken(dto.token(), AuthTokenType.PASSWORD_RESET_REQUEST);
    return generateAndSaveToken(dto.email(), AuthTokenType.PASSWORD_RESET);
  }

  public void resetPassword(ResetPasswordRequest dto) throws EntityNotFoundException {
    consumeToken(dto.token(), AuthTokenType.PASSWORD_RESET);

    var passwordHash = passwordEncoder.encode(dto.password());
    var user = userRepository
        .findByEmail(dto.email())
        .orElseThrow(() -> new EntityNotFoundException("Error resetting password"));

    user.setPassword(passwordHash);
    userRepository.save(user);
  }
}
