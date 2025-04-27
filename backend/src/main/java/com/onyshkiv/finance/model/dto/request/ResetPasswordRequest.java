package com.onyshkiv.finance.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResetPasswordRequest {
    @NotBlank(message = "Token should exists")
    private String token;
    @NotBlank(message = "Password should exists")
    @Pattern(regexp = "^[$\\/A-Za-z0-9_-]{6,60}$", message = "Bad password format")
    @Size(min = 5, max = 50, message = "User password should be between 5 and 50 symbols")
    private String newPassword;
}

