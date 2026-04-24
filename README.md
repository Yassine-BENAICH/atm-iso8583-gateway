# ATM ISO 8583 Gateway

Spring Boot REST API gateway that accepts JSON/XML, packs it into ISO 8583 (via jPOS), sends it over TCP to a switch, then returns the ISO 8583 response as JSON/XML. Features transaction logging, monitoring, and a PowerCard direct debit integration.

## Tech Stack

- Java 17
- Spring Boot 3.4.1
- jPOS 2.1.9
- Spring Data JPA
- H2 Database (in-memory) / PostgreSQL (production)
- Flyway (database migrations)
- Spring Boot Actuator
- Spring Boot Validation
- Maven
- OpenAPI / Swagger UI (springdoc)
- Lombok
- Jackson (JSON/XML support)

## Prerequisites

- JDK 17+
- Maven 3.8+

## Quick Start

### 1. Start the Mock Switch (port 9000)

Option A: Run the `com.atm.iso8583.simulator.Iso8583MockSwitch` main class from your IDE.

Option B: Run via Maven (uses the Maven Exec plugin):

```bash
mvn -DskipTests test-compile exec:java -Dexec.mainClass=com.atm.iso8583.simulator.Iso8583MockSwitch
```

The mock switch auto-responds with approval (`DE39=00`) and flips the MTI from `x200` to `x210` (e.g. `0200` -> `0210`).

### 2. Start the Gateway (port 8080)

```bash
mvn spring-boot:run
```

### 3. Access the Application

- **Dashboard**: `http://localhost:8080/dashboard/`
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api/docs`
- **Actuator Health**: `http://localhost:8080/api/actuator/health`

## API

### ISO 8583 Gateway Endpoints

#### Health Check

`GET /api/iso8583/health`

Response:

```json
{
  "status": "UP",
  "service": "ISO 8583 Gateway",
  "timestamp": 1234567890
}
```

#### Gateway Status

`GET /api/iso8583/status`

Returns detailed gateway status and statistics.

#### Gateway Configuration

`GET /api/iso8583/config`

Returns current ISO 8583 network configuration (host, port, timeouts, etc.).

#### Send ISO 8583 Message

`POST /api/iso8583/send`

General endpoint for sending any ISO 8583 message.

Request body (JSON or XML):

- `mti`: ISO 8583 MTI (e.g. `0200`)
- `fields`: object where keys are ISO field numbers as strings and values are field values
- `transactionRef`: optional transaction reference

Example:

```bash
curl -X POST "http://localhost:8080/api/iso8583/send" \
  -H "Content-Type: application/json" \
  -d '{"mti":"0200","fields":{"2":"1234567890123456","3":"000000","4":"000000010000","11":"123456","41":"12345678","49":"840"}}'
```

Response:

```json
{
  "mti": "0210",
  "fields": {
    "0": "0210",
    "2": "1234567890123456",
    "3": "000000",
    "4": "000000010000",
    "11": "123456",
    "39": "00",
    "41": "12345678",
    "49": "840"
  },
  "responseCode": "00",
  "message": "Success",
  "processingTimeMs": 45
}
```

#### Shortcut Endpoints

These endpoints are shortcuts for common MTI types:

- `POST /api/iso8583/authorize` - Authorization request (MTI 0100)
- `POST /api/iso8583/financial` - Financial request (MTI 0200)
- `POST /api/iso8583/presentment` - Presentment request (MTI 1200)
- `POST /api/iso8583/reversal` - Reversal request (MTI 0400)

#### Network Echo Test

`POST /api/iso8583/echo`

Sends an 0800/301 echo test to check switch connectivity.

### PowerCard Direct Debit

#### Direct Debit Transfer

`POST /api/powercard/direct-debit`

Accepts XML, maps it to ISO 8583 MTI 1200, forwards it to PowerCARD, and returns XML.

Request (XML):

```xml
<DirectDebitTransferRequest>
  <transactionRef>TXN123456</transactionRef>
  <amount>100.00</amount>
  <currency>840</currency>
  <!-- additional PowerCard-specific fields -->
</DirectDebitTransferRequest>
```

