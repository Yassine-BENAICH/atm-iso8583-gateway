package com.atm.iso8583.service;

import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import com.atm.iso8583.model.Transaction;
import com.atm.iso8583.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Transaction saveTransaction(Iso8583Request request, Iso8583Response response) {
        Transaction transaction = Transaction.builder()
                .transactionRef(request.getTransactionRef())
                .mti(request.getMti())
                .requestFields(objectMapper.valueToTree(request.getFields()))
                .responseFields(response.getAdditionalFields() != null
                        ? objectMapper.valueToTree(response.getAdditionalFields())
                        : null)
                .responseCode(response.getResponseCode())
                .responseDescription(response.getResponseDescription())
                .processingTimeMs(response.getProcessingTimeMs())
                .status(response.getStatus())
                .errorMessage(response.getErrorMessage())
                .build();

        return transactionRepository.save(Objects.requireNonNull(transaction));
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByMti(String mti, Pageable pageable) {
        return transactionRepository.findByMti(mti, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByResponseCode(String responseCode, Pageable pageable) {
        return transactionRepository.findByResponseCode(responseCode, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByDateRange(Instant startTime, Instant endTime, Pageable pageable) {
        return transactionRepository.findByCreatedAtBetween(startTime, endTime, pageable);
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionByRef(String transactionRef) {
        return transactionRepository.findByTransactionRef(transactionRef).orElse(null);
    }
}
