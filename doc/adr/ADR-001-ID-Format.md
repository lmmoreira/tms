# ADR-001 ID Format

## Status

Accepted

## Context

Evaluate the ID format in the system between auto-increment and UUID options.

## Decision

Due to the nature of the domain model architecture and domain-centric approach used in this project, deciding on the appropriate ID format posed a challenge.

The value object [Id.java](src/main/java/br/com/logistics/tms/commons/domain/Id.java) was created to centralize ID formats across the project.  

- The auto-increment format was discarded because the domain should be protected and independent from the database. Auto-increment IDs are very relational-like, but the key reason for this decision was the high number of database interactions required to obtain ID values, even for non-persisted components.

- UUID was chosen as the preferred option. However, the default Java [UUID](https://docs.oracle.com/javase/7/docs/api/java/util/UUID.html) was rejected since it is not time-based or sequential, which would negatively impact database insert performance due to inefficient B+ tree index balancing.

- Among UUID versions, version 7 (ULID) (universally unique lexicographically-sortable identifiers) was selected to leverage Unix Epoch time combined with random-based components. This ensures the IDs are both time-based and sequential, optimizing lookup speed in database indexes.

## Consequences

- Centralization of UUID generation.
- Calls made within the same millisecond produce very similar values, which may be unsafe in certain environments. Check experimentation.
- Be aware that all version 7 UUIDs may be converted to ULIDs but not all ULIDs may be converted to UUIDs. For that matter, all UUIDs of any version may be encoded as ULIDs, but they will not be monotonically increasing and sortable unless they are version 7 UUIDs. You will also not be able to extract a meaningful timestamp from the ULID, unless it was converted from a version 7 UUID.

## References
- https://uuid.ramsey.dev/en/4.6.0/rfc4122.html
- https://uuid.ramsey.dev/en/4.6.0/rfc4122/version7.html#rfc4122-version7
- https://github.com/cowtowncoder/java-uuid-generator
- https://medium.com/@tecnicorabi/should-you-use-uuids-for-database-keys-597b15b000bb
- https://medium.com/@chirag.softpoint/ulid-universally-unique-lexicographically-sortable-identifier-better-in-many-cases-then-uuid-72de34b66268

## Experimentation

```java 
public class UUIDTest {

    public static void main(String[] args) {
        System.out.println("UUIDTest");

        UUID u1 = Generators.timeBasedEpochGenerator().generate();
        UUID u2 = Generators.timeBasedEpochGenerator().generate();
        UUID u3 = Generators.timeBasedEpochGenerator().generate();

        System.out.println(u1);
        System.out.println(u2);
        System.out.println(u3);

    }

}

Results:

UUIDTest
0191ffc0-dbc4-7598-ad3a-14a54ce393ef
0191ffc0-dbc9-711e-aa26-54b75e5b69de
0191ffc0-dbc9-7e77-bdb7-1f3f6816bd9f
```
