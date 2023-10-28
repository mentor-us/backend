package com.hcmus.mentor.backend.usecase.users.authenticateuser.refreshtoken;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.exception.DomainException;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.AuthenticateConstant;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.AuthenticationTokenService;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.TokenModel;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.TokenModelGenerator;
import com.hcmus.mentor.backend.usercase.common.repository.UserRepository;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

/** Handler for {@link RefreshTokenCommand}. */
@Component
@RequiredArgsConstructor
public class RefreshTokenCommandHandler
    implements Command.Handler<RefreshTokenCommand, TokenModel> {

  private final UserRepository userRepository;
  private final AuthenticationTokenService tokenService;

  @SneakyThrows
  @Override
  public TokenModel handle(RefreshTokenCommand command) {
    // Get user.
    String email = getTokenEmail(command.getToken());
    Optional<User> user = userRepository.findByEmail(email);
    if (user.isEmpty()) {
      throw new DomainException(String.format("User with email %s not found", email));
    }

    // Validate token.
    var tokenCreationDate = getTokenCreateDate(command.getToken());
    if (tokenCreationDate
        .plusSeconds(AuthenticateConstant.accessTokenExpirationTime.getSeconds())
        .isBefore(LocalDateTime.now())) {
      throw new DomainException("Token has been expired.");
    }

    var claims = new HashMap<String, Object>();
    claims.put("sub", user.get().getId());
    claims.put("nameidentifier", user.get().getEmail());
    claims.put("name", user.get().getEmail());
    claims.put("emailaddress", user.get().getEmail());

    return TokenModelGenerator.generate(tokenService, claims);
  }

  private LocalDateTime getTokenCreateDate(String token) throws DomainException {
    try {
      var claims = getTokenClaims(token);

      return Instant.ofEpochSecond((int) claims.get("iat"))
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();
    } catch (Exception ex) {
      throw new DomainException("User identifier claim cannot be found. Invalid token");
    }
  }

  private String getTokenEmail(String token) throws DomainException {
    try {
      Claims claims = getTokenClaims(token);

      return (String) claims.get("nameidentifier");
    } catch (Exception ex) {
      throw new DomainException("User identifier claim cannot be found. Invalid token");
    }
  }

  private Claims getTokenClaims(String token) throws DomainException {
    try {
      return tokenService.getTokenClaims(token);
    } catch (Exception ex) {
      throw new DomainException("Invalid token.");
    }
  }
}
