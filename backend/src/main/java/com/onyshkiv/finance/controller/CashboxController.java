package com.onyshkiv.finance.controller;

import com.onyshkiv.finance.model.dto.request.CashboxRequest;
import com.onyshkiv.finance.model.dto.request.TransferRequest;
import com.onyshkiv.finance.model.dto.response.CashboxResponse;
import com.onyshkiv.finance.model.dto.response.ExtendedCashboxResponse;
import com.onyshkiv.finance.model.dto.response.TransferResponse;
import com.onyshkiv.finance.service.CashboxService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/cashbox")
public class CashboxController {
    private final CashboxService cashboxService;

    public CashboxController(CashboxService cashboxService) {
        this.cashboxService = cashboxService;
    }

    @PostMapping
    public ResponseEntity<CashboxResponse> createCashbox(@RequestBody @Valid CashboxRequest cashboxRequest) {
        CashboxResponse cashboxResponse = cashboxService.createCashbox(cashboxRequest);
        return ResponseEntity.ok(cashboxResponse);
    }

    @GetMapping
    public ResponseEntity<List<CashboxResponse>> getCashboxes() {
        return ResponseEntity.ok(cashboxService.findCashboxes());
    }

    @PutMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@RequestBody @Valid TransferRequest transferRequest) {
        TransferResponse transferResponse = cashboxService.transfer(transferRequest);
        return ResponseEntity.ok(transferResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CashboxResponse> updateCashbox(@PathVariable("id") UUID id, @RequestBody @Valid CashboxRequest cashboxRequest) {
        CashboxResponse cashboxResponse = cashboxService.update(id, cashboxRequest);
        return ResponseEntity.ok(cashboxResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExtendedCashboxResponse> getCashBoxById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(cashboxService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCashbox(@PathVariable("id") UUID id) {
        cashboxService.markDeleted(id);
        return ResponseEntity.noContent().build();
    }
}
