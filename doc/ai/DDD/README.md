# Domain-Driven Design (DDD) Documentation

This folder contains comprehensive Domain-Driven Design documentation for the Transportation Management System (TMS).

## What is DDD?

Domain-Driven Design is a software design philosophy that places the business domain at the center of the development process. It provides patterns and practices for handling complex business logic and maintaining clean, maintainable codebases.

## Documentation Structure

```
DDD/
├── README.md                           # This file
├── BOUNDED_CONTEXTS.md                 # Bounded contexts overview
├── EVENT_STORMING.md                   # Event storming results and event catalog
├── CONTEXT_MAP.md                      # How bounded contexts relate to each other
├── INTEGRATION_PATTERNS.md             # Patterns for context integration
├── ENTITY_RELATIONSHIP_DIAGRAMS.md     # Domain models and relationships
└── GLOSSARY.md                         # Domain language and terminology
```

## Quick Links

- **[Bounded Contexts](./BOUNDED_CONTEXTS.md)** - Understanding TMS's main domains
- **[Event Storming Results](./EVENT_STORMING.md)** - All events in the system
- **[Context Map](./CONTEXT_MAP.md)** - How bounded contexts communicate
- **[Integration Patterns](./INTEGRATION_PATTERNS.md)** - How services are integrated
- **[Entity Relationships](./ENTITY_RELATIONSHIP_DIAGRAMS.md)** - Domain models
- **[DDD Glossary](./GLOSSARY.md)** - Domain terminology

## TMS System Overview

The Transportation Management System (TMS) is organized around two primary bounded contexts:

1. **Company Subdomain** - Manages company information, configurations, and agreements
2. **Shipment Order Subdomain** - Manages shipment orders and their lifecycle

These contexts communicate asynchronously through domain events published via RabbitMQ.

## Key Principles in TMS

- **Ubiquitous Language** - Business terms are reflected in code (CompanyId, ShipmentOrderId, etc.)
- **Bounded Contexts** - Each domain has clear boundaries and ownership
- **Event-Driven** - Loose coupling through asynchronous event communication
- **Immutable Aggregates** - Domain objects maintain invariants and consistency
- **Explicit Commands** - Use cases represent explicit business operations

## Getting Started

1. **New to the project?** Start with [BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md)
2. **Understanding events?** Read [EVENT_STORMING.md](./EVENT_STORMING.md)
3. **How do services communicate?** Check [CONTEXT_MAP.md](./CONTEXT_MAP.md)
4. **Learning domain terminology?** See [GLOSSARY.md](./GLOSSARY.md)

## Key Metrics

- **Bounded Contexts**: 2
- **Domain Events**: 5
- **Aggregates**: 3
- **Event Listeners**: 3
- **Integration Points**: 2 (bidirectional)

## Related Documentation

- [Main Architecture Guide](../ARCHITECTURE.md)
- [Code Style Guide](../CODE_STYLE.md)
- [Test Structure](../TEST_STRUCTURE.md)
- [Integration Tests](../INTEGRATION_TESTS.md)
