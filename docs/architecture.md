# Architecture & Class Diagram

The ISO 8583 Gateway follows a clean layered architecture, abstracting the complexities of low-level TCP/IP and binary message packaging away from the RESTful API.

## 🧱 Component Overview

The main components are:
*   **Controller**: Exposes RESTful endpoints and handles request body validation.
*   **Service**: Orchestrates the round-trip from JSON to ISO 8583 and back.
*   **Codec**: Converts the JSON model to jPOS `ISOMsg` and vice-versa.
*   **Channel**: Low-level TCP client that communicates with the host (Mock Switch).
*   **jPOS Packager**: Uses XML-based definitions (`custom_iso87.xml`) to format binary messages.

---

## 🧬 Class Diagram

Below is a simplified view of the internal class structure.

```mermaid
classDiagram
    class Iso8583GatewayController {
        -gatewayService: Iso8583GatewayService
        -config: Iso8583Config
        +send(request: Iso8583Request): ResponseEntity
        +authorize(request: Iso8583Request): ResponseEntity
        +echo(): ResponseEntity
    }

    class Iso8583GatewayService {
        -codec: Iso8583Codec
        -channel: Iso8583Channel
        -packager: GenericPackager
        +process(request: Iso8583Request): Iso8583Response
        +sendEchoTest(institutionId: String): Iso8583Response
    }

    class Iso8583Codec {
        +toISOMsg(req: Iso8583Request): ISOMsg
        +fromISOMsg(msg: ISOMsg): Iso8583Response
    }

    class Iso8583Channel {
        -host: String
        -port: int
        -connectTimeout: int
        +sendAndReceive(bytes: byte[]): byte[]
    }

    class Iso8583Request {
        +mti: String
        +pan: String
        +stan: String
        +amount: String
        +processingCode: String
        +additionalFields: Map
    }

    class Iso8583Response {
        +status: String
        +mti: String
        +responseCode: String
        +responseDescription: String
        +authorizationCode: String
    }

    Iso8583GatewayController o-- Iso8583GatewayService
    Iso8583GatewayService o-- Iso8583Codec
    Iso8583GatewayService o-- Iso8583Channel
    Iso8583GatewayService ..> Iso8583Request
    Iso8583GatewayService ..> Iso8583Response
```

---

## 🔄 Sequence Diagram: Request Flow

The following diagram illustrates a full request/response cycle:

```mermaid
sequenceDiagram
    participant Client as Frontend / User
    participant Controller as Gateway Controller
    participant Service as Gateway Service
    participant Codec as ISO 8583 Codec
    participant Channel as TCP Channel
    participant Switch as Mock Switch (Host)

    Client->>Controller: POST /api/iso8583/send {JSON}
    Controller->>Service: process(request)
    Service->>Codec: toISOMsg(request)
    Codec-->>Service: ISOMsg object
    Service->>Service: Pack (using GenericPackager)
    Service->>Channel: sendAndReceive(bytes)
    Channel->>Switch: TCP SEND (ISO 8583 Bytes)
    Switch-->>Channel: TCP RECV (ISO 8583 Bytes)
    Channel-->>Service: responseBytes
    Service->>Service: Unpack (using GenericPackager)
    Service->>Codec: fromISOMsg(responseMsg)
    Codec-->>Service: {Iso8583Response}
    Service-->>Controller: {Iso8583Response}
    Controller-->>Client: 200 OK {JSON Response}
```

---

## 🛠 Data Mapping

Fields are mapped according to the **ISO 8583:1987** specification. The mapping rules are defined in the `Iso8583Codec` class and the `src/main/resources/packager/custom_iso87.xml` file.

Key fields include:
*   **DE 2**: Card Number (Primary Account Number)
*   **DE 4**: Transaction Amount (12-digit, zero-padded)
*   **DE 11**: System Trace Audit Number (STAN)
*   **DE 39**: Response Code (e.g., `00` for Approved, `51` for Insufficient Funds)
