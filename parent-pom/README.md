# parent-pom

## Table of contents

- [Overview](#overview)
- [Usage](#usage)
- [BOM usage](#bom-usage)
- [Dependency baseline](#dependency-baseline)
- [Build & quality plugins](#build--quality-plugins)
- [Security, reporting & distribution](#security-reporting--distribution)
- [Local workflows](#local-workflows)
- [Maintainer notes](#maintainer-notes)

## Overview

`parent-pom` is the shared Maven parent published as `io.cyborgcode.utilities:parent-pom`. Use it as the parent for every published module; the repo root module is only an aggregator and is not published to Maven Central.

- Standardizes Java 17 compilation and UTF-8 encoding, and enforces Maven 3.8+ / Java 17 via the Enforcer plugin.
- Centralizes dependency versions through BOM imports that cover logging, Spring Boot, serialization, API test clients, browsers, and the full JUnit/Mockito/TestNG stack.
- Pre-configures compiler, test, lint, coverage, reporting, release, and security plugins so child projects only need to declare code.
- Supports Maven Central publishing via the `central` profile. GitHub Packages publishing remains available via the `github` profile when needed.

Use this parent whenever you are building a reusable library, plugin, or service within the Cyborg Code ecosystem. It guarantees that every consumer inherits the same QA gates and publication target.

## Usage

Reference the module as your `<parent>` and keep the version aligned with the repo release tag you consume:

<details>
<summary>Show parent snippet</summary>

```xml
<parent>
  <groupId>io.cyborgcode.utilities</groupId>
  <artifactId>parent-pom</artifactId>
  <version>RELEASE</version>
  <relativePath>../parent-pom</relativePath> <!-- omit when the parent is pulled from GitHub Packages -->
</parent>
```

</details>

## BOM usage

If you cannot inherit from the parent (for example, when another repository already has its own parent), you can import `parent-pom` as a BOM to reuse the Cyborg Code Syndicate dependency versions:

<details>
<summary>Show BOM import snippet</summary>

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.cyborgcode.utilities</groupId>
      <artifactId>parent-pom</artifactId>
      <version>RELEASE</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

</details>

## Dependency baseline

`parent-pom` aggregates the versions for commonly used libraries so that every module compiles and tests against the same stack:

<details>
<summary>Show dependency baseline</summary>

| Area | Highlights |
| --- | --- |
| Logging & configuration | `org.apache.logging.log4j:log4j-bom`, `org.projectlombok:lombok`, `org.reflections:reflections`, `org.aeonbits.owner:owner`, `org.apache.commons:commons-lang3`, `com.google.code.gson:gson`, `org.openjdk.nashorn:nashorn-core`. |
| Spring & runtime | `org.springframework.boot` BOM + `spring-boot-starter`/`spring-boot-starter-test`, `org.apache.httpcomponents.client5:httpclient5`, `commons-io`, `commons-compress`. |
| Serialization & API clients | `tools.jackson:jackson-bom`, `com.jayway.jsonpath:json-path`, `io.rest-assured:rest-assured-bom`, `io.github.classgraph:classgraph`. |
| Test & QA | `org.junit:junit-bom`, the full JUnit Platform (`launcher`, `engine`, `commons`, `jupiter-*`), `org.mockito:*`, `com.h2database:h2`, `org.testng:testng`, `io.qameta.allure:allure-bom`. |
| Automation & browsers | `org.seleniumhq.selenium:selenium-dependencies-bom`, `selenium-java`, `io.github.bonigarcia:webdrivermanager`. |
| Build tooling | `org.ow2.asm:asm(-bom)`, `com.github.spotbugs:spotbugs-annotations`, `org.checkerframework:checker-qual`, `org.codehaus.plexus:*`, `org.apache.maven:maven-(plugin-api|core)`, `com.google.errorprone:error_prone_annotations`. |

</details>

Anything listed above can be used from a child module with just a `<dependency>` declaration. If a library is missing, add it here first so the rest of the ecosystem benefits from the change.

## Build & quality plugins

All critical plugins are defined under `<pluginManagement>` _and_ reiterated in `<build><plugins>` so they are always active in children:

<details>
<summary>Show plugins</summary>

| Plugin | Highlights |
| --- | --- |
| `maven-compiler-plugin` | Compiles with Java 17, emits parameter metadata, enables `-Xlint:unchecked,deprecation`, and forks the compiler for stability. |
| `maven-surefire-plugin` | Runs tests in parallel at the method level (`threadCount=5`) and skips anything tagged with `exclude-from-verify`. |
| `maven-enforcer-plugin` | Requires Maven 3.8+, Java 17, bans duplicate dependency versions, enforces dependency convergence, and blocks SNAPSHOT dependencies outside the `io.cyborgcode` group. |
| `maven-checkstyle-plugin` | Uses the shared `io/cyborgcode/static-data/roa_checks.xml` ruleset, fails on violations, and checks both main and test sources. |
| `maven-javadoc-plugin` & `maven-source-plugin` | Attach aggregated source/javadoc jars, publish docs to `target/javadocs`, and run during `package`/`site`. |
| `dependency-check-maven` | Executes module-level checks plus a parent-only aggregate report, writes JSON/HTML/SARIF outputs, and consumes `dependency-check-suppressions.xml` from the repo root. |
| `jacoco-maven-plugin` | Attaches the JaCoCo agent to every module, enforces 90% instruction coverage, creates per-module HTML reports, and produces an aggregate report in `target/jacoco-aggregate`. |
| `spotbugs-maven-plugin` | Generates XML/HTML/SARIF findings into `target/spotbugs` using the central exclude filter from `shared-static-data`. |
| `maven-resources` / `maven-clean` | Keep resource filtering UTF-8 aligned and ensure clean phases wipe previous artifacts. |
| `maven-release-plugin` | Coordinates `release:prepare` / `release:perform` with `pushChanges=false` so maintainers review and push the commits/tags manually. |

</details>

## Security, reporting & distribution

### OWASP & validator profile

- The `pr-validator` profile simply activates `dependency-check-maven` so CI can run `mvn -pl parent-pom -Ppr-validator verify` without additional flags.
- Provide an `nvd.api.key` via your `~/.m2/settings.xml` (see Maintainer notes) to avoid throttling.
- Suppressions are centralized in `/dependency-check-suppressions.xml`; keep it tight so alerts stay actionable.

### Coverage & static analysis reports

- JaCoCo outputs live under `target/site/jacoco` (per-module) and `target/jacoco-aggregate` (parent). The `jacoco-check` execution fails the build if coverage dips below `jacoco.minimum.coverage` (default `0.90`).
- SpotBugs runs during `verify`, emits SARIF for GitHub Advanced Security, and honors `spotbugs-maven-plugin.excludeFilterFile` from `shared-static-data`.
- The `reporting` section recreates OWASP, JaCoCo, and SpotBugs reports when `mvn site` is invoked so you get navigable HTML under `target/site`.

### Distribution

- Maven Central publishing is enabled via the `central` profile (GPG signing + Central publishing plugin).
- GitHub Packages publishing is available via the `github` profile (auto-activated when `env.GITHUB_PACKAGES_TOKEN` is present) and also adds the internal GitHub-backed repositories used by other Cyborg Code Syndicate assets.

## Local workflows

<details>
<summary>Show commands</summary>

- `mvn -pl parent-pom clean verify` – fast signal that the parent still builds and all default plugins wire up.
- `mvn -pl parent-pom -Ppr-validator verify` – runs the OWASP aggregate report exactly like CI.
- `mvn -pl parent-pom site` – generates the aggregated HTML/SARIF reports under `parent-pom/target/site`.
- `mvn -pl parent-pom -Pcentral deploy` – signs artifacts and publishes via Sonatype Central.
- `mvn -pl parent-pom versions:display-dependency-updates versions:display-plugin-updates` – preview upcoming dependency/plugin bumps.
- `mvn -pl parent-pom help:effective-pom -DforceStdout` – inspect the fully resolved configuration a child project inherits.

</details>

## Maintainer notes

- Properties under `<properties>` are the single source of truth for dependency and plugin versions. Update them here, then open release PRs for the modules that rely on the new versions.
- When OWASP raises noise, prefer tuning `/dependency-check-suppressions.xml` instead of disabling the plugin.
- Configure credentials and the NVD key once in `~/.m2/settings.xml`:

<details>
<summary>Show settings.xml example</summary>

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>GITHUB_USER</username>
      <password>${env.GITHUB_PACKAGES_TOKEN}</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>owasp-api</id>
      <properties>
        <nvd.api.key>${env.NVD_API_KEY}</nvd.api.key>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>owasp-api</activeProfile>
  </activeProfiles>
</settings>
```

</details>
