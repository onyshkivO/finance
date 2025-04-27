package com.onyshkiv.finance.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordRequest {
    @Size(min = 5, max = 255, message = "User email should be between 5 and 50 symbols")
    @NotBlank(message = "Email should exists")
    @Email(message = "Email should be in email format")
    private String email;
}
