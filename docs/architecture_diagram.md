# High-Level Architecture Visualization

The following diagram illustrates the interaction between the Gateway and the External Host (Mock Switch).

```mermaid
graph TB
    subgraph "External Client Network"
        FE[ATM / Web Dashboard]
    end

    subgraph "ISO 8583 Gateway (Spring Boot)"
        direction TB
        API[REST Controller]
        Service[Gateway Service]
        Codec[ISO 8583 Codec]
        Channel[TCP Channel]
        
        API -- "JSON" --> Service
        Service -- "JSON" --> Codec
        Codec -- "ISOMsg" --> Service
        Service -- "ISO 8583 Bytes" --> Channel
    end

    subgraph "Financial Switch Network"
        Switch[Mock Switch / Host]
    end

    FE -- "POST /api/iso8583/send" --> API
    Channel -- "TCP/IP Port 9000" --> Switch
    Switch -- "Approved (00)" --> Channel

    style API fill:#f9f,stroke:#333,stroke-width:4px
    style Switch fill:#00d2ff,stroke:#0575E6,stroke-width:4px
    style Service fill:#d4fc79,stroke:#96e6a1,stroke-width:4px
```

## Security Layer (Conceptual)

While not implemented in this demo, a production gateway should include:

*   **HSM (Hardware Security Module)**: For DE 52 (PIN) encryption/decryption.
*   **SSL/TLS**: For the REST API endpoints.
*   **VPN / Leased Line**: For the connection from Gateway to Switch.
*   **MAC (Message Authentication Code)**: For message integrity (DE 64/128).
