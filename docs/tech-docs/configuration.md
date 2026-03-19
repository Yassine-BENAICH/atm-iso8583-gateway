# Configuration Guide

The ISO 8583 Gateway app can be configured using the `application.yml` file or standard Spring environment variables.

## Network Configuration

The network configuration determines how the gateway connects to the target host (Bank Switch or Mock Switch).

| Property                  | Default     | Description                                    |
|:--------------------------|:------------|:-----------------------------------------------|
| `iso8583.host`            | `127.0.0.1` | The target IP/Host of the switch.              |
| `iso8583.port`            | `9000`      | The target TCP port.                           |
| `iso8583.connect-timeout` | `5000`      | Connection timeout in ms.                      |
| `iso8583.read-timeout`    | `30000`     | Read timeout for responses in ms.              |
| `iso8583.header-length`   | `4`         | Number of bytes used as the length prefix.     |
| `iso8583.institution-id`  | `000001`    | The acquiring institution ID sent in messages. |

## Packager Configuration

The gateway uses an XML-based jPOS packager configuration.

*   **File Location**: `src/main/resources/packager/custom_iso87.xml`
*   **Purpose**: Defines the length and data type of each ISO 8583 field (e.g., LLLVAR, NUMERIC, etc.).

## Environment Overrides

You can override any configuration property via environment variables when running the application.

```bash
# Example: Pointing to a production switch
export ISO8583_HOST=10.10.150.25
export ISO8583_PORT=8583
mvn spring-boot:run
```

---

## Logging

By default, the application logs critical events and transaction summaries.

*   **Default Log Level**: `INFO`
*   **Package Logs**: `com.atm` is set to `DEBUG` for detailed message mapping logs.
*   **Log File**: `logs/iso8583-gateway.log`

To change the log level, update the `logging.level` section in `application.yml`.
