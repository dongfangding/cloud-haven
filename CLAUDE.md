# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Cloud Haven (`cloud-haven`) is a Spring Cloud Alibaba microservices framework. It provides common modules for microservice architectures, including API definitions, core services, authentication filters, and gateway configuration.

**Tech Stack:**

- Java 17
- Spring Boot 3.3.5
- Spring Cloud 2025.0.0
- Spring Cloud Alibaba 2025.0.0.0
- Maven

## Build Commands

```bash
# Compile all modules
mvn clean compile

# Package all modules
mvn clean package

# Install to local repository
mvn clean install

# Run tests
mvn test

# View dependency tree
mvn dependency:tree
```

## Module Structure

| Module           | Purpose                                                             |
|------------------|---------------------------------------------------------------------|
| `api`            | Shared DTOs, interfaces, configuration properties                   |
| `core`           | Core implementation: config, service discovery, Feign, thread pools |
| `authentication` | Server-side authentication interceptor (Servlet)                    |
| `gateway`        | Reactive gateway service                                            |

## Authentication Flow

```
Client → Gateway (CloudHavenGatewayFilter) → Backend Service (ServerAuthenticateTokenFilter)
```

**Gateway Filter** (`gateway/.../handler/CloudHavenGatewayFilter.java`):

- Global reactive filter
- Token validation, signature verification
- Mock user support, trace ID generation
- Uses LMAX Disruptor for async logging

**Server Filter** (`authentication/.../filter/ServerAuthenticateTokenFilter.java`):

- `HandlerInterceptor` implementation
- Parses request context with MDC: userId, traceId, clientIp, imei

## Configuration Properties

Prefix: `customizer.cloud.authentication`

| Property            | Description                                                  |
|---------------------|--------------------------------------------------------------|
| `token-header-name` | Header name for access token (default: `access_token`)       |
| `ignores`           | Endpoints that require authentication but bypass token check |
| `open-ignores`      | Completely open endpoints (no auth required)                 |
| `mock`              | Enable mock login (non-production only)                      |
| `sign-enabled`      | Enable request signature verification                        |
| `sign-secret`       | Signature secret key                                         |

## Package Convention

```
com.snowball.cloud.haven.{module}
├── {module}
│   ├── config/     # Auto-configuration classes (XxxAutoConfiguration)
│   ├── handler/    # Request handlers
│   ├── filter/     # Servlet/WebFlux filters
│   ├── util/       # Utility classes
│   └── exception/  # Exception enums
```

## Key Source Files

| File                                                                   | Purpose                       |
|------------------------------------------------------------------------|-------------------------------|
| `gateway/.../handler/CloudHavenGatewayFilter.java`                     | Gateway authentication filter |
| `gateway/.../config/AuthenticationAutoConfiguration.java`              | Gateway auto-config           |
| `authentication/.../filter/ServerAuthenticateTokenFilter.java`         | Server-side auth interceptor  |
| `authentication/.../config/ServerAuthenticationAutoConfiguration.java` | Server auto-config            |
| `api/.../CloudAuthenticationProperties.java`                           | Auth configuration properties |

## Dependency Notes

- Project depends on internal `ddf-common` framework modules (`ddf-common-api`, `ddf-common-mvc`, `ddf-common-core`)
- `api` module defines interfaces only, no implementation
- All version management is in root `pom.xml`

## Code Conventions

- Classes must include `@author` and `@since` Javadoc
- No line-end comments
- Preserve existing comments during refactoring
- Use `BaseCallbackCode` interface for business error codes
- Use `ResponseData<T>` for unified response format
