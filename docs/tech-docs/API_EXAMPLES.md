# REST API Examples

## POST /api/iso8583/send

### Example 1: Balance Inquiry (MTI 0200)

**Request:**
```bash
curl -X POST http://localhost:8080/api/iso8583/send \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: REQ-001" \
  -d '{
    "mti": "0200",
    "transactionRef": "TXN-BAL-001",
    "fields": {
      "2": "4111111111111111",
      "3": "310000",
      "4": "000000000000",
      "7": "0312103045",
      "11": "123456",
      "12": "103045",
      "13": "0312",
      "41": "ATM00001",
      "49": "840"
    }
  }'
```

**Response (Success - 200 OK):**
```json
{
  "mti": "0210",
  "fields": {
    "2": "4111111111111111",
    "3": "310000",
    "4": "000000000000",
    "7": "0312103045",
    "11": "123456",
    "12": "103045",
    "13": "0312",
    "39": "00",
    "41": "ATM00001",
    "49": "840"
  },
  "responseCode": "00",
  "message": "Approved",
  "transactionRef": "TXN-BAL-001",
  "timestamp": "2024-03-12T10:30:45.123",
  "processingTimeMs": 150,
  "success": true
}
```

### Example 2: Cash Withdrawal (MTI 0200)

**Request:**
```bash
curl -X POST http://localhost:8080/api/iso8583/send \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: REQ-002" \
  -d '{
    "mti": "0200",
    "transactionRef": "TXN-WD-002",
    "fields": {
      "2": "5500000000000004",
      "3": "000000",
      "4": "000000010000",
      "7": "0312103050",
      "11": "123457",
      "12": "103050",
      "13": "0312",
      "37": "000000123457",
      "41": "ATM00001",
      "49": "840"
    }
  }'
```

**Response (Success - 200 OK):**
```json
{
  "mti": "0210",
  "fields": {
    "2": "5500000000000004",
    "3": "000000",
    "4": "000000010000",
    "7": "0312103050",
    "11": "123457",
    "12": "103050",
    "13": "0312",
    "37": "000000123457",
    "39": "00",
    "41": "ATM00001",
    "49": "840"
  },
  "responseCode": "00",
  "message": "Approved",
  "transactionRef": "TXN-WD-002",
  "timestamp": "2024-03-12T10:30:50.456",
  "processingTimeMs": 175,
  "success": true
}
```

### Example 3: Insufficient Funds (MTI 0200)

**Response (Payment Required - 402):**
```json
{
  "mti": "0210",
  "fields": {
    "2": "4111111111111111",
    "3": "000000",
    "4": "000000050000",
    "39": "51"
  },
  "responseCode": "51",
  "message": "Insufficient funds",
  "transactionRef": "TXN-WD-003",
  "timestamp": "2024-03-12T10:31:00.789",
  "processingTimeMs": 120,
  "success": false
}
```

### Example 4: Validation Error

**Request (Invalid MTI):**
```bash
curl -X POST http://localhost:8080/api/iso8583/send \
  -H "Content-Type: application/json" \
  -d '{
    "mti": "ABC",
    "fields": {
      "2": "4111111111111111"
    }
  }'
```

**Response (Bad Request - 400):**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    "mti: MTI must be a 4-digit numeric value"
  ],
  "path": "/api/iso8583/send",
  "timestamp": "2024-03-12T10:32:00.123"
}
```

### Example 5: Missing Required Fields

**Request:**
```bash
curl -X POST http://localhost:8080/api/iso8583/send \
  -H "Content-Type: application/json" \
  -d '{
    "mti": "0200"
  }'
```

**Response (Bad Request - 400):**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    "fields: Fields map is required"
  ],
  "path": "/api/iso8583/send",
  "timestamp": "2024-03-12T10:33:00.456"
}
```

## GET /api/iso8583/health

**Request:**
```bash
curl -X GET http://localhost:8080/api/iso8583/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "ISO 8583 Gateway",
  "timestamp": 1710241800000
}
```

## GET /api/iso8583/status

**Request:**
```bash
curl -X GET http://localhost:8080/api/iso8583/status
```

**Response (200 OK):**
```json
{
  "status": "ACTIVE",
  "version": "1.0.0",
  "uptime": 1710241800000
}
```

## ISO 8583 Field Reference

| Field | Name | Description | Example |
|-------|------|-------------|---------|
| 0 | MTI | Message Type Indicator | 0200, 0210 |
| 2 | PAN | Primary Account Number | 4111111111111111 |
| 3 | Processing Code | Transaction type | 000000 (purchase), 310000 (balance) |
| 4 | Amount | Transaction amount | 000000010000 ($100.00) |
| 7 | Transmission Date/Time | MMDDhhmmss | 0312103045 |
| 11 | STAN | System Trace Audit Number | 123456 |
| 12 | Local Time | hhmmss | 103045 |
| 13 | Local Date | MMDD | 0312 |
| 37 | RRN | Retrieval Reference Number | 000000123456 |
| 39 | Response Code | Transaction result | 00 (approved) |
| 41 | Terminal ID | Card acceptor terminal | ATM00001 |
| 49 | Currency Code | ISO 4217 code | 840 (USD) |

## Response Codes

| Code | Message | HTTP Status |
|------|---------|-------------|
| 00 | Approved | 200 OK |
| 01 | Refer to card issuer | 400 Bad Request |
| 05 | Do not honor | 403 Forbidden |
| 12 | Invalid transaction | 400 Bad Request |
| 14 | Invalid card number | 403 Forbidden |
| 41 | Lost card | 403 Forbidden |
| 43 | Stolen card | 403 Forbidden |
| 51 | Insufficient funds | 402 Payment Required |
| 54 | Expired card | 400 Bad Request |
| 55 | Incorrect PIN | 400 Bad Request |
| 61 | Exceeds withdrawal limit | 402 Payment Required |
| 91 | Issuer inoperative | 503 Service Unavailable |
| 96 | System malfunction | 503 Service Unavailable |

## Request Headers

| Header | Required | Description | Example |
|--------|----------|-------------|---------|
| Content-Type | Yes | Request content type | application/json |
| X-Request-ID | No | Unique request identifier (auto-generated if not provided) | REQ-12345 |

## Response Headers

| Header | Description | Example |
|--------|-------------|---------|
| Content-Type | Response content type | application/json |
| X-Request-ID | Request identifier (echoed or generated) | REQ-12345 |
