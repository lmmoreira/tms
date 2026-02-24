# HTTP Request Files Guide

## ⚡ TL;DR

- **When:** Manual API testing during development
- **Why:** Quick endpoint testing without Postman, version-controlled
- **Pattern:** `src/main/resources/{module}/request.http` with variables and response handlers
- **See:** Read on for complete structure

---

## Purpose
Create and maintain HTTP request files for manual API testing with IntelliJ HTTP Client.

---

## Location

**Path:** `src/main/resources/{module}/request.http`

**Examples:**
- `src/main/resources/company/request.http`
- `src/main/resources/shipmentorder/request.http`

---

## Basic Structure

```http
@server = http://localhost:8080
@now = {{ $timestamp }}

### Create Resource
POST {{server}}/resources
Content-Type: application/json
Accept: application/json

{
  "field1": "value1",
  "field2": "{{variableFromPreviousRequest}}"
}

> {% client.global.set("resourceId", response.body.id) %}


### Get Resource
GET {{server}}/resources/{{resourceId}}
Accept: application/json


### Update Resource
PUT {{server}}/resources/{{resourceId}}
Content-Type: application/json

{
  "field1": "updated value"
}


### Delete Resource
DELETE {{server}}/resources/{{resourceId}}
```

---

## Key Concepts

### Variables

**Server Variables (Top of file):**
```http
@server = http://localhost:8080
@now = {{ $timestamp }}
@customVar = some-value
```

**Global Variables (Store response data):**
```http
> {% client.global.set("variableName", response.body.fieldName) %}
```

**Use Variables:**
```http
{{server}}        # Defined at top
{{variableName}}  # Stored from response
{{$uuid}}         # Dynamic UUID
{{$timestamp}}    # Current timestamp
```

### Request Separator

Use `###` to separate multiple requests:
```http
### First Request
POST {{server}}/endpoint1

### Second Request
GET {{server}}/endpoint2
```

### Response Handlers

Store response data for later use:
```http
> {% client.global.set("companyId", response.body.companyId) %}
> {% client.global.set("userId", response.body.data.userId) %}
```

---

## TMS Patterns

### Pattern 1: Create and Reference

**Company Module:**
```http
@server = http://localhost:8080

### Create Marketplace Company
POST {{server}}/companies
Content-Type: application/json
Accept: application/json

{
  "name": "Shein",
  "cnpj": "50.617.696/0002-11",
  "types": ["MARKETPLACE"],
  "configuration": {
    "notification": "true",
    "webhook": "http://shein.com.br/logistics/callback"
  }
}

> {% client.global.set("companyId", response.body.companyId) %}


### Create Logistics Provider
POST {{server}}/companies
Content-Type: application/json
Accept: application/json

{
  "name": "Loggi",
  "cnpj": "13.895.286/0001-93",
  "types": ["LOGISTICS_PROVIDER"],
  "configuration": {
    "notification": "true",
    "webhook": "http://loggi.com.br/logistics/callback"
  }
}

> {% client.global.set("shipper", response.body.companyId) %}


### Get Company
GET {{server}}/companies/{{companyId}}
Accept: application/json
```

### Pattern 2: Chained Resources

**ShipmentOrder Module:**
```http
@server = http://localhost:8080
@now = {{ $timestamp }}

### Create ShipmentOrder
POST {{server}}/shipmentorders
Content-Type: application/json
Accept: application/json

{
  "companyId": "{{companyId}}",
  "shipperId": "{{shipper}}",
  "externalId": "externalId_{{$uuid}}",
  "createdAt": "{{now}}"
}

> {% client.global.set("shipmentOrderId", response.body.shipmentOrderId) %}


### Get ShipmentOrders by Company
GET {{server}}/shipmentorders/company/{{companyId}}?page=0&size=10
Accept: application/json
```

### Pattern 3: CRUD Operations

```http
### CREATE
POST {{server}}/resources
Content-Type: application/json

{ "name": "New Resource" }

> {% client.global.set("resourceId", response.body.id) %}


### READ
GET {{server}}/resources/{{resourceId}}


### UPDATE
PUT {{server}}/resources/{{resourceId}}
Content-Type: application/json

{ "name": "Updated Resource" }


### DELETE
DELETE {{server}}/resources/{{resourceId}}
```

