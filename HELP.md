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
* [Copilot](https://plugins.jetbrains.com/plugin/17718-github-copilot)

### Development
* [`java:21`](https://sdkman.io/usage)
* [`maven:3.9.7`](https://sdkman.io/sdks/#maven)

### Documentation
* [Mermaid](https://mermaid.js.org/intro/getting-started.html)

### Localhost Shortcuts

* [Minio](http://127.0.0.1:9001/login) 
  * tms-user - tms-password
* [RabbitMQ](http://127.0.0.1:15672/)
  * tms - bitnami
* [Keycloak](http://127.0.0.0:8000/) 
  * admin - password
* [Loki](http://127.0.0.0:3100/ready)
* [Grafana](http://127.0.0.0:3000/)
  * admin - admin
* [Promtail](http://127.0.0.1:9080/targets)
* [Neo4J](http://localhost:7474/browser/)


## C4
### Context
```mermaid
C4Context
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
