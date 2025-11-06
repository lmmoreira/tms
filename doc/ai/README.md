# AI Documentation for TMS

This directory contains comprehensive documentation for AI assistants working on the TMS codebase.

---

## 🚀 Quick Start

**First time here?** Go to [START_HERE.md](START_HERE.md) for fast navigation.

**Need a specific pattern?** Go to [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for code snippets.

**Need complete architecture?** Go to [ARCHITECTURE.md](ARCHITECTURE.md) for full details.

---

## 📚 Documentation Structure

### Core Documentation (Start Here)

- **[START_HERE.md](START_HERE.md)** ⭐ - Quick navigation hub with decision tree
- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** ⭐ - Fast pattern lookup, code snippets
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Architecture guide (inverted pyramid structure)
- **[CODEBASE_CONTEXT.md](CODEBASE_CONTEXT.md)** - Project essentials (tech stack, modules, commands)
- **[GLOSSARY.md](GLOSSARY.md)** - Domain terminology and ubiquitous language

### Detailed Documentation

- **[ARCHITECTURE_FULL.md](ARCHITECTURE_FULL.md)** - Complete architecture details
- **[CODEBASE_CONTEXT_FULL.md](CODEBASE_CONTEXT_FULL.md)** - Complete project context
- **[AI_DOCUMENTATION_REVIEW.md](AI_DOCUMENTATION_REVIEW.md)** - Optimization analysis and strategy
- **[ARCHUNIT_GUIDELINES.md](ARCHUNIT_GUIDELINES.md)** ⭐ - Comprehensive ArchUnit testing guide

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

## 🎯 Most Common Tasks

| Task | Primary Reference | Secondary |
|------|------------------|-----------|
| **Create Use Case** | [prompts/new-use-case.md](prompts/new-use-case.md) | [examples/complete-use-case.md](examples/complete-use-case.md) |
| **Create Controller** | [examples/complete-controller.md](examples/complete-controller.md) | [QUICK_REFERENCE.md](QUICK_REFERENCE.md) |
| **Create Aggregate** | [prompts/new-aggregate.md](prompts/new-aggregate.md) | [examples/complete-aggregate.md](examples/complete-aggregate.md) |
| **Add Event Listener** | [prompts/new-event-listener.md](prompts/new-event-listener.md) | [examples/event-driven-communication.md](examples/event-driven-communication.md) |
| **Quick Pattern Lookup** | [QUICK_REFERENCE.md](QUICK_REFERENCE.md) | - |
| **Review Code** | [QUICK_REFERENCE.md § Validation](QUICK_REFERENCE.md) | [ARCHITECTURE.md](ARCHITECTURE.md) |
| **Create ArchUnit Tests** | [ARCHUNIT_GUIDELINES.md](ARCHUNIT_GUIDELINES.md) | - |

---

## 📖 Learning Paths

### First Time Here?
1. Read [START_HERE.md](START_HERE.md) - Decision tree navigation
2. Scan [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - 5 min patterns
3. Review [ARCHITECTURE.md](ARCHITECTURE.md) - 10 min overview
4. Browse [examples/](examples/) - See real code

### Creating Something Specific?
1. Go to [START_HERE.md](START_HERE.md)
2. Use decision tree to find right prompt/example
3. Follow the template
4. Validate against [QUICK_REFERENCE.md](QUICK_REFERENCE.md)

---
---

## 🔑 Key Principles

1. **Domain objects are IMMUTABLE** - Update methods return NEW instances
2. **Events are placed in AGGREGATES** - Not in use cases, saved transactionally
3. **Modules communicate via EVENTS** - No direct repository calls between modules
4. **CQRS is MANDATORY** - All use cases and controllers annotated
5. **Layer boundaries are STRICT** - Domain = pure Java only
6. **One aggregate per transaction** - Eventual consistency between aggregates

See [ARCHITECTURE.md](ARCHITECTURE.md) for complete details.

---

## 📝 Related Documentation

- **GitHub Copilot Config:** [/.github/copilot-instructions.md](../../.github/copilot-instructions.md) (auto-loaded)
- **Architecture Decisions:** [../adr/](../adr/)
- **Technical Debt:** [../debt/DEBT.md](../debt/DEBT.md)
- **Example Modules:** `src/main/java/.../company/`, `src/main/java/.../shipmentorder/`

---

## 🤝 Contributing to AI Documentation

When adding new patterns or examples:

1. Keep files focused (<200 lines ideal)
2. Use inverted pyramid (critical info first)
3. Include complete, runnable code
4. Update [START_HERE.md](START_HERE.md) decision tree
5. Follow existing structure and formatting

---

**Last Updated:** 2025-11-05

**Quick Links:** [START_HERE](START_HERE.md) | [ARCHITECTURE](ARCHITECTURE.md) | [QUICK_REFERENCE](QUICK_REFERENCE.md) | [Examples](examples/)

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
- [x] Testing patterns (unit + integration)
- [x] **ArchUnit testing patterns** ⭐ NEW
- [x] Module creation
- [x] Quick reference
- [x] Code examples
- [ ] Performance optimization (TODO)
- [ ] Security patterns (TODO)
- [ ] Deployment guide (TODO)

---

**Last Updated:** 2025-11-04

**Maintained by:** TMS Development Team
