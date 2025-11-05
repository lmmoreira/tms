# Examples Index - Pattern Navigator

**Purpose:** Quick navigation to focused code examples. Each file contains one specific pattern in <150 lines.

---

## 🎯 Quick Find

**Need a specific pattern?** Use the table below to jump directly to the example you need.

| What You Need | File | Lines |
|---------------|------|-------|
| **Create aggregate** | [aggregate-creation.md](aggregate-creation.md) | ~100 |
| **Update aggregate (immutable)** | [aggregate-update-immutable.md](aggregate-update-immutable.md) | ~100 |
| **Place domain events** | [aggregate-with-events.md](aggregate-with-events.md) | ~80 |
| **Value objects** | [value-object-pattern.md](value-object-pattern.md) | ~60 |
| **WRITE use case (CREATE)** | [usecase-write-create.md](usecase-write-create.md) | ~100 |
| **WRITE use case (UPDATE)** | [usecase-write-update.md](usecase-write-update.md) | ~100 |
| **READ use case (by ID)** | [usecase-read-byid.md](usecase-read-byid.md) | ~80 |
| **READ use case (query)** | [usecase-read-query.md](usecase-read-query.md) | ~100 |
| **POST controller** | [controller-post.md](controller-post.md) | ~100 |
| **GET controller** | [controller-get.md](controller-get.md) | ~80 |
| **PUT controller** | [controller-put.md](controller-put.md) | ~90 |
| **DELETE controller** | [controller-delete.md](controller-delete.md) | ~70 |
| **Repository interface** | [repository-interface.md](repository-interface.md) | ~80 |
| **Repository implementation (write)** | [repository-impl-write.md](repository-impl-write.md) | ~120 |
| **Repository implementation (read)** | [repository-impl-read.md](repository-impl-read.md) | ~100 |
| **JPA entity mapping** | [repository-jpa-entity.md](repository-jpa-entity.md) | ~100 |
| **Define domain event** | [event-definition.md](event-definition.md) | ~60 |
| **Place events in aggregate** | [event-placement.md](event-placement.md) | ~80 |
| **Event listener** | [event-listener.md](event-listener.md) | ~100 |
| **Outbox pattern** | [event-outbox.md](event-outbox.md) | ~100 |
| **Test events** | [event-testing.md](event-testing.md) | ~100 |
| **Unit test (domain)** | [testing-unit-domain.md](testing-unit-domain.md) | ~100 |
| **Unit test (use case)** | [testing-unit-usecase.md](testing-unit-usecase.md) | ~100 |
| **Integration test** | [testing-integration.md](testing-integration.md) | ~150 |
| **REST API test** | [testing-rest-api.md](testing-rest-api.md) | ~120 |
| **Event test** | [testing-events.md](testing-events.md) | ~130 |

---

## 📂 By Category

### Aggregate Patterns
1. [aggregate-creation.md](aggregate-creation.md) - Factory methods, creation
2. [aggregate-update-immutable.md](aggregate-update-immutable.md) - Immutable updates
3. [aggregate-with-events.md](aggregate-with-events.md) - Event placement
4. [value-object-pattern.md](value-object-pattern.md) - Value objects

### Use Case Patterns
5. [usecase-write-create.md](usecase-write-create.md) - CREATE operations
6. [usecase-write-update.md](usecase-write-update.md) - UPDATE operations
7. [usecase-read-byid.md](usecase-read-byid.md) - Read by ID
8. [usecase-read-query.md](usecase-read-query.md) - Query/search

### Controller Patterns
9. [controller-post.md](controller-post.md) - POST endpoints
10. [controller-get.md](controller-get.md) - GET endpoints
11. [controller-put.md](controller-put.md) - PUT endpoints
12. [controller-delete.md](controller-delete.md) - DELETE endpoints

### Repository Patterns
13. [repository-interface.md](repository-interface.md) - Repository contract
14. [repository-impl-write.md](repository-impl-write.md) - Write ops + outbox
15. [repository-impl-read.md](repository-impl-read.md) - Read operations
16. [repository-jpa-entity.md](repository-jpa-entity.md) - JPA mapping

### Event Patterns
17. [event-definition.md](event-definition.md) - Event structure
18. [event-placement.md](event-placement.md) - Where to place events
19. [event-listener.md](event-listener.md) - Listener implementation
20. [event-outbox.md](event-outbox.md) - Outbox pattern
21. [event-testing.md](event-testing.md) - Testing events

### Testing Patterns
22. [testing-unit-domain.md](testing-unit-domain.md) - Domain unit tests
23. [testing-unit-usecase.md](testing-unit-usecase.md) - Use case tests
24. [testing-integration.md](testing-integration.md) - Integration tests
25. [testing-rest-api.md](testing-rest-api.md) - REST API tests
26. [testing-events.md](testing-events.md) - Event testing

---

## 📘 Complete Examples (Legacy)

These files contain comprehensive examples but are longer (300-500 lines):

- [complete-aggregate.md](complete-aggregate.md) - All aggregate patterns
- [complete-use-case.md](complete-use-case.md) - All use case patterns
- [complete-controller.md](complete-controller.md) - All controller patterns
- [repository-implementation.md](repository-implementation.md) - Complete repository
- [event-driven-communication.md](event-driven-communication.md) - Complete event flow
- [testing-patterns.md](testing-patterns.md) - All testing strategies

**Recommendation:** Use focused files above for faster access. Use complete files only when you need to understand the full context.

---

## 🎓 Learning Path

**New to TMS patterns?** Follow this order:

1. Start with [aggregate-creation.md](aggregate-creation.md)
2. Then [value-object-pattern.md](value-object-pattern.md)
3. Then [usecase-write-create.md](usecase-write-create.md)
4. Then [controller-post.md](controller-post.md)
5. Then [repository-interface.md](repository-interface.md)
6. Finally [event-placement.md](event-placement.md)

**Already know the basics?** Jump directly to what you need using the quick find table above.

---

## 💡 Usage Tips

### For AI Assistants
- **Load only what you need** - Each focused file is <150 lines
- **Combine patterns** - Load 2-3 specific files instead of one large file
- **Quick validation** - Use for checking specific patterns during code review

### For Humans
- **Quick reference** - Find specific pattern without scrolling through large files
- **Copy-paste ready** - Each example is complete and runnable
- **Learning** - Focused examples easier to understand than comprehensive guides

---

**Related:** [../START_HERE.md](../START_HERE.md) | [../QUICK_REFERENCE.md](../QUICK_REFERENCE.md) | [../ARCHITECTURE.md](../ARCHITECTURE.md)

**Last Updated:** 2025-11-05
