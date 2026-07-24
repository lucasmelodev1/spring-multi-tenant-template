package com.example.demo.user;

import java.time.Instant;

import javax.security.auth.login.CredentialNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.user.dto.CreateUserRequest;
import com.example.demo.user.dto.LoginRequest;
import com.example.demo.user.dto.LoginResponse;
import com.example.demo.user.dto.RequestEmailVerificationRequest;
import com.example.demo.user.dto.RequestResetPasswordRequest;
import com.example.demo.user.dto.ResetPasswordRequest;
import com.example.demo.user.dto.VerifyEmailRequest;
import com.example.demo.user.dto.VerifyEmailResponse;
import com.example.demo.user.dto.VerifyPasswordResetRequestRequest;
import com.example.demo.utils.EnvUtils;
import com.example.demo.utils.TokenUtils;

import io.micrometer.observation.annotation.Observed;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  @Autowired
  private EnvUtils envUtils;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AuthTokenRepository authTokenRepository;

  @Autowired
  private AuthenticationManager authenticationManager;

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
      log.warn("Token is invalid");
      throw notFoundException;
    }

    foundToken.consume();
    authTokenRepository.save(foundToken);
    log.info(type.toString() + " token consumed");

    return foundToken;
  }

  private void logTokenInDev(String label, String token) {
    // TODO: send email
    if (envUtils.isDevEnvironment()) {
      System.out.println(label + ": " + token);
    }
    log.info(label + " sent to email");
  }

  @Observed(name = "auth.request-email-verification")
  public void requestEmailVerification(RequestEmailVerificationRequest dto) {
    var type = AuthTokenType.EMAIL_VERIFICATION;
    authTokenRepository.revokeActiveByEmailAndType(dto.email(), type, Instant.now());

    var token = generateAndSaveToken(dto.email(), type);
    logTokenInDev("Email Verification Token", token);
  }

  @Observed(name = "auth.verify-email")
  public VerifyEmailResponse verifyEmail(VerifyEmailRequest dto) throws EntityNotFoundException {
    var oldToken = consumeToken(dto.token(), AuthTokenType.EMAIL_VERIFICATION);
    var newToken = generateAndSaveToken(oldToken.getEmail(), AuthTokenType.USER_CREATION);
    return new VerifyEmailResponse(newToken);
  }

  @Observed(name = "auth.create-user")
  public void createUser(CreateUserRequest dto) throws EntityNotFoundException {
    var token = consumeToken(dto.token(), AuthTokenType.USER_CREATION);

    var passwordHash = passwordEncoder.encode(dto.password());
    var user = new User(dto.name(), token.getEmail(), passwordHash);
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
    var token = consumeToken(dto.token(), AuthTokenType.PASSWORD_RESET_REQUEST);
    return generateAndSaveToken(token.getEmail(), AuthTokenType.PASSWORD_RESET);
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

  @Observed(name = "auth.login")
  public LoginResponse login(LoginRequest dto) throws BadCredentialsException {
    Authentication auth = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));
    User user = (User) auth.getPrincipal();
    return new LoginResponse(user.getId(), user.getEmail(), user.getRole());
  }
}
