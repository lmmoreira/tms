# AI Assistant Documentation Index

**Complete navigation guide for AI tools working on TMS.**

---

## 🎯 Start Here

### First Time?
1. Read [ARCHITECTURE.md](ARCHITECTURE.md) for the big picture
2. Scan [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for common patterns
3. Browse [examples/](examples/) to see actual code

### Need to Create Something?
Go directly to the relevant prompt:
- [New Aggregate](prompts/new-aggregate.md)
- [New Use Case](prompts/new-use-case.md)
- [New Event Listener](prompts/new-event-listener.md)
- [New Module](prompts/new-module.md)

### Need an Example?
Find complete implementations in [examples/](examples/)

---

## 📚 Documentation by Purpose

### Understanding the System

| Document | Purpose | When to Use |
|----------|---------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Complete system architecture | Learning the system, understanding design decisions |
| [QUICK_REFERENCE.md](QUICK_REFERENCE.md) | Quick pattern lookup | Need a snippet or reminder |
| [README.md](README.md) | Documentation overview | First time, finding what you need |

### Creating New Code

| Document | Purpose | When to Use |
|----------|---------|-------------|
| [prompts/new-aggregate.md](prompts/new-aggregate.md) | Create aggregate root | New domain entity needed |
| [prompts/new-use-case.md](prompts/new-use-case.md) | Create use case | New business operation |
| [prompts/new-event-listener.md](prompts/new-event-listener.md) | Create event listener | Module communication needed |
| [prompts/new-module.md](prompts/new-module.md) | Create entire module | New bounded context |

### Understanding Patterns

| Document | Purpose | When to Use |
|----------|---------|-------------|
| [examples/complete-aggregate.md](examples/complete-aggregate.md) | Full aggregate example | Understanding immutability, events |
| [examples/complete-use-case.md](examples/complete-use-case.md) | Use case examples | WRITE/READ operations |
| [examples/complete-controller.md](examples/complete-controller.md) | Controller examples | REST API endpoints |
| [examples/repository-implementation.md](examples/repository-implementation.md) | Repository pattern | Persistence layer |
| [examples/event-driven-communication.md](examples/event-driven-communication.md) | Event flow | Module communication |
| [examples/testing-patterns.md](examples/testing-patterns.md) | Testing strategies | Writing tests |

### Code Review & Validation

| Document | Purpose | When to Use |
|----------|---------|-------------|
| [/.github/copilot-instructions.md](/.github/copilot-instructions.md) | GitHub Copilot config | Using GitHub Copilot |
| [QUICK_REFERENCE.md](QUICK_REFERENCE.md) | Validation rules | Checking code compliance |

---

## 🔍 Find Documentation by Topic

### Domain-Driven Design

**Aggregates:**
- Pattern: [ARCHITECTURE.md § Aggregates](ARCHITECTURE.md)
- Example: [examples/complete-aggregate.md](examples/complete-aggregate.md)
- Prompt: [prompts/new-aggregate.md](prompts/new-aggregate.md)
- Quick: [QUICK_REFERENCE.md § Create New Aggregate](QUICK_REFERENCE.md)

**Value Objects:**
- Pattern: [ARCHITECTURE.md § Value Objects](ARCHITECTURE.md)
- Example: [examples/complete-aggregate.md § Value Objects](examples/complete-aggregate.md)
- Quick: [QUICK_REFERENCE.md § Value Object](QUICK_REFERENCE.md)

**Domain Events:**
- Pattern: [ARCHITECTURE.md § Domain Events](ARCHITECTURE.md)
- Example: [examples/event-driven-communication.md](examples/event-driven-communication.md)
- Quick: [QUICK_REFERENCE.md § Domain Event](QUICK_REFERENCE.md)

### Application Layer

**Use Cases:**
- Pattern: [ARCHITECTURE.md § Use Cases](ARCHITECTURE.md)
- Example: [examples/complete-use-case.md](examples/complete-use-case.md)
- Prompt: [prompts/new-use-case.md](prompts/new-use-case.md)
- Quick: [QUICK_REFERENCE.md § Use Case Pattern](QUICK_REFERENCE.md)

**Repository Interfaces:**
- Pattern: [ARCHITECTURE.md § Repositories](ARCHITECTURE.md)
- Example: [examples/repository-implementation.md § Repository Interface](examples/repository-implementation.md)

### Infrastructure Layer

**REST Controllers:**
- Pattern: [ARCHITECTURE.md § REST Controllers](ARCHITECTURE.md)
- Example: [examples/complete-controller.md](examples/complete-controller.md)
- Quick: [QUICK_REFERENCE.md § Controller Pattern](QUICK_REFERENCE.md)

**Repository Implementation:**
- Example: [examples/repository-implementation.md](examples/repository-implementation.md)
- Quick: [QUICK_REFERENCE.md § Repository Implementation](QUICK_REFERENCE.md)

**Event Listeners:**
- Example: [examples/event-driven-communication.md § Event Listener](examples/event-driven-communication.md)
- Prompt: [prompts/new-event-listener.md](prompts/new-event-listener.md)
- Quick: [QUICK_REFERENCE.md § Event Listener](QUICK_REFERENCE.md)

**JPA Entities:**
- Example: [examples/repository-implementation.md § JPA Entity](examples/repository-implementation.md)

### Cross-Cutting Concerns

**CQRS:**
- Pattern: [ARCHITECTURE.md § CQRS](ARCHITECTURE.md)
- Examples: Throughout all examples
- Quick: [QUICK_REFERENCE.md § Essential Annotations](QUICK_REFERENCE.md)

**Event-Driven Architecture:**
- Pattern: [ARCHITECTURE.md § Event-Driven Architecture](ARCHITECTURE.md)
- Complete Flow: [examples/event-driven-communication.md](examples/event-driven-communication.md)
- Listener Creation: [prompts/new-event-listener.md](prompts/new-event-listener.md)

**Testing:**
- Comprehensive: [examples/testing-patterns.md](examples/testing-patterns.md)
- Quick: [QUICK_REFERENCE.md § Testing Shortcuts](QUICK_REFERENCE.md)

**Immutability:**
- Pattern: [ARCHITECTURE.md § Immutability](ARCHITECTURE.md)
- Example: [examples/complete-aggregate.md § Update Methods](examples/complete-aggregate.md)

---

## 🎨 Documentation by Role

### For AI Code Generators

**Start with these (in order):**
1. [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Patterns and snippets
2. Relevant prompt from [prompts/](prompts/)
3. Corresponding example from [examples/](examples/)

**Key principle:** Follow patterns EXACTLY - consistency over creativity.

### For AI Code Reviewers

**Review checklist:**
2. [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture compliance
3. [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Pattern validation

**Focus areas:** Layer boundaries, immutability, CQRS, events.

### For AI Documentation Assistants

**Reference these:**
1. [ARCHITECTURE.md](ARCHITECTURE.md) - Technical accuracy
2. [examples/](examples/) - Code examples
3. This INDEX - Navigation structure

### For AI Debugging Assistants

**Investigation flow:**
1. [QUICK_REFERENCE.md § Common Errors](QUICK_REFERENCE.md)
2. [examples/testing-patterns.md](examples/testing-patterns.md)
3. [ARCHITECTURE.md](ARCHITECTURE.md) - System understanding

---

## 📖 Learning Paths

### Path 1: Domain Layer Developer

```
1. ARCHITECTURE.md § Domain Layer
2. examples/complete-aggregate.md
3. examples/complete-aggregate.md § Value Objects
4. prompts/new-aggregate.md
5. QUICK_REFERENCE.md § Domain Layer rules
```

### Path 2: Application Layer Developer

```
1. ARCHITECTURE.md § Application Layer
2. examples/complete-use-case.md
3. prompts/new-use-case.md
4. QUICK_REFERENCE.md § Use Case Pattern
```

### Path 3: Infrastructure Layer Developer

```
1. ARCHITECTURE.md § Infrastructure Layer
2. examples/complete-controller.md
3. examples/repository-implementation.md
4. QUICK_REFERENCE.md § Controller & Repository patterns
```

### Path 4: Event-Driven Developer

```
1. ARCHITECTURE.md § Event-Driven Architecture
2. examples/event-driven-communication.md
3. prompts/new-event-listener.md
4. QUICK_REFERENCE.md § Queue Naming
```

### Path 5: Full-Stack Module Developer

```
1. ARCHITECTURE.md (complete read)
2. All examples/ in order
3. prompts/new-module.md
4. Create test module to practice
```

---

## 🔧 Task-Based Navigation

### "I need to add a new field to an aggregate"

1. Review [examples/complete-aggregate.md § Update Methods](examples/complete-aggregate.md)
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
5. Add tests → [examples/testing-patterns.md § REST Tests](examples/testing-patterns.md)

### "I need modules to communicate"

1. Define event in source module
2. Place event in aggregate → [examples/complete-aggregate.md](examples/complete-aggregate.md)
3. Create listener in target → [prompts/new-event-listener.md](prompts/new-event-listener.md)
4. Configure queue/binding
5. Test flow → [examples/testing-patterns.md § Event Tests](examples/testing-patterns.md)

### "I need to create a new module"

1. Follow [prompts/new-module.md](prompts/new-module.md) checklist
2. Reference [examples/](examples/) for each component
3. Mirror structure of `company` or `shipmentorder` modules
4. Validate against [ARCHITECTURE.md](ARCHITECTURE.md)

### "I need to write tests"

1. Domain tests → [examples/testing-patterns.md § Unit Tests](examples/testing-patterns.md)
2. Use case tests → [examples/testing-patterns.md § Integration Tests (Mock)](examples/testing-patterns.md)
3. Full integration → [examples/testing-patterns.md § Full Integration Tests](examples/testing-patterns.md)
4. REST tests → [examples/testing-patterns.md § REST API Tests](examples/testing-patterns.md)

### "I need to review a PR"

1. Use [.github/AI_GUIDELINES.md](/.github/AI_GUIDELINES.md) checklist
2. Verify against [ARCHITECTURE.md](ARCHITECTURE.md) principles
3. Check patterns in [QUICK_REFERENCE.md](QUICK_REFERENCE.md)
4. Ensure tests included

---

## 📊 Documentation Map

```
doc/ai/
├── README.md                           # Start here
├── INDEX.md                            # This file - navigation
├── ARCHITECTURE.md                     # Complete architecture
├── QUICK_REFERENCE.md                  # Quick patterns lookup
│
├── examples/                           # Complete code examples
│   ├── complete-aggregate.md          # Immutable aggregates
│   ├── complete-use-case.md           # WRITE/READ use cases
│   ├── complete-controller.md         # REST controllers
│   ├── repository-implementation.md   # Persistence layer
│   ├── event-driven-communication.md  # Module communication
│   └── testing-patterns.md            # All test types
│
└── prompts/                           # Creation templates
    ├── new-aggregate.md              # Create aggregate
    ├── new-use-case.md               # Create use case
    ├── new-event-listener.md         # Create listener
    └── new-module.md                 # Create module

.github/
├── copilot-instructions.md           # GitHub Copilot config
└── AI_GUIDELINES.md                  # PR review guide
```

---

## 🎯 Quick Decision Tree

```
What do you need to do?
│
├─ Understand the system?
│  └─ Read ARCHITECTURE.md
│
├─ Create something new?
│  ├─ Domain entity? → prompts/new-aggregate.md
│  ├─ Business operation? → prompts/new-use-case.md
│  ├─ Module communication? → prompts/new-event-listener.md
│  └─ Entire module? → prompts/new-module.md
│
├─ Need a code example?
│  └─ Browse examples/ directory
│
├─ Need a quick pattern?
│  └─ Check QUICK_REFERENCE.md
│
├─ Review code?
│  └─ Use .github/AI_GUIDELINES.md
│
└─ Not sure?
   └─ Start with README.md
```

---

## 💡 Pro Tips

### For Maximum Efficiency

1. **Bookmark QUICK_REFERENCE.md** - Use it constantly
2. **Copy-paste from examples/** - Don't reinvent patterns
3. **Use prompts as checklists** - Don't skip steps
4. **Validate every change** - Check against guidelines

### For Best Results

1. **Follow patterns exactly** - Consistency is critical
2. **Test immediately** - Don't accumulate untested code
3. **Document as you go** - Update examples if you find gaps
4. **Ask when unsure** - Better than guessing

---

## 📞 Support

If this documentation doesn't help:

1. Check related docs in `/doc/`
2. Look at reference modules (`company`, `shipmentorder`)
3. Search for similar code in the codebase
4. Ask the development team

---

## 🔄 Keep This Updated

When adding new documentation:
1. Add entry to this INDEX
2. Update README.md if new section
3. Link from QUICK_REFERENCE.md if pattern
4. Cross-link related documents

---

**Quick Links:**
- [README](README.md) | [Architecture](ARCHITECTURE.md) | [Quick Ref](QUICK_REFERENCE.md)
- [Examples](examples/) | [Prompts](prompts/)
- [Copilot Config](/.github/copilot-instructions.md) | [Review Guide](/.github/copilot-instructions.md)

---

**Last Updated:** 2025-11-04
