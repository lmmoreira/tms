package br.com.logistics.tms;

import br.com.logistics.tms.commons.domain.id.DomainUuidProvider;
import br.com.logistics.tms.commons.infrastructure.uuid.TestUuidAdapter;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for ALL test types (unit, integration, e2e).
 * Initializes the DomainUuidProvider with a test-specific UuidAdapter.
 * 
 * <p><b>CRITICAL:</b> All test classes MUST extend this class to ensure UUID generation works correctly.</p>
 * 
 * <p>This class handles initialization that is required regardless of test type:
 * <ul>
 *   <li>Unit tests: No Spring context, just domain objects</li>
 *   <li>Integration tests: Full Spring context with testcontainers</li>
 *   <li>E2E tests: Full application testing</li>
 * </ul>
 * 
 * <p>The UuidAdapter initialization happens once per test class via {@code @BeforeAll},
 * ensuring that domain objects can generate IDs in any test environment.</p>
 */
public abstract class AbstractTestBase {

    @BeforeAll
    static void initializeUuidAdapter() {
        DomainUuidProvider.setUuidAdapter(new TestUuidAdapter());
    }
}
