package com.onyshkiv.finance.controller;

import com.onyshkiv.finance.model.dto.MonobankAuthDto;
import com.onyshkiv.finance.model.dto.monobank.MonobankTransactionDto;
import com.onyshkiv.finance.service.MonobankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mono")
public class MonobankIntegrationController {
    private final MonobankService monobankService;

    public MonobankIntegrationController(MonobankService monobankService) {
        this.monobankService = monobankService;
    }

    @PostMapping("/request")
    public ResponseEntity<String> requestAccessToMonobank() {
        MonobankAuthDto monobankAuth = monobankService.requestAccessAndStore();
        return ResponseEntity.ok(monobankAuth.getAcceptUrl());
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
