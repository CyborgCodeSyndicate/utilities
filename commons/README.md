# commons

## Table of contents

- [Overview](#overview)
- [Quick start](#quick-start)
- [Package guide](#package-guide)
- [Logging](#logging)
  - [Static API](#static-api)
  - [Custom levels](#custom-levels)
  - [Runtime controls](#runtime-controls)
  - [Log4j2 configuration](#log4j2-configuration)
  - [Extensibility](#extensibility)
- [Retry utilities](#retry-utilities)
- [Reflection utilities](#reflection-utilities)
  - [Enum discovery](#enum-discovery)
  - [Implementation scanning](#implementation-scanning)
  - [Field access helpers](#field-access-helpers)
- [Configuration helpers](#configuration-helpers)
- [Author](#author)

## Overview

`commons` packages the cross-cutting utilities that back Cyborg Code automations and services. The module stays lightweight while covering the boring-but-essential pieces you reach for in most projects:

- Structured Log4j2 logging with custom levels, markers, and opt-in verbosity flags.
- Resilient retry helpers for polling APIs or waiting on eventual consistency.
- Reflection utilities for discovering implementations, enum constants, and field values in a safe, well-defined way.
- A thin configuration facade that standardizes how Owner-based configs are defined and tagged.

Use it when you want consistent infrastructure primitives without dragging a framework into your build.

## Quick start

Add the dependency (align the version with your BOM or parent):

```xml
<dependency>
  <groupId>io.cyborgcode.utilities</groupId>
  <artifactId>commons</artifactId>
  <version>RELEASE</version>
</dependency>
```

The parent POM manages the versions for:

- `org.apache.logging.log4j:log4j-layout-template-json`
- `org.reflections:reflections`
- `org.aeonbits.owner:owner`
- `org.openjdk.nashorn:nashorn-core`
- `org.mockito:mockito-core` (consumers typically keep it `test` scoped)
- `org.junit.jupiter:junit-jupiter`

## Package guide

| Package | Highlights |
| --- | --- |
| `io.cyborgcode.utilities.logging` | `LogCommon` one-liners backed by `LogCore` and `LogCyborg` for logger/marker management, custom levels, and runtime switches. |
| `io.cyborgcode.utilities.reflections` | `ReflectionUtil` for classpath scanning, enum resolution, and field extraction plus `RetryUtils` for polling with logging. |
| `io.cyborgcode.utilities.reflections.exceptions` | `ReflectionException`, the domain-specific runtime exception surfaced by the reflection helpers. |
| `io.cyborgcode.utilities.config` | `PropertyConfig` and `@ConfigSource` to standardize Owner configuration contracts. |

## Logging

The logging API wraps Log4j2 so you can use structured, marker-aware logging without repeating setup code.

### Static API

<details>
<summary>Show example</summary>

```java
import io.cyborgcode.utilities.logging.LogCommon;

LogCommon.info("Starting job {}", jobId);
LogCommon.warn("Retry #{} due to {}", attempt, reason);
LogCommon.error("Failure: {}", errorMessage);
LogCommon.debug("Payload: {}", payload);
LogCommon.trace("Entering {}", methodName);

// Custom levels
LogCommon.step("Open login page");
LogCommon.extended("Raw response: {}", rawBody);
```

</details>

### Custom levels

| Level | Priority | When to use |
| --- | --- | --- |
| `STEP` | 350 | Trace human-readable progress (especially in tests). |
| `VALIDATION` | 350 | Highlight assertion or verification outcomes  (available via `validationLog()` when extending `LogCore`). |
| `EXTENDED` | 450 | Verbose diagnostics gated behind `extended.logging=true`. |

All custom levels still participate in the same marker-driven logging pipeline.

### Runtime controls

Two JVM system properties gate noisy output:

- `silent.mode` (default `false`): when `true`, suppresses warn/error/debug/trace/step/validation/extended logs.
- `extended.logging` (default `false`): when `true`, allows `LogCommon.extended(...)` to emit.

Enable them via JVM args: 

```bash
-Dsilent.mode=true -Dextended.logging=true
```

### Log4j2 configuration

`commons` does not ship a ready-made `log4j2.xml`. Configure appenders in the consuming app. A minimal JSON console setup:

<details>
<summary>Show minimal configuration</summary>

```xml
<Configuration status="WARN">
  <Appenders>
    <Console name="Console" target="SYSTEM_OUT">
      <JsonTemplateLayout eventTemplateUri="classpath:LogstashJsonEventLayoutV1.json"/>
    </Console>
  </Appenders>
  <Loggers>
    <Logger name="Cyborg.COMMON" level="info" additivity="false">
      <AppenderRef ref="Console"/>
    </Logger>
    <Root level="info">
      <AppenderRef ref="Console"/>
    </Root>
  </Loggers>
</Configuration>
```

</details>

### Extensibility

Need to intercept logs (for example, while unit testing) or redirect them temporarily? Swap the singleton with a custom `LogCommon` instance:

<details>
<summary>Show example</summary>

```java
LogCommon custom = Mockito.mock(LogCommon.class); // or any LogCommon-compatible test double
LogCommon.extend(custom);

// run code that should log...

// restore the original instance when you're done
LogCommon.extend(null);
```

</details>

Passing `null` resets the singleton so the next log call recreates the default implementation. `LogCyborg` exposes helpers to fetch loggers or markers directly if you want finer-grained control without replacing the singleton.

## Retry utilities

`RetryUtils.retryUntil` polls until a predicate is met or time runs out. It validates inputs, emits informative logs, and suppresses the last failure on timeout to keep stack traces available.

<details>
<summary>Show example</summary>

```java
String response = RetryUtils.retryUntil(
    Duration.ofSeconds(10),
    Duration.ofMillis(250),
    () -> api.call(),
    body -> body != null && body.contains("OK")
);
```

</details>

During retries the helper:

- Logs each attempt number.
- Warns on exceptions (with the stack trace at debug level).
- Sleeps the smaller of the configured interval or the remaining time so it never oversleeps the deadline.

Expect an `IllegalStateException` when the predicate is still false at the end; the last exception (if any) is attached as a suppressed cause.

## Reflection utilities

The reflection helpers lean on `org.reflections.Reflections` to traverse the classpath while surfacing clear errors through `ReflectionException`.
Most APIs accept optional package prefixes; when omitted, scanning uses the full base configuration.

### Enum discovery

<details>
<summary>Show example</summary>

```java
List<Class<? extends Enum>> candidates =
    ReflectionUtil.findEnumClassImplementationsOfInterface(MyInterface.class, "com.acme.enums");
```

</details>

Use that list to inspect enum types or convert to constants. A convenience method finds a single constant by name:

<details>
<summary>Show example</summary>

```java
MyInterface constant = ReflectionUtil.findEnumImplementationsOfInterface(
    MyInterface.class, "MY_CONSTANT", "com.acme.enums");
```

</details>

Ambiguous or missing matches raise a `ReflectionException`.

### Implementation scanning

<details>
<summary>Show example</summary>

```java
List<Class<? extends MyInterface>> handlers =
    ReflectionUtil.findImplementationsOfInterface(MyInterface.class, "com.acme.plugins");
```

</details>

Results cover the entire inheritance tree beneath the provided package prefix.
If you need to scan from a custom classpath (for example from a Maven plugin), override the base configuration via `ReflectionUtil.setBaseConfigurationBuilder(...)`.

### Field access helpers

<details>
<summary>Show example</summary>

```java
List<MyType> values = ReflectionUtil.getFieldValues(target, MyType.class);
```

</details>

The helper:

- Walks the full class hierarchy looking for assignable fields.
- Bypasses Java access checks to read private/protected members.

## Configuration helpers

Define strongly typed Owner configs that share a common base and metadata:

<details>
<summary>Show example</summary>

```java
@ConfigSource("app")
public interface AppConfig extends PropertyConfig {
  @Key("base.url")
  @DefaultValue("https://example.org")
  String baseUrl();
}

AppConfig config = ConfigFactory.create(AppConfig.class);
```

</details>

`@ConfigSource` gives you a canonical identifier for wiring or documentation, while `PropertyConfig` keeps all configs aligned on the Owner `Config` interface.

## Author
**Cyborg Code Syndicate 💍👨💻**