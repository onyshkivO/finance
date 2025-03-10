package com.onyshkiv.finance.controller;

import com.onyshkiv.finance.model.entity.Currency;
import com.onyshkiv.finance.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/changeCurrency/{currencyToConvert}")
    public ResponseEntity<Void> changeBaseCurrency(@PathVariable("currencyToConvert") Currency currencyToConvert) {
        userService.changeUserBaseCurrency(currencyToConvert);
        return ResponseEntity.ok().build();
    }
}
