# TMS

## Overview
A Transportation Management System (TMS) designed to deliver key functionalities for efficient logistics and transportation operations:
- **Quotation**: Generate and manage transportation cost estimates for various services and routes.
- **Order Management**: Oversee and process transportation orders from initiation to completion, including order creation, tracking, and updates.
- **Volume Tracking**: Monitor and manage the volume of shipments, ensuring accurate tracking and reporting of cargo throughout its journey.
- **Integration Between Actors**: Facilitate seamless communication and data exchange between various stakeholders, such as shippers, carriers, and customers, to streamline operations and enhance coordination.

## High Level

```mermaid
graph TD
    Client((Client))
    PostgreSQL[(Database)]
    RabbitMQ[[RabbitMQ]]
    
    Client -->|Sends Request| NGINX
    NGINX -->|Validated API Key| TMS
    NGINX -->|Returns Response| Client

    NGINX -->|Forward Bearer Token| OAuth2-Proxy
    OAuth2-Proxy -->|Forward Bearer Token| Keycloak
    Keycloak -->|Verify Token and Authenticate| OAuth2-Proxy
    OAuth2-Proxy -->|Send Authentication Response| NGINX
    NGINX -->|Forward Authenticated Request| TMS
    
    subgraph "TMS Modules"
        TMS -->|Accesses| CompanyModule
        TMS -->|Accesses| OrderModule
    end

    CompanyModule -->|domain-event| RabbitMQ
    OrderModule -->|domain-event| RabbitMQ
    CompanyModule -->|persistence| PostgreSQL
    OrderModule -->|persistence| PostgreSQL
```

## To Engineers
For more detailed and technical information, please refer to the [HELP.md](HELP.md) file.
