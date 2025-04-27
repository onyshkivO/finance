package com.onyshkiv.finance.controller;

import com.onyshkiv.finance.model.dto.request.ForgotPasswordRequest;
import com.onyshkiv.finance.model.dto.request.ResetPasswordRequest;
import com.onyshkiv.finance.model.dto.request.SignInRequest;
import com.onyshkiv.finance.model.dto.request.SignUpRequest;
import com.onyshkiv.finance.model.dto.response.AuthorizationResponse;
import com.onyshkiv.finance.service.AuthorizationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthorizationController {
    private final AuthorizationService authorizationService;

    @Autowired
    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthorizationResponse> register(@RequestBody @Valid SignUpRequest signUpRequest) {
        AuthorizationResponse response = authorizationService.singUp(signUpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthorizationResponse> authenticate(@RequestBody @Valid SignInRequest signInRequest) {
        AuthorizationResponse response = authorizationService.singIn(signInRequest);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authorizationService.sendResetLink(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok("Password reset link sent to your email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authorizationService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password has been reset successfully");
    }
}
