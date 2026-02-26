# Session Log

**Date:** 2026-02-26T19:16:12Z  
**Topic:** UUID test initialization infrastructure  
**Requested by:** Leonardo Moreira

## Context

UUID adapter initialization failures in tests after entity refactors. Unit tests don't load Spring context, so DomainUuidProvider never gets initialized.

## Work Completed

**Mouse** — Created AbstractTestBase with @BeforeAll static initialization of TestUuidAdapter. All tests must extend this base class. Pattern documented in TEST_STRUCTURE.md.

**Cypher** — Validated full test suite after fix.

**Scribe** — Logging and decision merge (this session).

## Key Decision

Universal test base class pattern established. ALL tests in TMS must extend AbstractTestBase or AbstractIntegrationTest (which extends it). Initialization happens once per test class via static @BeforeAll.

## Outcome

✅ Test infrastructure fixed. UUID generation works in unit and integration tests.
