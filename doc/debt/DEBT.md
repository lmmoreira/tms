# TECHNICAL DEBT

## Context

List of technical debt to be paid.

## URGENT

-[x] **DEBT-001** - Add TTL by default on domain events in the event store.
-[x] **DEBT-002** - Add Logback async appender for logs in order to improve performance and do not unmount and mount virtual threads.
-[x] **DEBT-004** - For log centralization, we are currently using Promtail and Loki. However, since Promtail is a vendor lock-in, evaluates OpenTelemetry (OTel) collectors for log management to unify logs, metrics and traces on collector and facilitate switching from Loki to another solution if needed.
-[ ] **DEBT-010** - On management of ReadOnly Transaction and Read Replicas now we are on the premise that is the same database, or a replica of a relational, so all the writes go for the same transaction and all read for read only database, but must be possible in the future a scenario where read transactions goes to write replica in an easy way.

## NOT URGENT

- [x] **DEBT-003** - Enable compression of endpoints in order to improve performance.
- [x] **DEBT-005** - Enable RFC 9457 for error standardization on spring-boot.
  - Ref.: https://datatracker.ietf.org/doc/html/rfc9457
  - Ref.: https://dev.to/abdelrani/error-handling-in-spring-web-using-rfc-9457-specification-5dj1
- [x] **DEBT-006** - Metrics on ports and adapters - create a pattern
- [x] **DEBT-007** - Consistency on integration events inside usecases. Should be thrown by the aggregate
- [x] **DEBT-008** - Monorepo study. Modular monolith controlled by env and component scanning limitation.
- [x] **DEBT-009** - Add Sbom support.
