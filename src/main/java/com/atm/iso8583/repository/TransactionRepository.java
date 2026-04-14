package com.atm.iso8583.repository;

import com.atm.iso8583.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionRef(String transactionRef);
    Page<Transaction> findByMti(String mti, Pageable pageable);
    Page<Transaction> findByResponseCode(String responseCode, Pageable pageable);
    Page<Transaction> findByCreatedAtBetween(Instant startTime, Instant endTime, Pageable pageable);
}
