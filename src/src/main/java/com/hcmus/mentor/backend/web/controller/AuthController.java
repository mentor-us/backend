package com.hcmus.mentor.backend.web.controller;

import an.awesome.pipelinr.Pipeline;
import com.hcmus.mentor.backend.exception.ValidationException;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.TokenModel;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.loginuser.LoginUserCommand;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.loginuser.LoginUserCommandResult;
import com.hcmus.mentor.backend.usecase.users.authenticateuser.refreshtoken.RefreshTokenCommand;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Authentication controller. */
@Tag(name = "auth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

  private final Pipeline pipeline;

  /**
   * Authenticate user by email and password.
   *
   * @param command Command hold email and password.
   * @return The response hold access token and expire time.
   * @throws ValidationException Username or password wrong.
   */
  @PostMapping("/login")
  @ApiResponses({
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "400", description = "Username or password wrong", content = @Content(schema = @Schema()))
  })

  public LoginUserCommandResult authenticate(@RequestBody LoginUserCommand command, BindingResult errors)
      throws ValidationException {
    if (errors.hasErrors()) {
      throw new ValidationException(errors);
    }

    return command.execute(pipeline);
  }

  /**
   * Get new token by refresh token.
   *
   * @param command Command hold user token.
   * @return The response hold access token and expire time.
   * @throws ValidationException Token is invalid.
   */
  @PostMapping("/refresh-token")
  @ApiResponses({
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "400", description = "Token is invalid", content = @Content(schema = @Schema()))
  })
  public TokenModel refreshToken(@RequestBody RefreshTokenCommand command, BindingResult errors)
      throws ValidationException {
    if (errors.hasErrors()) {
      throw new ValidationException(errors);
    }

    return command.execute(pipeline);
  }
}
