# Angular Monitoring Frontend

This app is the monitoring dashboard for the ISO8583 gateway.

## Local development

1. Start backend (`http://localhost:8080`).
2. Run:

```bash
npm install
npm start
```

`npm start` uses `proxy.conf.json`, so `/api/*` calls are proxied to Spring Boot.

## Build for Spring Boot

```bash
npm run build:spring
```

This builds Angular and copies output to:
`../src/main/resources/static/dashboard/`

Then open:
`http://localhost:8080/dashboard/`
