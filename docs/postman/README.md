# Postman Integration

This folder contains ready-to-import Postman assets for the ATM ISO 8583 Gateway.

## Files

- `ATM_ISO8583.postman_collection.json`
- `ATM_ISO8583.local.postman_environment.json`

## Import Steps

1. Open Postman.
2. Click `Import`.
3. Import both JSON files from this folder.
4. Select environment `ATM ISO 8583 - Local`.

## Base URL

Default `baseUrl` is:

`http://localhost:8080/api`

If your gateway runs on another host/port, update `baseUrl` in the environment.

## Suggested Run Order

1. `Gateway Config`
2. `Echo (0800)`
3. `Authorize (0100/0110)`
4. `Financial (0200/0210)`
5. `Reversal (0400/0410)`

The collection pre-request script auto-generates:

- `stan` (6 digits)
- `transmissionDateTime` (`MMddHHmmss`, UTC)
- `localTime` (`HHmmss`)
- `localDate` (`MMdd`)
- `rrn` (12 digits)
