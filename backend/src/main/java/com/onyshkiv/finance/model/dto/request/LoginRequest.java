package com.onyshkiv.finance.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @Pattern(regexp = "^(?=.{4,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$", message = "Bad user login")
    @Size(min = 5, max = 50, message = "User login should be between 5 and 50 symbols")
    @NotBlank(message = "login should exists")
    private String login;

    @NotBlank(message = "Password should exists")
    @Pattern(regexp = "^[$\\/A-Za-z0-9_-]{6,60}$", message = "Bad password format")
    @Size(min = 5, max = 50, message = "User password should be between 5 and 50 symbols")
    private String password;
}

