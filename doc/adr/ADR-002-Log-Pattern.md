# ADR-002 Log Pattern

## Status

Accepted

## Context

Evaluate the best framework for logging patterns between Logback and Log4j2. Slf4j is a facade to interact with those frameworks.

## Decision

Both frameworks are equally good, but Logback was chosen because Logback is the default logging framework in Spring Boot decreasing complexity.

- Rolling logging is a must be.
- Async logging is a must be in order to do not decrease performance on mount and unmount of virtual threads.
- Log configuration should be straightforward and thoroughly externalized, utilizing both XML and application.yml files for comprehensive management. Is mandatory the files to become comprehensive and easy to maintain.
  - In order to achieve this, the logback-spring.xml file was created to centralize all logging configurations with external files as appenders. 
    - [logback-spring.xml](..%2F..%2Fsrc%2Fmain%2Fresources%2Flogback-spring.xml) 
    - [logback-spring-appenders-json.xml](..%2F..%2Fsrc%2Fmain%2Fresources%2Flogback-spring-appenders-json.xml)
    - [logback-spring-appenders-text.xml](..%2F..%2Fsrc%2Fmain%2Fresources%2Flogback-spring-appenders-text.xml)
    - [logback-spring-scope.xml](..%2F..%2Fsrc%2Fmain%2Fresources%2Flogback-spring-scope.xml)
  - Logstash Encoder was added for enabling JSON logging, which is essential for log aggregation and analysis in centralized logging systems like ELK Stack.
  - Janino was added to enable tags like if and else in logback-spring.xml to improve readability and maintainability and avoid profile dependent configuration.

## Consequences

- Centralization of configuration.
- Readability and maintainability of log configuration.

## Future

- Externalize log configuration in a other repository and jar dependency and put it on ecosystem pom in order to create a pattern for logging in the company.

## References
- https://faun.pub/java-23-springboot-3-3-4-logback-setup-part-3-c2ffe2d0a358
- https://github.com/arafkarsh/ms-springboot-334-vanilla/
- https://medium.com/javarevisited/effective-logging-strategies-for-java-microservices-081658ce92ac