package com.atm.iso8583.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_ref", columnList = "transaction_ref"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_mti", columnList = "mti")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_ref", length = 50)
    private String transactionRef;

    @Column(name = "mti", length = 4, nullable = false)
    private String mti;

    @Column(name = "request_fields", columnDefinition = "CLOB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode requestFields;

    @Column(name = "response_fields", columnDefinition = "CLOB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode responseFields;

    @Column(name = "response_code", length = 2)
    private String responseCode;

    @Column(name = "response_description", length = 255)
    private String responseDescription;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
