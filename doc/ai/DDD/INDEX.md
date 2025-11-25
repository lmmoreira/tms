# DDD Documentation Index

Complete guide to the Domain-Driven Design documentation for the TMS system.

## 📚 Documentation Map

### Quick Start (Start Here!)
1. **[README.md](./README.md)** - Overview and structure
   - What is DDD?
   - TMS System Overview
   - Key Principles
   - Quick Links

### Core Domain Knowledge
2. **[BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md)** - Understanding TMS Domains
   - Company Bounded Context (Full domain model)
   - Shipment Order Bounded Context (Full domain model)
   - Aggregates, Value Objects, Events
   - Database schemas
   - Use cases per context

3. **[EVENT_STORMING.md](./EVENT_STORMING.md)** - Complete Event Catalog
   - Event Storming results
   - All 5 events in detail (payload, triggers, consumers)
   - Event statistics
   - Event flow diagrams
   - Event processing guarantees

4. **[EVENT_CATALOG_SUMMARY.md](./EVENT_CATALOG_SUMMARY.md)** - Quick Event Reference
   - Events overview table
   - Events details matrix
   - Event flow choreography (scenarios)
   - Debugging guide
   - Best practices

### System Design
5. **[CONTEXT_MAP.md](./CONTEXT_MAP.md)** - How Contexts Relate
   - Relationship overview (Partnership)
   - Integration channels (2-way async)
   - Shared entities & Anti-corruption layer
   - Dependencies graph
   - Data flows and test strategies

6. **[INTEGRATION_PATTERNS.md](./INTEGRATION_PATTERNS.md)** - How to Integrate
   - Event-Driven Integration pattern
   - Eventual Consistency pattern
   - Outbox Pattern (transactional safety)
   - Anti-Corruption Layer pattern
   - Saga Pattern (future)
   - Best practices

### Reference & Learning
7. **[ENTITY_RELATIONSHIP_DIAGRAMS.md](./ENTITY_RELATIONSHIP_DIAGRAMS.md)** - Visual Models
   - Class diagrams for both contexts
   - Database schemas for both contexts
   - Entity relationships
   - Cross-context data flows
   - Data lifecycle timelines
   - Model validation rules

8. **[GLOSSARY.md](./GLOSSARY.md)** - DDD Terminology
   - Core DDD concepts (Aggregate, Entity, Value Object, etc.)
   - Integration patterns terminology
   - Data synchronization concepts
   - Database patterns
   - Lifecycle and quality concepts
   - Quick reference table

## 🎯 How to Use This Documentation

### I want to understand...

**...the system architecture**
1. Read: [README.md](./README.md) - Overview
2. Then: [BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md) - Domain models
3. Finally: [CONTEXT_MAP.md](./CONTEXT_MAP.md) - How they relate

