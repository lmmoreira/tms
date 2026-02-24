# Plan 002 Review Summary

**Reviewers:** Morpheus (Lead), Switch (Java), Apoc (Business)  
**Date:** 2026-02-24  
**For:** Leonardo Moreira

---

## You Were Right — UpdateAgreement is Missing

Leonardo identified the critical gap: **no way to update existing agreements**.

### What's Missing

**UpdateAgreement Operations:**
- Extend validity period (change validTo)
- Terminate early (set validTo = now)
- Update conditions (10% → 15% discount)
- Add/remove conditions

**Technical Artifacts:**
- 8 domain methods (Agreement.updateValidTo, Company.updateAgreement, Agreement.overlapsWith, etc.)
- 2 use cases (UpdateAgreementUseCase, GetAgreementByIdUseCase)
- 2 controllers (UpdateAgreementController, GetAgreementController)
- 1 event (AgreementUpdated)
- Test suites for update operations
- Enhanced HTTP scenarios

**Impact:** +9 tasks, +1 day effort (3-4 days total vs 2-3)

---

## Key Additions by Phase

### Phase 1: Domain — ADD 3 TASKS

**Task 1.5: Agreement Lifecycle Methods**
```java
// Agreement.java
public Agreement updateValidTo(Instant newValidTo) {
    // Validation + return new instance
}

public Agreement updateConditions(Set<AgreementCondition> newConditions) {
    // Validation + return new instance
}

public boolean overlapsWith(Agreement other) {
    // Check date range + type + destination overlap
}
```

**Task 1.6: Company Update Method**
```java
// Company.java
public Company updateAgreement(AgreementId id, Agreement updated) {
    // Replace agreement in Set + place AgreementUpdated event + return new Company
}
```

**Task 1.7: Enhance Overlapping Validation**
```java
// Company.addAgreement() — add check
if (agreements.stream().anyMatch(a -> a.overlapsWith(agreement))) {
    throw new ValidationException("Overlapping agreement exists");
}
```

### Phase 2: Events — ADD 1 TASK

**Task 4.3: AgreementUpdated Event**
```java
public class AgreementUpdated extends AbstractDomainEvent {
    private final UUID sourceCompanyId;
    private final UUID agreementId;
    private final String fieldChanged;
    private final String oldValue;
    private final String newValue;
    // Constructor + getters
}
```

### Phase 3: Use Cases — ADD 2 TASKS

**Task 5.4: UpdateAgreementUseCase**
```java
@DomainService
@Cqrs(DatabaseRole.WRITE)
public class UpdateAgreementUseCase {
    public record Input(UUID agreementId, Instant newValidTo, Set<AgreementCondition> conditions) {}
    public record Output(UUID agreementId, String message) {}
    
    // Flow: Find company → find agreement → update agreement → update company → persist
}
```

**Task 5.5: GetAgreementByIdUseCase**
```java
@DomainService
@Cqrs(DatabaseRole.READ)
public class GetAgreementByIdUseCase {
    public record Input(UUID agreementId) {}
    public record Output(UUID agreementId, UUID sourceCompanyId, UUID destinationCompanyId, ...) {}
    
    // Flow: Find company by agreement ID → extract agreement → return details
}
```

### Phase 4: Infrastructure — ADD 2 TASKS

**Task 6.6: UpdateAgreementController + DTO**
```java
@RestController
@RequestMapping("agreements")
@Cqrs(DatabaseRole.WRITE)
public class UpdateAgreementController {
    @PutMapping("/{agreementId}")
    public Object update(@PathVariable UUID agreementId, @RequestBody UpdateAgreementDTO dto) {
        // RestUseCaseExecutor pattern
    }
}

public record UpdateAgreementDTO(Instant validTo, Set<AgreementConditionDTO> conditions) {}
```

**Task 6.7: GetAgreementController**
```java
@RestController
@RequestMapping("agreements")
@Cqrs(DatabaseRole.READ)
public class GetAgreementController {
    @GetMapping("/{agreementId}")
    public Object get(@PathVariable UUID agreementId) {
        // RestUseCaseExecutor pattern
    }
}
```

### Phase 5: Tests — ADD 2 TASKS

**Task 7.5: Domain Tests for Updates**
- Agreement.updateValidTo() returns new instance
- Agreement.updateConditions() returns new instance
- Company.updateAgreement() returns new Company + places event
- Agreement.overlapsWith() edge cases
- Overlapping validation in Company.addAgreement()

**Task 7.6: Integration Test for UpdateAgreement**
- Create agreement
- Update validTo → verify 200 OK + event
- Update conditions → verify 200 OK + changed discount
- Verify AgreementUpdated in outbox

### Phase 6: HTTP Scenarios — ENHANCE Task 8.1

Add to agreement-requests.http:

