# Developer miscelanea 

## Environment Set-Up

### Tools
* [SdkMan](https://sdkman.io/)
* [Docker Engine](https://docs.docker.com/engine/install/ubuntu/)
* [Docker Compose](https://docs.docker.com/compose/)
* [Postman](https://www.postman.com/)
* [IntelliJ](https://www.jetbrains.com/pt-br/idea/)

#### IntelliJ Plugins
* [Mermaid](https://plugins.jetbrains.com/plugin/20146-mermaid)
* [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile)
* [Copilot](https://plugins.jetbrains.com/plugin/17718-github-copilot)

### Development
* [`java:21`](https://sdkman.io/usage)
* [`maven:3.9.7`](https://sdkman.io/sdks/#maven)

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
## C4
```mermaid
C4Context
title System Context diagram for TMS

Person(marketplace, "Marketplace", "Shein, Shopee Amazon, etc.")
Person(logistic-provider, "Logistic Provider", "Correios, Loggi, etc.")


Enterprise_Boundary(edge, "Edge Boundary") {
    System(ngix, "NGIX", "NGIX Edge")

    System_Boundary(authentication, "Authentication Boundary") {
        System(Oauth2, "OAuth2-Proxy", "Reverse Proxy to provide authentication using Keycloak as provider")
        System(Keycloak, "Keycloak", "Authentication and Authorization Server")
    }
}

BiRel(marketplace, ngix, "Requests with Api-Key")
BiRel(logistic-provider, ngix, "Requests with Api-Key")
BiRel(ngix, Oauth2, "Validates bearer")
BiRel(Oauth2, Keycloak, "Identity provider")
```