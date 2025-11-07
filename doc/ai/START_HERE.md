# Start Here - AI Assistant Navigation

**Purpose:** Quick navigation for AI assistants to find the right documentation fast.

---

## 🎯 What do you need?

### 🆕 Create Something New

| Task | Go To |
|------|-------|
| **New Aggregate** (domain entity) | [prompts/new-aggregate.md](prompts/new-aggregate.md) |
| **New Use Case** (business operation) | [prompts/new-use-case.md](prompts/new-use-case.md) |
| **New Controller** (REST endpoint) | [examples/controller-post.md](examples/complete-controller.md) |
| **New Event Listener** (module communication) | [prompts/new-event-listener.md](prompts/new-event-listener.md) |
| **New Module** (bounded context) | [prompts/new-module.md](prompts/new-module.md) |
| **Test Data Builder** (test utilities) | [prompts/test-data-builders.md](prompts/test-data-builders.md) |
| **Fake Repository** (test doubles) | [prompts/fake-repositories.md](prompts/fake-repositories.md) |

### 📚 Understand Pattern

| Need | Go To |
|------|-------|
| **Quick pattern lookup** | [QUICK_REFERENCE.md](QUICK_REFERENCE.md) ⭐ |
| **Core architectural concepts** | [ARCHITECTURE.md](ARCHITECTURE.md) |
| **Code style & comments** | [CODE_STYLE.md](CODE_STYLE.md) |
| **Project overview & tech stack** | [CODEBASE_CONTEXT.md](CODEBASE_CONTEXT.md) |
| **Domain terminology** | [GLOSSARY.md](GLOSSARY.md) |

### 💻 See Code Example

| Pattern | Go To |
|---------|-------|
| **Aggregate** (immutable domain entity) | [examples/complete-aggregate.md](examples/complete-aggregate.md) |
| **Use Case** (WRITE/READ operations) | [examples/complete-use-case.md](examples/complete-use-case.md) |
| **Controller** (REST API) | [examples/complete-controller.md](examples/complete-controller.md) |
| **Repository** (persistence) | [examples/repository-implementation.md](examples/repository-implementation.md) |
| **Events** (module communication) | [examples/event-driven-communication.md](examples/event-driven-communication.md) |
| **Testing** (unit, integration, REST) | [examples/testing-patterns.md](examples/testing-patterns.md) |

### ✅ Review Code

| Task | Go To |
|------|-------|
| **Validation rules** | [QUICK_REFERENCE.md § Validation Rules](QUICK_REFERENCE.md) |
| **Anti-patterns to avoid** | [.github/copilot-instructions.md § Anti-Patterns](../.github/copilot-instructions.md) |
| **Layer boundaries** | [ARCHITECTURE.md § Layer Architecture](ARCHITECTURE.md) |

---

## 🚀 Quick Start by Task

### "I need to add a new field to an aggregate"

1. Review immutability pattern: [examples/complete-aggregate.md § Update Methods](examples/complete-aggregate.md)
2. Remember: Return NEW instance
3. Place domain event if significant change
4. Update JPA entity
5. Update use cases as needed
6. Test immutability

### "I need to add a new REST endpoint"

1. Create use case → [prompts/new-use-case.md](prompts/new-use-case.md)
2. Create controller → [examples/complete-controller.md](examples/complete-controller.md)
3. Create DTOs (request & response)
4. Wire in configuration
5. Add tests → [examples/testing-patterns.md](examples/testing-patterns.md)

### "I need modules to communicate"

1. Define event in source module
2. Place event in aggregate → [examples/complete-aggregate.md](examples/complete-aggregate.md)
3. Create listener in target → [prompts/new-event-listener.md](prompts/new-event-listener.md)
4. Configure queue/binding
5. Test flow → [examples/testing-patterns.md § Event Tests](examples/testing-patterns.md)

### "I need to create a new module"

1. Follow checklist: [prompts/new-module.md](prompts/new-module.md)
2. Reference examples for each component
3. Mirror structure of `company` or `shipmentorder` modules
4. Validate against [ARCHITECTURE.md](ARCHITECTURE.md)

### "I need to write tests"

1. Domain tests → [examples/testing-patterns.md § Unit Tests](examples/testing-patterns.md)
2. Use case tests → [examples/testing-patterns.md § Integration Tests](examples/testing-patterns.md)
3. Test data builders → [prompts/test-data-builders.md](prompts/test-data-builders.md)
4. Fake repositories → [prompts/fake-repositories.md](prompts/fake-repositories.md)
5. Full integration → [examples/testing-patterns.md § Full Integration](examples/testing-patterns.md)
6. REST tests → [examples/testing-patterns.md § REST API Tests](examples/testing-patterns.md)

---

## ⚡ Most Common Tasks

**90% of the time, you need one of these:**

1. **Create Use Case** → [prompts/new-use-case.md](prompts/new-use-case.md) + [examples/complete-use-case.md](examples/complete-use-case.md)
2. **Create Controller** → [examples/complete-controller.md](examples/complete-controller.md)
3. **Update Aggregate** → [examples/complete-aggregate.md § Update Methods](examples/complete-aggregate.md)
4. **Quick Pattern** → [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
5. **Validation Rules** → [QUICK_REFERENCE.md § Validation Rules](QUICK_REFERENCE.md)

---

## 📖 Full Documentation Map

```
doc/ai/
├── START_HERE.md (this file) ⭐ Navigation hub
├── QUICK_REFERENCE.md ⭐ Fast pattern lookup
├── ARCHITECTURE.md - Complete architecture guide
├── CODE_STYLE.md - Code style and documentation guidelines
├── CODEBASE_CONTEXT.md - Project overview
├── GLOSSARY.md - Domain terminology
├── AI_DOCUMENTATION_REVIEW.md - Optimization analysis
│
├── examples/ - Complete code examples
│   ├── complete-aggregate.md
│   ├── complete-use-case.md
│   ├── complete-controller.md
│   ├── repository-implementation.md
│   ├── event-driven-communication.md
│   └── testing-patterns.md
│
└── prompts/ - Templates for creating components
    ├── new-aggregate.md
    ├── new-use-case.md
    ├── new-event-listener.md
    ├── new-module.md
    ├── test-data-builders.md
    └── fake-repositories.md
```

---

## 🎓 Learning Path

**If this is your first time:**

1. Read [ARCHITECTURE.md](ARCHITECTURE.md) - 10 min overview
2. Scan [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - 5 min patterns
3. Browse [examples/](examples/) - See real code
4. Try creating something with [prompts/](prompts/)

**If you're experienced:**

1. Go directly to what you need using tables above
2. Use [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for quick lookups
3. Reference [examples/](examples/) when needed

---

## 🔗 External Links

- **GitHub Copilot Config:** [.github/copilot-instructions.md](../.github/copilot-instructions.md) (auto-loaded)
- **Architecture Decisions:** [../adr/](../adr/)
- **Technical Debt:** [../debt/DEBT.md](../debt/DEBT.md)

---

**Last Updated:** 2025-11-05

**Questions?** Most answers are in [QUICK_REFERENCE.md](QUICK_REFERENCE.md) or [ARCHITECTURE.md](ARCHITECTURE.md).
