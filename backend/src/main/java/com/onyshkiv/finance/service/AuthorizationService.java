package com.onyshkiv.finance.service;

import com.onyshkiv.finance.model.dto.request.SignInRequest;
import com.onyshkiv.finance.model.dto.request.SignUpRequest;
import com.onyshkiv.finance.model.dto.response.AuthorizationResponse;

public interface AuthorizationService {
    AuthorizationResponse singIn(SignInRequest signInRequest);

    AuthorizationResponse singUp(SignUpRequest signUpRequest);

    void sendResetLink(String email);

    void resetPassword(String token, String newPassword);
}
