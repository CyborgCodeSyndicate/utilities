# Utilities

A comprehensive collection of reusable libraries and tools for the Ring of Automation (ROA) framework.

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

The `utilities` library provides the foundational and supporting building blocks shared across the Cyborg Code Syndicate ecosystem. It provides enterprise-grade utilities, CI/CD automation, and standardized tooling that powers modern test automation and development workflows.

## Project Structure

```
utilities/
├── commons/                    # Core utilities library
├── shared-static-data/         # Configuration files & resources  
├── pipelines/                  # GitHub Actions workflows
├── parent-pom/                 # Maven parent configuration
├── .github/workflows/          # CI/CD automation
├── dependency-check-suppressions.xml
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
  <version>1.3.1</version>
</parent>
```

### Adding Individual Modules

```xml
<dependency>
  <groupId>io.cyborgcode.utilities</groupId>
  <artifactId>commons</artifactId>
  <version>1.3.1</version>
</dependency>
```

### GitHub Packages Authentication

Add to your `~/.m2/settings.xml`:

```xml
<servers>
  <server>
    <id>github-utilities</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_GITHUB_TOKEN</password>
  </server>
</servers>
```

## Author

**Cyborg Code Syndicate 💍👨💻**