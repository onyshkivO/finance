package com.onyshkiv.finance.security;

import com.google.common.base.Preconditions;
import com.onyshkiv.finance.exception.UnauthorizedException;
import io.micrometer.common.util.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class SecurityContextHelper {
    public CustomUserDetails getLoggedInUser() {
        return getUserPrincipal();
    }

    public void validateLoggedInUser() {
        getUserPrincipal();
    }

    private static CustomUserDetails getUserPrincipal() {
        CustomUserDetails user = (CustomUserDetails) Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .orElseThrow(() -> new UnauthorizedException("User not found in context"));

        Preconditions.checkState(StringUtils.isNotBlank(user.getLogin()), "login is required in context");
        Preconditions.checkState(!Objects.isNull(user.getId()), "id is required in context");
        return user;
    }
}