**...what events exist**
1. Quick lookup: [EVENT_CATALOG_SUMMARY.md](./EVENT_CATALOG_SUMMARY.md) - Tables & scenarios
2. Deep dive: [EVENT_STORMING.md](./EVENT_STORMING.md) - Complete details
3. Reference: [GLOSSARY.md](./GLOSSARY.md#domain-event) - Terminology

**...how to integrate new features**
1. Understand pattern: [INTEGRATION_PATTERNS.md](./INTEGRATION_PATTERNS.md)
2. Review examples: [CONTEXT_MAP.md](./CONTEXT_MAP.md) - Current integrations
3. Check best practices: [EVENT_CATALOG_SUMMARY.md](./EVENT_CATALOG_SUMMARY.md) - Event patterns

**...the data model**
1. Visual overview: [ENTITY_RELATIONSHIP_DIAGRAMS.md](./ENTITY_RELATIONSHIP_DIAGRAMS.md)
2. By context: [BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md) - Domain models
3. Lifecycle: [ENTITY_RELATIONSHIP_DIAGRAMS.md](./ENTITY_RELATIONSHIP_DIAGRAMS.md) - Timelines

**...DDD concepts**
1. Quick terms: [GLOSSARY.md](./GLOSSARY.md) - Definitions & examples
2. In context: [BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md) - Applied to TMS
3. Advanced: [INTEGRATION_PATTERNS.md](./INTEGRATION_PATTERNS.md) - Pattern details

**...how to debug a problem**
1. Event not delivered: [EVENT_CATALOG_SUMMARY.md](./EVENT_CATALOG_SUMMARY.md) - Debugging guide
2. Context integration: [CONTEXT_MAP.md](./CONTEXT_MAP.md) - Data flows
3. Pattern issue: [INTEGRATION_PATTERNS.md](./INTEGRATION_PATTERNS.md) - Pattern details

## 📊 Content Statistics

| Document | Size | Topics |
|----------|------|--------|
| README.md | 3KB | Overview, structure, quick links |
| BOUNDED_CONTEXTS.md | 14KB | 2 contexts, 3 aggregates, 9 value objects |
| EVENT_STORMING.md | 31KB | 5 events, flows, sequences, guarantees |
| EVENT_CATALOG_SUMMARY.md | 20KB | Event tables, scenarios, debugging |
| CONTEXT_MAP.md | 26KB | Integration channels, dependencies, flows |
| INTEGRATION_PATTERNS.md | 20KB | 5 patterns with examples |
| ENTITY_RELATIONSHIP_DIAGRAMS.md | 32KB | ERDs, schemas, validations, lifecycles |
| GLOSSARY.md | 19KB | 30+ terms with examples |
| **TOTAL** | **165KB** | **Comprehensive DDD coverage** |

## 🔍 Cross-References

### By Topic

**Events**:
- Overview: [EVENT_STORMING.md](./EVENT_STORMING.md)
- Quick reference: [EVENT_CATALOG_SUMMARY.md](./EVENT_CATALOG_SUMMARY.md)
- Catalog: [BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md#events-generated)
- Term: [GLOSSARY.md](./GLOSSARY.md#domain-event)

**Aggregates**:
- Company Aggregate: [BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md#root-aggregate-company)
- ShipmentOrder Aggregate: [BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md#root-aggregate-shipmentorder)
- Definition: [GLOSSARY.md](./GLOSSARY.md#aggregate)
- Model diagram: [ENTITY_RELATIONSHIP_DIAGRAMS.md](./ENTITY_RELATIONSHIP_DIAGRAMS.md)

**Integration**:
- Patterns: [INTEGRATION_PATTERNS.md](./INTEGRATION_PATTERNS.md)
- Channels: [CONTEXT_MAP.md](./CONTEXT_MAP.md#integration-channels)
- Choreography: [EVENT_CATALOG_SUMMARY.md](./EVENT_CATALOG_SUMMARY.md#event-flow-choreography)

**Data Synchronization**:
- Anti-Corruption Layer: [CONTEXT_MAP.md](./CONTEXT_MAP.md#shared-entities--anti-corruption-layer)
- Pattern: [INTEGRATION_PATTERNS.md](./INTEGRATION_PATTERNS.md#pattern-4-anti-corruption-layer)
- Implementation: [INTEGRATION_PATTERNS.md](./INTEGRATION_PATTERNS.md#implementation-in-tms)

**Database**:
- Company schema: [BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md#database-schema)
- ShipmentOrder schema: [BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md#database-schema-1)
- ERDs: [ENTITY_RELATIONSHIP_DIAGRAMS.md](./ENTITY_RELATIONSHIP_DIAGRAMS.md)
- Lifecycle: [ENTITY_RELATIONSHIP_DIAGRAMS.md](./ENTITY_RELATIONSHIP_DIAGRAMS.md#data-lifecycle)

## 🎓 Learning Paths

### Path 1: New Developer (No DDD Background)
```
1. README.md              (10 min)  - Get oriented
2. GLOSSARY.md            (20 min)  - Learn terminology
3. BOUNDED_CONTEXTS.md    (30 min)  - Understand domains
4. EVENT_CATALOG_SUMMARY  (20 min)  - See events in action
5. CONTEXT_MAP.md         (20 min)  - Understand integration
Total: ~100 minutes
```

### Path 2: Senior Developer (Familiar with DDD)
```
1. README.md              (5 min)   - Quick overview
2. BOUNDED_CONTEXTS.md    (15 min)  - Review models
3. CONTEXT_MAP.md         (15 min)  - Integration design
4. INTEGRATION_PATTERNS.md (15 min)  - Pattern review
Total: ~50 minutes
```

### Path 3: Adding New Feature
```
1. EVENT_CATALOG_SUMMARY  (10 min)  - Understand events
2. CONTEXT_MAP.md         (10 min)  - Find integration point
3. INTEGRATION_PATTERNS.md (15 min)  - Choose pattern
4. BOUNDED_CONTEXTS.md    (20 min)  - Identify model changes
Total: ~55 minutes
```

### Path 4: Debugging Integration Issue
```
1. EVENT_CATALOG_SUMMARY  (5 min)   - Find event info
2. CONTEXT_MAP.md         (10 min)  - Trace flow
3. EVENT_CATALOG_SUMMARY  (5 min)   - Debugging guide
4. INTEGRATION_PATTERNS.md (10 min)  - Pattern details
Total: ~30 minutes
```

## 🔗 Related Documentation

- **Main Architecture**: [../ARCHITECTURE.md](../ARCHITECTURE.md)
- **Code Style**: [../CODE_STYLE.md](../CODE_STYLE.md)
- **Test Structure**: [../TEST_STRUCTURE.md](../TEST_STRUCTURE.md)
- **Integration Tests**: [../INTEGRATION_TESTS.md](../INTEGRATION_TESTS.md)
- **ArchUnit Guidelines**: [../ARCHUNIT_GUIDELINES.md](../ARCHUNIT_GUIDELINES.md)

## ❓ FAQ

**Q: Where do I find what events exist?**
A: [EVENT_STORMING.md](./EVENT_STORMING.md) has the complete catalog, or [EVENT_CATALOG_SUMMARY.md](./EVENT_CATALOG_SUMMARY.md) for quick lookup.

**Q: How do I understand the Company and ShipmentOrder relationship?**
A: Start with [CONTEXT_MAP.md](./CONTEXT_MAP.md) - it shows the Partnership relationship and integration channels.

**Q: What's the anti-corruption layer?**
A: It's the CompanyData value object. See [CONTEXT_MAP.md](./CONTEXT_MAP.md#shared-entities--anti-corruption-layer) and [INTEGRATION_PATTERNS.md](./INTEGRATION_PATTERNS.md#pattern-4-anti-corruption-layer).

**Q: How do I add a new event?**
A: Follow [INTEGRATION_PATTERNS.md](./INTEGRATION_PATTERNS.md#pattern-1-event-driven-integration) and use the examples from [EVENT_CATALOG_SUMMARY.md](./EVENT_CATALOG_SUMMARY.md).

**Q: What's the difference between Company and ShipmentOrder contexts?**
A: See [BOUNDED_CONTEXTS.md](./BOUNDED_CONTEXTS.md) for full domain models and responsibilities.

**Q: How is data synchronized between contexts?**
A: Events trigger synchronization. See [CONTEXT_MAP.md](./CONTEXT_MAP.md#integration-point-1-company-data-synchronization) for the flow.

**Q: What's eventual consistency?**
A: Read [INTEGRATION_PATTERNS.md](./INTEGRATION_PATTERNS.md#pattern-2-eventual-consistency) for full explanation with examples.

**Q: How do I test integration between contexts?**
A: See [CONTEXT_MAP.md](./CONTEXT_MAP.md#testing-contexts-in-integration) for testing strategies.

## 📝 Document Information

- **Created**: November 25, 2024
- **Last Updated**: November 25, 2024
- **Scope**: Transportation Management System (TMS)
- **Team**: Full development team
- **Status**: Complete ✓

## 🚀 Next Steps

1. **Read** the appropriate documents for your role/task
2. **Review** examples in the documents
3. **Refer back** when implementing features
4. **Update documentation** when adding new concepts
5. **Share** with team members learning the system

---

**Tip**: Use Ctrl+F (Cmd+F) within these markdown files to search for specific terms or concepts. Most markdown viewers support search within documents.
