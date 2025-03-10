package com.onyshkiv.finance.service.impl;

import com.onyshkiv.finance.exception.NotFoundException;
import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.model.entity.User;
import com.onyshkiv.finance.repository.UserRepository;
import com.onyshkiv.finance.security.SecurityContextHelper;
import com.onyshkiv.finance.service.TransactionService;
import com.onyshkiv.finance.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final SecurityContextHelper securityContextHelper;

    public UserServiceImpl(UserRepository userRepository, TransactionService transactionService, SecurityContextHelper securityContextHelper) {
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.securityContextHelper = securityContextHelper;
    }

    @Transactional
    public void changeUserBaseCurrency(Currency currencyToConvert) {
        UUID userId = securityContextHelper.getLoggedInUser().getId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found with id " + userId));
        user.setCurrency(currencyToConvert);
        transactionService.updateAmountAfterUserBaseCurrencyChange(currencyToConvert);
    }
}
