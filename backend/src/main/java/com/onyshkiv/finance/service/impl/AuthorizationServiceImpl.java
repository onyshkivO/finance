package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.DuplicationException;
import com.onyshkiv.finance.exception.InvalidTokenException;
import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.exception.UnauthorizedException;
import com.onyshkiv.finance.model.dto.request.SignInRequest;
import com.onyshkiv.finance.model.dto.request.SignUpRequest;
import com.onyshkiv.finance.model.dto.response.AuthorizationResponse;
import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.model.entity.PasswordResetToken;
import com.onyshkiv.finance.model.entity.User;
import com.onyshkiv.finance.repository.PasswordResetTokenRepository;
import com.onyshkiv.finance.repository.UserRepository;
import com.onyshkiv.finance.security.JwtUtil;
import com.onyshkiv.finance.service.AuthorizationService;
import com.onyshkiv.finance.service.CashboxService;
import com.onyshkiv.finance.util.ApplicationMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AuthorizationServiceImpl implements AuthorizationService {
//    private static final String HTML_CONTENT = """
//            <!DOCTYPE html>
//            <html>
//            <head>
//              <meta charset="UTF-8">
//              <title>Password Reset</title>
//            </head>
//            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
//              <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
//                <h2 style="color: #333;">Hello,</h2>
//                <p style="color: #555;">You recently requested to reset your password. Click the button below to reset it:</p>
//
//                <p style="text-align: center;">
//                  <a href="%s" style="background-color: #4CAF50; color: white; padding: 12px 20px; text-decoration: none; font-size: 16px; border-radius: 5px; display: inline-block;">Reset Password</a>
//                </p>
//
//                <p style="color: #555;">If the button above doesn't work, please navigate to:</p>
//                <p style="color: #555; text-align: center;">
//                  <a href="%s" style="color: #4CAF50;">%s</a>
//                </p>
//
//                <p style="color: #555;">If you didn't request a password reset, you can safely ignore this email.</p>
//                <p style="color: #555;">Thanks,<br>The Support Team</p>
//              </div>
//            </body>
//            </html>
//            """;
private static final String HTML_CONTENT = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <title>Password Reset</title>
              <style>
                /* Style the reset password button and fallback link */
                .reset-link-button {
                  background-color: #4CAF50;
                  color: white;
                  padding: 12px 20px;
                  text-decoration: none;
                  font-size: 16px;
                  border-radius: 5px;
                  display: inline-block;
                }
                .reset-link-text {
                  color: #4CAF50;
                  text-decoration: none;
                }
              </style>
            </head>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
              <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                <h2 style="color: #333;">Hello,</h2>
                <p style="color: #555;">You recently requested to reset your password. Click the button below to reset it:</p>

                <p style="text-align: center;">
                  <a href="%s" class="reset-link-button">Reset Password</a>
                </p>

                <p style="color: #555;">If the button above doesn't work, please copy and paste the following link into your browser:</p>
                <p style="color: #555; text-align: center;">
                  <a href="%s" class="reset-link-text">%s</a>
                </p>

                <p style="color: #555;">If you didn't request a password reset, you can safely ignore this email.</p>
                <p style="color: #555;">Thanks,<br>The Support Team</p>
              </div>
            </body>
            </html>
            """;




    private final AuthenticationProvider authenticationProvider;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final CashboxService cashboxService;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Autowired
    public AuthorizationServiceImpl(AuthenticationProvider authenticationProvider, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, UserRepository userRepository, ApplicationMapper applicationMapper, CashboxService cashboxService, PasswordResetTokenRepository tokenRepository, JavaMailSender mailSender) {
        this.authenticationProvider = authenticationProvider;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.applicationMapper = applicationMapper;
        this.cashboxService = cashboxService;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }

    @Override
    public AuthorizationResponse singIn(SignInRequest signInRequest) {
        String login = signInRequest.getLogin();
        try {
            authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(login, signInRequest.getPassword()));
            log.info("User signed in successfully: {}", login);
            String jwtToken = jwtUtil.generateToken(login);
            User authorizedUser = userRepository.findByLogin(login).get();
            return new AuthorizationResponse(jwtToken, authorizedUser.getLogin(), authorizedUser.getId(), authorizedUser.getCurrency());
        } catch (AuthenticationException ex) {
            log.error("Authentication failed for user: {}", login, ex);
            throw new UnauthorizedException("Invalid login or password", ex);
        }
    }

    @Override
    @Transactional
    public AuthorizationResponse singUp(SignUpRequest signUpRequest) {
        log.info("Attempting to sign up user with login: {} email: {}", signUpRequest.getLogin(), signUpRequest.getEmail());

        if (userRepository.findByLogin(signUpRequest.getLogin()).isPresent()) {
            log.warn("Signup failed: Login {} is already in use.", signUpRequest.getLogin());
            throw new DuplicationException("Login already in use");
        }

        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            log.warn("Signup failed: Email {} is already in use.", signUpRequest.getEmail());
            throw new DuplicationException("Email already in use");
        }

        User user = applicationMapper.signUpRequestToUser(signUpRequest);
        user.setId(UUID.randomUUID());
        user.setCurrency(Currency.UAH);//todo add currency to dto(optional)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        cashboxService.createCashCashbox(savedUser.getId());

        String jwt = jwtUtil.generateToken(user.getLogin());
        log.info("User signed up successfully: {}", savedUser.getLogin());
        return new AuthorizationResponse(jwt, savedUser.getLogin(), savedUser.getId(), savedUser.getCurrency());
    }

//    @Transactional
//    public void sendResetLink(String email) {
//        String token = UUID.randomUUID().toString();
//
//        // Check if user exists
//        userRepository.findByEmail(email)
//                .orElseThrow(() -> new NotFoundException("User not found for email: " + email));
//
//        PasswordResetToken resetToken = new PasswordResetToken();
//        resetToken.setEmail(email);
//        resetToken.setToken(token);
//        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
//
//        tokenRepository.save(resetToken);
//
//        String resetLink = frontendUrl + "/reset-password?token=" + token;
//
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo(email);
//            helper.setSubject("Password Reset Request");
//
//            // Use formatted HTML with reset link
//            String htmlContent = HTML_CONTENT.formatted(resetLink, resetLink, resetLink);
//            helper.setText(htmlContent, true); // true = send HTML
//
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            throw new RuntimeException("Failed to send password reset email", e);
//        }
//    }
@Transactional
public void sendResetLink(String email) {
    String token = UUID.randomUUID().toString();

    // Check if user exists
    userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found for email: " + email));

    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setEmail(email);
    resetToken.setToken(token);
    resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

    tokenRepository.save(resetToken);

    String resetLink = frontendUrl + "/reset-password?token=" + token;

    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Password Reset Request");

        // Use formatted HTML with reset link
        String htmlContent = HTML_CONTENT.formatted(resetLink, resetLink, resetLink);
        helper.setText(htmlContent, true); // true = send HTML

        mailSender.send(message);
    } catch (MessagingException e) {
        throw new RuntimeException("Failed to send password reset email", e);
    }
}


    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token expired");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found for email: " + resetToken.getEmail()));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}
