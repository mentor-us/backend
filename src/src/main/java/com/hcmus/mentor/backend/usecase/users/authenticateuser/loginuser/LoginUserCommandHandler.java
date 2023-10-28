package com.hcmus.mentor.backend.usecase.users.authenticateuser.loginuser;

import an.awesome.pipelinr.Command;
import com.hcmus.mentor.backend.exception.DomainException;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.AuthenticationTokenService;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.TokenModelGenerator;
import com.hcmus.mentor.backend.web.infrastructure.security.UserPrincipal;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Handler for {@link LoginUserCommand}. */
@Component
@RequiredArgsConstructor
public class LoginUserCommandHandler
    implements Command.Handler<LoginUserCommand, LoginUserCommandResult> {

  private final AuthenticationManager authenticationManager;
  private final AuthenticationTokenService authenticationTokenService;

  @SneakyThrows
  @Override
  public LoginUserCommandResult handle(LoginUserCommand command) {
    UsernamePasswordAuthenticationToken authRequest =
        new UsernamePasswordAuthenticationToken(command.getEmail(), command.getPassword());

    try {
      var authentication = authenticationManager.authenticate(authRequest);
      var context = SecurityContextHolder.getContext();
      context.setAuthentication(authentication);

      UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

      var claims = new HashMap<String, Object>();
      claims.put("sub", principal.getId());
      claims.put("nameidentifier", principal.getEmail());
      claims.put("name", principal.getEmail());
      claims.put("emailaddress", principal.getEmail());

      var tokenModel = TokenModelGenerator.generate(authenticationTokenService, claims);

      return LoginUserCommandResult.builder()
          .accessToken(tokenModel.getAccessToken())
          .expiresIn(tokenModel.getExpiresIn())
          .build();
    } catch (Exception ex) {
      throw new DomainException(ex.getMessage(), ex);
    }
  }
}
