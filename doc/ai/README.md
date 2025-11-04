# AI Documentation for TMS

This directory contains comprehensive documentation for AI assistants working on the TMS codebase.

---

## 📚 Documentation Structure

### Core Documentation

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Complete architecture guide
  - System overview and principles
  - Layer architecture details
  - CQRS implementation
  - Event-driven patterns
  - Module structure

- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Quick lookup guide
  - Essential annotations
  - Code snippets
  - Common commands
  - Validation rules
  - Troubleshooting

### Examples (`examples/`)

Complete, runnable code examples following TMS patterns:

- **[complete-aggregate.md](examples/complete-aggregate.md)**
  - Full aggregate implementation
  - Immutability patterns
  - Domain events
  - Factory methods

- **[complete-use-case.md](examples/complete-use-case.md)**
  - WRITE use cases
  - READ use cases
  - UPDATE use cases
  - Input/Output patterns

- **[complete-controller.md](examples/complete-controller.md)**
  - POST/GET/PUT controllers
  - DTO mappings
  - RestUseCaseExecutor usage
  - Request/Response handling

- **[repository-implementation.md](examples/repository-implementation.md)**
  - Repository interface
  - JPA entities
  - Repository implementation
  - Event outbox integration

- **[event-driven-communication.md](examples/event-driven-communication.md)**
  - Complete event flow
  - Event listeners
  - RabbitMQ configuration
  - Testing events

- **[testing-patterns.md](examples/testing-patterns.md)**
  - Unit tests
  - Integration tests
  - REST API tests
  - Event-driven tests
  - Testcontainers setup

### Prompts (`prompts/`)

Templates for creating new components:

- **[new-aggregate.md](prompts/new-aggregate.md)**
  - Create new aggregate root
  - Value objects
  - Domain events

- **[new-use-case.md](prompts/new-use-case.md)**
  - Create WRITE/READ use cases
  - Input/Output records
  - Validation logic

- **[new-event-listener.md](prompts/new-event-listener.md)**
  - Event listeners
  - Queue configuration
  - Error handling

- **[new-module.md](prompts/new-module.md)**
  - Complete module creation
  - Module structure
  - Integration points

---

## 🎯 Quick Start for AI Assistants

### When Asked to Create Something New

1. **New Aggregate?** → Check `prompts/new-aggregate.md`
2. **New Use Case?** → Check `prompts/new-use-case.md`
3. **New Event Listener?** → Check `prompts/new-event-listener.md`
4. **New Module?** → Check `prompts/new-module.md`

### When Reviewing Code

1. Check layer boundaries (ARCHITECTURE.md)
2. Verify immutability patterns (examples/complete-aggregate.md)
3. Validate CQRS annotations (QUICK_REFERENCE.md)
4. Ensure event patterns (examples/event-driven-communication.md)

### When Writing Tests

1. Refer to `examples/testing-patterns.md`
2. Use Testcontainers for integration tests
3. Mock repositories for use case tests
4. Pure domain tests with no Spring context

---

## 📖 How to Use This Documentation

### For Understanding the System

Read in this order:
1. `ARCHITECTURE.md` - Big picture
2. `examples/complete-aggregate.md` - Domain layer
3. `examples/complete-use-case.md` - Application layer
4. `examples/complete-controller.md` - Infrastructure layer
5. `examples/event-driven-communication.md` - Module communication

### For Creating New Features

Follow this process:
1. Identify what you're creating (aggregate, use case, etc.)
2. Find the relevant prompt in `prompts/`
3. Gather required information
4. Reference the corresponding example in `examples/`
5. Follow the checklist in the prompt
6. Validate against patterns in `QUICK_REFERENCE.md`

### For Code Reviews

Check these aspects:
1. ✅ Layer boundaries respected
2. ✅ Immutability maintained
3. ✅ Events placed correctly
4. ✅ CQRS annotations present
5. ✅ Naming conventions followed
6. ✅ Tests included

---

## 🎨 Architecture at a Glance

