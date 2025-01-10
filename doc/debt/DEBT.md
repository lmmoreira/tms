# TECHNICAL DEBT

## Context

List of technical debt to be paid.

## URGENT

-[x] **DEBT-001** - Add TTL by default on domain events in the event store.
-[x] **DEBT-002** - Add Logback async appender for logs in order to improve performance and do not unmount and mount virtual threads.
- [x] **DEBT-004** - For log centralization, we are currently using Promtail and Loki. However, since Promtail is a vendor lock-in, we are evaluating OpenTelemetry (OTel) collectors for log management to facilitate switching from Loki to another solution if needed. While OpenTelemetry supports log file collection through the otel-contrib extension, it has some known issues that need to be addressed.

## NOT URGENT

- [x] **DEBT-003** - Enable compression of endpoints in order to improve performance.
- [x] **DEBT-005** - Enable RFC 9457 for error standardization on spring-boot.
  - Ref.: https://datatracker.ietf.org/doc/html/rfc9457
  - Ref.: https://dev.to/abdelrani/error-handling-in-spring-web-using-rfc-9457-specification-5dj1