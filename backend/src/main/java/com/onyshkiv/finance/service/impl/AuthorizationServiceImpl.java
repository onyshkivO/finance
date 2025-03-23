package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.DuplicationException;
import com.onyshkiv.finance.exception.UnauthorizedException;
import com.onyshkiv.finance.model.dto.request.SignInRequest;
import com.onyshkiv.finance.model.dto.request.SignUpRequest;
import com.onyshkiv.finance.model.dto.response.AuthorizationResponse;
import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.model.entity.User;
import com.onyshkiv.finance.repository.UserRepository;
import com.onyshkiv.finance.security.JwtUtil;
import com.onyshkiv.finance.service.AuthorizationService;
import com.onyshkiv.finance.util.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AuthorizationServiceImpl implements AuthorizationService {

    private final AuthenticationProvider authenticationProvider;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;

    @Autowired
    public AuthorizationServiceImpl(AuthenticationProvider authenticationProvider, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, UserRepository userRepository, ApplicationMapper applicationMapper) {
        this.authenticationProvider = authenticationProvider;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.applicationMapper = applicationMapper;
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
        String jwt = jwtUtil.generateToken(user.getLogin());
        log.info("User signed up successfully: {}", savedUser.getLogin());
        return new AuthorizationResponse(jwt, savedUser.getLogin(), savedUser.getId(), savedUser.getCurrency());
    }
}