```
┌─────────────────────────────────────────────────────────┐
│                    REST Controllers                      │
│              (Infrastructure Layer)                      │
│    @Cqrs(WRITE/READ) + RestUseCaseExecutor             │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│                      Use Cases                           │
│              (Application Layer)                         │
│    @DomainService + @Cqrs(WRITE/READ)                   │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│              Aggregates (Immutable)                      │
│               (Domain Layer)                             │
│    Pure Java - Events - Business Logic                  │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│         Repository Implementation                        │
│              (Infrastructure Layer)                      │
│    JPA + Outbox Pattern                                 │
└─────────────────────────────────────────────────────────┘
```

---

## 🔑 Key Principles (Never Forget)

1. **Domain objects are IMMUTABLE**
   - Update methods return NEW instances
   - No setters allowed

2. **Events are placed in AGGREGATES**
   - Not in use cases
   - Saved transactionally with entity

3. **Modules communicate via EVENTS**
   - No direct repository calls between modules
   - RabbitMQ for async communication

4. **CQRS is MANDATORY**
   - All use cases annotated
   - All controllers annotated
   - Routes to correct database

5. **Layer boundaries are STRICT**
   - Domain: Pure Java only
   - Application: Use cases + interfaces
   - Infrastructure: All frameworks

6. **One aggregate per transaction**
   - Maintain consistency within aggregate
   - Eventual consistency between aggregates

---

## 🛠️ Common Tasks

### Add new operation to existing aggregate

1. Create use case → `prompts/new-use-case.md`
2. Create controller → `examples/complete-controller.md`
3. Create DTOs
4. Add tests

### Add communication between modules

1. Define domain event in source module
2. Place event in aggregate
3. Create listener in target module → `prompts/new-event-listener.md`
4. Configure queue/binding
5. Test event flow

### Fix failing tests

1. Check test output
2. Verify immutability (new instances returned?)
3. Check mocks (correct behavior?)
4. Validate database state (Testcontainers logs)
5. Check event outbox (events saved?)

---

## 📝 Additional Resources

### Related Documentation

- `/doc/CODEBASE_CONTEXT.md` - Project overview
- `/doc/GLOSSARY.md` - Domain terminology
- `/doc/TESTING_GUIDE.md` - Testing strategies
- `/doc/CONTRIBUTING.md` - Contribution guidelines
- `/.github/copilot-instructions.md` - GitHub Copilot config
- `/.github/AI_GUIDELINES.md` - PR review guidelines

### Example Modules

- `src/main/java/.../company/` - Reference implementation
- `src/main/java/.../shipmentorder/` - Another reference
- `src/main/java/.../commons/` - Shared infrastructure

---

## 🤝 Contributing to AI Documentation

When adding new patterns or examples:

1. Follow existing structure
2. Include complete, runnable code
3. Add validation checklist
4. Provide anti-patterns (what NOT to do)
5. Update this README if adding new files

---

## 💡 Tips for Effective Use

### For Junior Developers (and AI)

- Start with `QUICK_REFERENCE.md` for snippets
- Copy patterns from `examples/` directory
- Use prompts as checklists
- Don't deviate from patterns - consistency is key

### For Senior Developers

- Use as validation tool during reviews
- Reference when mentoring
- Update when patterns evolve
- Ensure team alignment

### For AI Assistants

- **ALWAYS** follow patterns exactly
- Reference examples when generating code
- Use prompts as structured input
- Validate output against quick reference
- Prioritize clarity over cleverness

---

## 🔄 Documentation Maintenance

This documentation should be updated when:

- ✅ New architectural patterns are introduced
- ✅ Existing patterns change significantly
- ✅ Common mistakes are discovered
- ✅ New modules are added
- ✅ Technology stack changes

---

## ❓ Questions?

If this documentation doesn't answer your question:

1. Check the related docs (listed above)
2. Look at existing code in reference modules
3. Ask the team for clarification
4. Consider updating this doc with the answer

---

## 📊 Documentation Coverage

- [x] Aggregate patterns
- [x] Use case patterns
- [x] Controller patterns
- [x] Repository patterns
- [x] Event-driven patterns
- [x] Testing patterns
- [x] Module creation
- [x] Quick reference
- [x] Code examples
- [ ] Performance optimization (TODO)
- [ ] Security patterns (TODO)
- [ ] Deployment guide (TODO)

---

**Last Updated:** 2025-11-04

**Maintained by:** TMS Development Team
