# UML (PFE)

This folder contains PlantUML diagrams for the ATM ISO 8583 Gateway.

## Diagrams

- `use-case.puml`: Main actors and use cases.
- `component.puml`: High-level component architecture.
- `class-diagram.puml`: Core classes (controller/service/codec/channel/models + simulator).
- `sequence-send.puml`: End-to-end flow for `POST /api/iso8583/send`.
- `deployment.puml`: Simple deployment view (client, gateway, switch).

## Rendering

Recommended: IntelliJ PlantUML plugin (open a `.puml` file and preview/render).

If you already have PlantUML installed locally:

```bash
plantuml docs/uml/*.puml
```

