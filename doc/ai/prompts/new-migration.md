# Database Migration Template

## ⚡ TL;DR

- **When:** Adding/modifying database schema (tables, columns, indexes)
- **Why:** Version-controlled, sequential schema evolution via Flyway
- **Pattern:** `V{N}__{description}.sql` in `/infra/database/migration/`
- **See:** Read on for SQL templates

---

## Purpose
Create a new database migration to modify the database schema.

---

## Steps

### 1. Check Existing Migrations
```bash
ls infra/database/migration/
```
Output example: `V1__create_schema.sql`, `V2__create_company.sql`, ..., `V6__create_shipment_order_outbox.sql`

**Determine next version:** Last version + 1 (e.g., if V6 exists, create V7)

---

### 2. Create Migration File

**Naming Convention:** `V{number}__{objective_description}.sql`

**Examples:**
- `V7__add_shipper_to_shipment_order.sql`
- `V8__create_carrier_table.sql`
- `V9__add_index_to_company_cnpj.sql`

**Location:** `/infra/database/migration/V{N}__{description}.sql`

---

### 3. Write Migration SQL

#### Add Column
```sql
ALTER TABLE {schema}.{table_name} ADD COLUMN {column_name} {data_type} {constraints};
```

**Example:**
```sql
ALTER TABLE shipmentorder.shipment_order ADD COLUMN shipper UUID NOT NULL;
```

#### Create Table
```sql
CREATE TABLE {schema}.{table_name} (
    id UUID NOT NULL,
    field1 VARCHAR(255) NOT NULL,
    field2 TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);
```

**Example:**
```sql
CREATE TABLE company.carrier (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);
```

#### Create Index
```sql
CREATE INDEX idx_{table}_{column} ON {schema}.{table}({column});
```

**Example:**
```sql
CREATE INDEX idx_company_cnpj ON company.company(cnpj);
```

#### Add Constraint
```sql
ALTER TABLE {schema}.{table} ADD CONSTRAINT {constraint_name} 
    FOREIGN KEY ({column}) REFERENCES {other_schema}.{other_table}(id);
```

**Example:**
```sql
ALTER TABLE shipmentorder.shipment_order ADD CONSTRAINT fk_shipper 
    FOREIGN KEY (shipper) REFERENCES company.company(id);
```

#### Modify Column
```sql
ALTER TABLE {schema}.{table} ALTER COLUMN {column} TYPE {new_type};
ALTER TABLE {schema}.{table} ALTER COLUMN {column} SET NOT NULL;
ALTER TABLE {schema}.{table} ALTER COLUMN {column} DROP NOT NULL;
```

**Example:**
```sql
ALTER TABLE company.company ALTER COLUMN name TYPE VARCHAR(500);
```

#### Drop Column
```sql
ALTER TABLE {schema}.{table} DROP COLUMN {column};
```

**Example:**
```sql
ALTER TABLE company.company DROP COLUMN old_field;
```

---

### 4. Apply Migration

Migrations are applied automatically by Flyway container:

```bash
# Start infrastructure (applies pending migrations)
docker compose up

# Check migration logs
docker compose logs tms-flyway
```

---

## Common Data Types

| Type | Usage |
|------|-------|
| `UUID` | IDs (aggregates, entities) |
| `VARCHAR(n)` | Short text (names, codes) |
| `TEXT` | Long text (descriptions, notes) |
| `TIMESTAMP WITH TIME ZONE` | Dates and times |
| `BOOLEAN` | True/false flags |
| `INTEGER` | Whole numbers |
| `DECIMAL(p,s)` | Precise decimals (money, measurements) |
| `JSONB` | JSON data (flexible structures) |

---

## Common Constraints

| Constraint | Syntax |
|------------|--------|
| `NOT NULL` | `{column} {type} NOT NULL` |
| `UNIQUE` | `{column} {type} UNIQUE` |
| `DEFAULT` | `{column} {type} DEFAULT {value}` |
| `PRIMARY KEY` | `PRIMARY KEY (column)` |
| `FOREIGN KEY` | `FOREIGN KEY (column) REFERENCES table(id)` |

---

## Examples from TMS

### V3 - Create Partitioned Table
```sql
CREATE TABLE shipmentorder.shipment_order (
    id UUID NOT NULL,
    is_archived BOOLEAN DEFAULT FALSE NOT NULL,
    company_id UUID NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id, is_archived)
) PARTITION BY LIST (is_archived);

CREATE TABLE shipmentorder.shipment_order_active 
    PARTITION OF shipmentorder.shipment_order FOR VALUES IN (false);
    
CREATE TABLE shipmentorder.shipment_order_archived 
    PARTITION OF shipmentorder.shipment_order FOR VALUES IN (true);

CREATE INDEX idx_shipment_order_active_company_id 
    ON shipmentorder.shipment_order_active(company_id);
```

### V7 - Add Column
```sql
ALTER TABLE shipmentorder.shipment_order ADD COLUMN shipper UUID NOT NULL;
```

---

## Best Practices

✅ **DO:**
- Use sequential version numbers (V1, V2, V3, ...)
- Use clear, objective descriptions in filenames
- Add indexes for frequently queried columns
- Use `NOT NULL` when appropriate
- Include schema name in table references
- Check existing migrations to understand table structures

❌ **DON'T:**
- Never modify existing migrations (create new ones)
- Don't skip version numbers
- Don't use vague descriptions (e.g., `V7__update.sql`)
- Don't forget to specify schema
- Don't add indexes unnecessarily (impacts write performance)

---

## Troubleshooting

**Migration fails:**
1. Check logs: `docker compose logs tms-flyway`
2. Verify SQL syntax
3. Ensure referenced tables/schemas exist
4. Check for constraint violations (e.g., adding NOT NULL to existing data)

**Need to rollback:**
1. Create a new migration to undo changes (e.g., `DROP COLUMN`)
2. Flyway tracks applied migrations - don't delete files

**Check migration status:**
```bash
# View Flyway logs
docker compose logs tms-flyway

# Connect to database
docker compose exec tms-database psql -U tms -d tms
\dt  -- list tables
\d {table_name}  -- describe table
```

---

## Related Documentation

- [ARCHITECTURE.md](../ARCHITECTURE.md) - Overall architecture
- [QUICK_REFERENCE.md](../QUICK_REFERENCE.md) - Quick commands
- Existing migrations: `/infra/database/migration/`
