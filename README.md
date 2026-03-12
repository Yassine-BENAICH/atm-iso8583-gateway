# ATM ISO 8583 Gateway

Spring Boot REST API gateway that accepts JSON, packs it into ISO 8583 (via jPOS), sends it over TCP to a switch, then returns the ISO 8583 response as JSON.

## Tech Stack

- Java 17
- Spring Boot 3.2.3
- jPOS 2.1.10
- Maven
- OpenAPI / Swagger UI (springdoc)

## Prerequisites

- JDK 17+
- Maven 3.8+

## Quick Start

### 1) Start the Mock Switch (port 9000)

Option A: Run the `com.atm.iso8583.simulator.Iso8583MockSwitch` main class from your IDE.

Option B: Run via Maven (uses the Maven Exec plugin):

```bash
mvn -DskipTests test-compile exec:java -Dexec.mainClass=com.atm.iso8583.simulator.Iso8583MockSwitch
```

The mock switch auto-responds with approval (`DE39=00`) and flips the MTI from `x200` to `x210` (e.g. `0200` -> `0210`).

### 2) Start the Gateway (port 8080)

```bash
mvn spring-boot:run
```

### 3) OpenAPI / Swagger

- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/docs`

## API

### Health

`GET /api/iso8583/health`

Response:

```text
Gateway is running
```

### Send ISO 8583

`POST /api/iso8583/send`

Request body:

- `mti`: ISO 8583 MTI (e.g. `0200`)
- `fields`: object where keys are ISO field numbers as strings and values are field values (e.g. `"11": "123456"`)

Example:

PowerShell:

```powershell
$body = @{
  mti = "0200"
  fields = @{
    "2" = "1234567890123456"
    "3" = "000000"
    "4" = "000000010000"
    "11" = "123456"
    "41" = "12345678"
    "49" = "840"
  }
} | ConvertTo-Json -Depth 5

Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/iso8583/send" -ContentType "application/json" -Body $body
```

Bash:

```bash
curl -X POST "http://localhost:8080/api/iso8583/send" \
  -H "Content-Type: application/json" \
  -d '{"mti":"0200","fields":{"2":"1234567890123456","3":"000000","4":"000000010000","11":"123456","41":"12345678","49":"840"}}'
```

Example response shape:

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
  "message": "Success"
}
```

## Configuration

Default config lives in `src/main/resources/application.properties`:

```properties
server.port=8080
iso8583.switch.host=localhost
iso8583.switch.port=9000
iso8583.switch.timeout=30000

springdoc.api-docs.path=/api/docs
springdoc.swagger-ui.path=/api/swagger-ui.html
```

## ISO 8583 Packager

The packager definition is `src/main/resources/iso8583-packager.xml`.

Currently defined fields:

- 0 (MTI)
- 1 (Bitmap)
- 2, 3, 4, 7, 11, 12, 13
- 37, 39, 41, 49

## Tests

```bash
mvn test
```

## Notes / Limitations

- No authentication/authorization yet.
- Packager and supported fields are intentionally minimal (expand `iso8583-packager.xml` as needed).
