# Utilities

A comprehensive collection of reusable libraries and tools for the Cyborg Code Syndicate repositories.

## Table of contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Modules](#modules)
- [Quick start](#quick-start)
  - [Using The Parent POM](#using-the-parent-pom)
  - [Adding Individual Modules](#adding-individual-modules)
  - [GitHub Packages Authentication](#github-packages-authentication)
- [Author](#author)

## Overview

The `utilities` library provides the foundational and supporting building blocks shared across the Cyborg Code Syndicate ecosystem. It provides enterprise-grade utilities, CI/CD automation, and standardized tooling that powers modern development workflows.

## Project Structure

```
utilities/
├── commons/                    # Core utilities library
├── shared-static-data/         # Configuration files & resources  
├── pipelines/                  # GitHub Actions workflows
├── parent-pom/                 # Maven parent configuration
├── .github/workflows/          # CI/CD automation
└── pom.xml                     # Root aggregator POM
```

## Modules

| Module | Description | Key Features | Documentation |
|--------|-------------|--------------|---------------|
| `commons` | Cross-cutting utilities for enterprise Java applications | • Structured Log4j2 logging with custom levels<br>• Resilient retry utilities and polling strategies<br>• Safe reflection helpers and classpath scanning<br>• Standardized Owner-based configuration contracts | [📖 README](commons/README.md) |
| `shared-static-data` | Centralized configuration files and resources | • CheckStyle and SpotBugs configurations<br>• Standardized quality metrics and thresholds<br>• Shared templates and configuration files<br>• Essential for code quality consistency | [📖 README](shared-static-data/README.md) |
| `pipelines` | Reusable GitHub Actions for CI/CD automation | • Automated library deployment with semantic versioning<br>• Comprehensive PR validation with security scanning<br>• Maven setup and version management helpers<br>• Quality gates and SonarCloud integration | [📖 README](pipelines/README.md) |
| `parent-pom` | Centralized Maven configuration and dependency management | • Version-managed BOM for 50+ libraries<br>• Pre-configured Maven plugins with sensible defaults<br>• Quality enforcement (CheckStyle, SpotBugs, JaCoCo, OWASP)<br>• Environment-specific build profiles | [📖 README](parent-pom/README.md) |

## Quick start

### Using the Parent POM

```xml
<parent>
  <groupId>io.cyborgcode.utilities</groupId>
  <artifactId>parent-pom</artifactId>
  <version>RELEASE</version>
</parent>
```

### Adding Individual Modules

```xml
<dependency>
  <groupId>io.cyborgcode.utilities</groupId>
  <artifactId>commons</artifactId>
  <version>RELEASE</version>
</dependency>
```

### Maven Central (recommended)

Cyborg Code Syndicate artifacts are published to **Maven Central**, so in most cases you **do not need any GitHub Packages authentication**.
Just add the dependency (Maven Central is used by default).

### GitHub Packages authentication (optional)

GitHub Packages is only needed if you **explicitly** want to consume artifacts from GitHub Packages (e.g., early-access builds like `-SNAPSHOT`, or if you’re mirroring releases there).  
Note: For the **Maven** registry, GitHub Packages downloads are typically **authenticated**, even for public packages. :contentReference[oaicite:0]{index=0}

<details>
<summary>Use GitHub Packages (only if you really need it)</summary>

Add this to your `~/.m2/settings.xml`:

```xml
<servers>
  <server>
    <!-- Must match the <repository><id>...</id> you use in your pom.xml -->
    <id>github-utilities</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_GITHUB_TOKEN</password>
  </server>
</servers>


## Author

**Cyborg Code Syndicate 💍👨💻**

Licensed under Apache-2.0