---

## Dynamic Values

### Built-in Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `{{$uuid}}` | Random UUID v4 | `550e8400-e29b-41d4-a716-446655440000` |
| `{{$timestamp}}` | Current Unix timestamp | `1699200000000` |
| `{{$randomInt}}` | Random integer | `12345` |

**Usage:**
```http
{
  "externalId": "order_{{$uuid}}",
  "createdAt": "{{$timestamp}}",
  "orderNumber": {{$randomInt}}
}
```

---

## Common Workflows

### Workflow 1: Setup Test Data

**Execution Order:**
1. Run company requests → Creates Shein and Loggi
2. Stored as `{{companyId}}` and `{{shipper}}`
3. Run shipmentorder request → Uses both variables

```http
# Step 1: company/request.http
### Create Shein (companyId)
### Create Loggi (shipper)

# Step 2: shipmentorder/request.http  
### Create ShipmentOrder (uses companyId + shipper)
```

### Workflow 2: Test Relationships

```http
### Create Parent
POST {{server}}/parents
{ "name": "Parent" }
> {% client.global.set("parentId", response.body.id) %}

### Create Child
POST {{server}}/children
{
  "parentId": "{{parentId}}",
  "name": "Child"
}
> {% client.global.set("childId", response.body.id) %}

### Get Parent with Children
GET {{server}}/parents/{{parentId}}/children
```

---

## Best Practices

### Naming Variables

✅ **DO:**
- Use descriptive names: `companyId`, `shipper`, `userId`
- Match domain language: `shipmentOrderId` not `orderId`
- Be consistent across files

❌ **DON'T:**
- Generic names: `id`, `var1`, `temp`
- Abbreviations: `cmpId`, `shpmtOrd`

### Organizing Requests

✅ **DO:**
- Group by functionality (Create, Read, Update, Delete)
- Order logically (setup → usage)
- Add descriptive comments with `###`

❌ **DON'T:**
- Mix unrelated operations
- Random order requiring manual execution

### File Organization

✅ **DO:**
- One file per module: `{module}/request.http`
- Include all CRUD operations
- Document required variables at top

❌ **DON'T:**
- Mix multiple modules in one file
- Create files outside module resources

---

## When to Update

### New Aggregate Created
✅ Create `src/main/resources/{module}/request.http`
✅ Include CREATE and GET requests minimum
✅ Store aggregate ID in variable

### New Field Added to DTO
✅ Update request body in relevant file
✅ If field references another aggregate, use variable

### New Endpoint Added
✅ Add request to appropriate module file
✅ Follow naming pattern: `### {Verb} {Resource}`

### Field Made Optional/Required
✅ Update request bodies to match
✅ Comment if field can be omitted

---

## Example: Adding a New Field

**Scenario:** Add `shipper` field to ShipmentOrder

**Steps:**

1. **Update company/request.http** (if new reference):
```http
### Create Logistics Provider
POST {{server}}/companies
{ "name": "Loggi", "types": ["LOGISTICS_PROVIDER"] }
> {% client.global.set("shipper", response.body.companyId) %}
```

2. **Update shipmentorder/request.http**:
```http
### Create ShipmentOrder
POST {{server}}/shipmentorders
{
  "companyId": "{{companyId}}",
  "shipperId": "{{shipper}}",    # ← NEW FIELD
  "externalId": "{{$uuid}}"
}
```

3. **Test workflow**:
   - Run company requests → Store IDs
   - Run shipmentorder request → Uses stored IDs

---

## Troubleshooting

**Variable not found:**
- Ensure previous request executed successfully
- Check variable name matches exactly
- Verify response handler syntax

**Request fails:**
- Check server is running: `mvn spring-boot:run`
- Verify endpoint path matches controller
- Confirm request body matches DTO

**Cannot reference ID:**
- Check response handler: `{% client.global.set(...) %}`
- Verify response structure matches expected

---

## Related Documentation

- [ARCHITECTURE.md](../ARCHITECTURE.md) - REST patterns
- [QUICK_REFERENCE.md](../QUICK_REFERENCE.md) - API conventions
- Existing files: `src/main/resources/{company,shipmentorder}/request.http`
