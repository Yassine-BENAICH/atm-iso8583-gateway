# Transaction Logging API - Quick Reference

## Overview
All ISO 8583 transactions are automatically logged to the database. Use these endpoints to query transaction history.

## Endpoints

### 1. Get Transaction by Reference
```http
GET /api/transactions/{transactionRef}
```

**Example:**
```bash
curl http://localhost:8080/api/transactions/ATM-20260414-0001
```

**Response:** Single transaction object or 404 if not found

---

### 2. Query Transactions
```http
GET /api/transactions?mti={mti}&responseCode={code}&startDate={iso8601}&endDate={iso8601}&page={page}&size={size}
```

**Query Parameters:**
- `mti` (optional) - Filter by Message Type Indicator (e.g., 0200, 0210)
- `responseCode` (optional) - Filter by response code (e.g., 00, 05)
- `startDate` (optional) - ISO 8601 timestamp (e.g., 2026-04-14T00:00:00Z)
- `endDate` (optional) - ISO 8601 timestamp
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 20) - Page size

**Examples:**

```bash
# Get all purchase transactions (0200)
curl "http://localhost:8080/api/transactions?mti=0200"

# Get all approved transactions
curl "http://localhost:8080/api/transactions?responseCode=00"

# Get transactions in date range
curl "http://localhost:8080/api/transactions?startDate=2026-04-14T00:00:00Z&endDate=2026-04-14T23:59:59Z"

# Get last 24 hours (default)
curl "http://localhost:8080/api/transactions"

# Pagination - get page 2 with 50 items
curl "http://localhost:8080/api/transactions?mti=0200&page=1&size=50"
```

**Response Format:**
```json
{
  "content": [
    {
      "id": 1,
      "transactionRef": "ATM-20260414-0001",
      "mti": "0200",
      "requestFields": {
        "2": "1234567890123456",
        "3": "000000",
        "4": "000000010000",
        "11": "123456",
        "41": "12345678",
        "49": "840"
      },
      "responseFields": {
        "0": "0210",
        "2": "1234567890123456",
        "39": "00"
      },
      "responseCode": "00",
      "responseDescription": "Approved",
      "processingTimeMs": 142,
      "status": "SUCCESS",
      "errorMessage": null,
      "createdAt": "2026-04-14T14:30:00.123Z",
      "updatedAt": "2026-04-14T14:30:00.123Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

---

## Common Use Cases

### 1. Find a specific transaction
```bash
curl http://localhost:8080/api/transactions/ATM-20260414-0001
```

### 2. Get all failed transactions
```bash
curl "http://localhost:8080/api/transactions?responseCode=05"
```

### 3. Get today's transactions
```bash
START=$(date -u +"%Y-%m-%dT00:00:00Z")
END=$(date -u +"%Y-%m-%dT23:59:59Z")
curl "http://localhost:8080/api/transactions?startDate=$START&endDate=$END"
```

### 4. Get all reversal transactions
```bash
curl "http://localhost:8080/api/transactions?mti=0400"
```

### 5. Get network management messages
```bash
curl "http://localhost:8080/api/transactions?mti=0800"
```

---

## Response Codes Reference

Common ISO 8583 response codes:
- `00` - Approved
- `05` - Do not honor
- `14` - Invalid card number
- `51` - Insufficient funds
- `54` - Expired card
- `55` - Incorrect PIN
- `91` - Issuer unavailable

---

## Transaction Status

- `SUCCESS` - Transaction completed successfully
- `ERROR` - Transaction failed (check errorMessage field)

---

## Swagger UI

Access the interactive API documentation at:
```
http://localhost:8080/api/swagger-ui.html
```

---

## PowerShell Examples

```powershell
# Get transaction by reference
Invoke-RestMethod -Uri "http://localhost:8080/api/transactions/ATM-20260414-0001"

# Get approved transactions
Invoke-RestMethod -Uri "http://localhost:8080/api/transactions?responseCode=00"

# Get transactions with pagination
Invoke-RestMethod -Uri "http://localhost:8080/api/transactions?mti=0200&page=0&size=50"
```

---

## Notes

- All timestamps are in UTC (ISO 8601 format)
- Results are sorted by `createdAt` in descending order (newest first)
- Default page size is 20 items
- If no filters are provided, returns last 24 hours of transactions
- Request and response fields are stored as JSON for easy querying
