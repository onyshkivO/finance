package com.onyshkiv.finance.controller;

import com.onyshkiv.finance.model.dto.MonobankAuthDto;
import com.onyshkiv.finance.model.dto.monobank.AccessResponse;
import com.onyshkiv.finance.model.dto.monobank.MonobankAccountDto;
import com.onyshkiv.finance.model.dto.monobank.MonobankCardResponse;
import com.onyshkiv.finance.model.dto.monobank.MonobankTransactionDto;
import com.onyshkiv.finance.service.MonobankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mono")
public class MonobankIntegrationController {
    private final MonobankService monobankService;

    public MonobankIntegrationController(MonobankService monobankService) {
        this.monobankService = monobankService;
    }

    @PostMapping("/request")
    public ResponseEntity<AccessResponse> requestAccessToMonobank() {
        MonobankAuthDto monobankAuth = monobankService.requestAccessAndStore();
        AccessResponse accessResponse = AccessResponse.builder()
                .isAccepted(monobankAuth.getIsConnected())
                .confirmUrl(monobankAuth.getAcceptUrl())
                .build();
        return ResponseEntity.ok(accessResponse);
    }

    @GetMapping("/account")
    public ResponseEntity<List<MonobankCardResponse>> getUserMonobankAccounts() {
        List<MonobankCardResponse> userMonobankCards = monobankService.getUserMonobankAccounts();
        return ResponseEntity.ok(userMonobankCards);
    }

    @PutMapping("/account/monitor/{accountId}")
    public ResponseEntity<Void> monitorMonobankAccount(@PathVariable("accountId") String accountId) {
        monobankService.monitorAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/account/unmonitor/{accountId}")
    public ResponseEntity<Void> unmonitorMonobankAccount(@PathVariable("accountId") String accountId) {
        monobankService.unmonitorAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/confirm")
    public void acceptAccess(@RequestHeader("X-Request-Id") String requestId) {
        monobankService.confirmAccess(requestId);
    }

    @PostMapping("/transaction")
    public ResponseEntity<Void> transactionWebhook(@RequestBody(required = false) MonobankTransactionDto monobankTransactionDto) {
        if (monobankTransactionDto != null) {
            monobankService.parseAndSaveTransactionWebhook(monobankTransactionDto.getData());
        }
        return ResponseEntity.ok().build();
    }
}
