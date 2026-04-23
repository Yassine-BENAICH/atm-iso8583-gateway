package com.atm.iso8583.controller;

import com.atm.iso8583.model.Transaction;
import com.atm.iso8583.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction History", description = "Query transaction logs and history")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{transactionRef}")
    @Operation(summary = "Get transaction by reference")
    public ResponseEntity<Transaction> getByReference(@PathVariable String transactionRef) {
        Transaction transaction = transactionService.getTransactionByRef(transactionRef);
        return transaction != null ? ResponseEntity.ok(transaction) : ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "Query transactions with filters")
    public ResponseEntity<Page<Transaction>> queryTransactions(
            @RequestParam(required = false) String mti,
            @RequestParam(required = false) String responseCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Transaction> result;
        if (mti != null) {
            result = transactionService.getTransactionsByMti(mti, pageRequest);
        } else if (responseCode != null) {
            result = transactionService.getTransactionsByResponseCode(responseCode, pageRequest);
        } else if (startDate != null && endDate != null) {
            result = transactionService.getTransactionsByDateRange(startDate, endDate, pageRequest);
        } else {
            result = transactionService.getTransactionsByDateRange(
                    Instant.now().minusSeconds(86400), Instant.now(), pageRequest);
        }

        return ResponseEntity.ok(result);
    }
}