Response (XML):

```xml
<DirectDebitTransferResponse>
  <transactionRef>TXN123456</transactionRef>
  <responseCode>00</responseCode>
  <status>APPROVED</status>
</DirectDebitTransferResponse>
```

### Monitoring Endpoints

#### Traffic Metrics

`GET /api/monitoring/metrics`

Returns aggregated gateway metrics (total requests, success rate, average response time, etc.).

#### Recent Events

`GET /api/monitoring/events?limit=50`

Returns recent transaction events. Limit parameter (1-500) controls the number of events returned.

#### Recent Errors

`GET /api/monitoring/errors?limit=50`

Returns only failed operations. Limit parameter (1-500) controls the number of errors returned.

### Transaction History

#### Get Transaction by Reference

`GET /api/transactions/{transactionRef}`

Retrieves a specific transaction by its reference.

#### Query Transactions

`GET /api/transactions`

Query transactions with optional filters:

- `mti`: Filter by MTI
- `responseCode`: Filter by response code
- `startDate`: ISO 8601 datetime (e.g., `2024-01-01T00:00:00Z`)
- `endDate`: ISO 8601 datetime
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)

Example:

```bash
curl "http://localhost:8080/api/transactions?mti=0200&responseCode=00&page=0&size=20"
```

## Configuration

Default config lives in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  application:
    name: ATM ISO8583 Gateway
  datasource:
    url: jdbc:h2:mem:atm_iso8583
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    baseline-on-migrate: true

iso8583:
  host: 127.0.0.1
  port: 9000
  connect-timeout: 8080
  read-timeout: 30000
  header-length: 4
  institution-id: "000001"

springdoc:
  api-docs:
    path: /api/docs
  swagger-ui:
    path: /api/swagger-ui.html
```

### Database Configuration

The gateway uses H2 in-memory database by default. For production, configure PostgreSQL:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/atm_iso8583
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

## Features

### Transaction Logging

All transactions are automatically logged to the database with:
- Transaction reference
- MTI and fields
- Response code and processing time
- Timestamp

Database migrations are managed via Flyway in `src/main/resources/db/migration/`.

### Monitoring Dashboard

A web dashboard is available at `/dashboard/` providing:
- Real-time traffic metrics
- Recent transaction events
- Error tracking
- Performance statistics

### PowerCard Integration

The gateway includes a PowerCard direct debit XML facade that:
- Accepts PowerCard-specific XML requests
- Maps to ISO 8583 MTI 1200
- Returns XML responses in PowerCard format

See `PowerCardDirectDebitController` and `PowerCardDirectDebitMapper` for details.

### Request/Response Logging

All API requests are logged with:
- Request ID (from `X-Request-ID` header if provided)
- Transaction reference
- MTI and response code
- Processing duration

## ISO 8583 Packager

The packager definition is `src/main/resources/packager/custom_iso87.xml`.

Currently defined fields:

- 0 (MTI)
- 1 (Bitmap)
- 2, 3, 4, 7, 11, 12, 13
- 37, 39, 41, 49

Expand the packager XML to add support for additional ISO 8583 fields as needed.

## Spring Boot Actuator

Actuator endpoints are available under `/api/actuator`:

- `GET /api/actuator/health` - Application health status
- `GET /api/actuator/info` - Application information
- `GET /api/actuator/metrics` - Application metrics

## Tests

```bash
mvn test
```

## Docker

A Dockerfile is provided for containerized deployment:

```bash
docker build -t atm-iso8583-gateway .
docker run -p 8080:8080 atm-iso8583-gateway
```

Or use docker-compose:

```bash
docker-compose up
```

## Notes

- No authentication/authorization yet.
- Packager and supported fields are intentionally minimal (expand `custom_iso87.xml` as needed).
- H2 database is used by default; switch to PostgreSQL for production use.
- All endpoints support both JSON and XML request/response formats.