```http
### UPDATE SCENARIOS

### Extend validity to 2027
PUT {{server}}/agreements/{{shoppeLoggiAgreementId}}
Content-Type: application/json
{
  "validTo": "2027-12-31T23:59:59Z",
  "conditions": [
    {"type": "DISCOUNT_PERCENTAGE", "conditions": {"percentage": 10.0}}
  ]
}

### Increase discount to 15%
PUT {{server}}/agreements/{{shoppeLoggiAgreementId}}
Content-Type: application/json
{
  "validTo": "2027-12-31T23:59:59Z",
  "conditions": [
    {"type": "DISCOUNT_PERCENTAGE", "conditions": {"percentage": 15.0}}
  ]
}

### Terminate early
PUT {{server}}/agreements/{{biqueloLoggiAgreementId}}
Content-Type: application/json
{
  "validTo": "2026-02-24T23:59:59Z",
  "conditions": [...]
}

### QUERY SCENARIOS

### Get by ID
GET {{server}}/agreements/{{shoppeLoggiAgreementId}}

### Get active only
GET {{server}}/companies/{{shoppeId}}/agreements?status=ACTIVE

### NEGATIVE TESTS

### Duplicate (should fail 400)
POST {{server}}/companies/{{shoppeId}}/agreements
{
  "destinationCompanyId": "{{loggiId}}",
  "type": "DELIVERS_WITH",
  ...
}

### Self-agreement (should fail 400)
POST {{server}}/companies/{{shoppeId}}/agreements
{
  "destinationCompanyId": "{{shoppeId}}",  // Same!
  "type": "DELIVERS_WITH",
  ...
}
```

---

## Other Gaps Found

### Important (Should Have)

1. **Agreement Status Enum** — PENDING/ACTIVE/TERMINATED/EXPIRED
2. **Filtered Queries** — GET .../agreements?status=ACTIVE, ?type=DELIVERS_WITH
3. **Business Rule Decision Needed:** Condition Precedence

**Question for Leonardo:**

If Shoppe has two active DELIVERS_WITH agreements with Loggi (10% and 15% discount), which applies?

- **Option A:** Highest discount wins (15%) — customer-friendly
- **Option B:** Most recent wins (15%) — latest negotiation
- **Option C:** Explicit priority field — manual control
- **Option D:** Validation prevents multiples — only one active agreement allowed

**Recommendation:** Option D (simplest) or Option A (customer-friendly)

### Nice-to-Have (Future)

- Agreement templates
- Approval workflows
- Additional condition types (VOLUME_DISCOUNT, PAYMENT_TERMS, etc.)
- Retroactive changes

---

## Updated Effort Estimate

**Original:** 2-3 days  
**New:** 3-4 days  
**Additional:** +1 day  
**New Tasks:** 9 (across all phases)

**Breakdown:**
- Domain methods: 2-3 hours
- Use cases: 2-3 hours
- Controllers: 1-2 hours
- Tests: 3-4 hours
- HTTP scenarios: 1 hour
- **Total: ~8-12 hours**

---

## Implementation Checklist

**Must-Have (Blocks MVP):**
- [ ] Task 1.5 — Agreement lifecycle methods
- [ ] Task 1.6 — Company.updateAgreement()
- [ ] Task 1.7 — Overlapping validation
- [ ] Task 4.3 — AgreementUpdated event
- [ ] Task 5.4 — UpdateAgreementUseCase
- [ ] Task 5.5 — GetAgreementByIdUseCase
- [ ] Task 6.6 — UpdateAgreementController
- [ ] Task 6.7 — GetAgreementController
- [ ] Task 7.5 — Domain tests for updates
- [ ] Task 7.6 — Integration test for updates
- [ ] Task 8.1 — Enhanced HTTP scenarios

**Should-Have (Important):**
- [ ] Agreement status enum
- [ ] Filtered query support
- [ ] Condition precedence decision

**Nice-to-Have (Defer to v2):**
- [ ] Templates
- [ ] Approval workflows
- [ ] Additional condition types

---

## What Each Reviewer Said

**Morpheus (Lead):**
- Plan structure solid, phases ordered correctly
- Missing: UpdateAgreement, direct queries, status lifecycle
- Critical: No way to modify agreements after creation

**Switch (Java):**
- Immutability patterns correct in existing code
- Missing: Company.updateAgreement(), Agreement update methods, overlapsWith helper
- Critical: Without these, UpdateAgreementUseCase can't work immutably

**Apoc (Business):**
- HTTP scenarios tell good stories but incomplete
- Missing: Update operations, negative tests, temporal scenarios
- Critical: No way to extend partnerships or change terms (real-world requirement)
- Need decision: What happens with overlapping agreements?

---

## Recommendation

**The plan is 80% complete.** The missing 20% is critical business functionality.

**Next Steps:**
1. Review this summary
2. Decide on condition precedence rule (Question above)
3. Insert 9 new tasks into plan (see Key Additions section)
4. Update effort estimate to 3-4 days
5. Proceed with enhanced scope

The UpdateAgreement gap affects **4 of 6 phases**. Better to catch it now than after implementation starts.

Ready to enhance the plan?
