# shared-static-data

## Table of contents

- [Overview](#overview)
- [Structure](#structure)
- [Checkstyle rules (`roa_checks.xml`)](#checkstyle-rules-roa_checksxml)
- [IDE codestyle (`roa_codestyle.xml`)](#ide-codestyle-roa_codestylexml)
- [SpotBugs excludes (`spotbugs-exclude.xml`)](#spotbugs-excludes-spotbugs-excludexml)
- [How to consume the resources](#how-to-consume-the-resources)
- [Local validation](#local-validation)

## Overview

`shared-static-data` is a thin JAR that bundles the static quality assets (Checkstyle rules, IDE codestyle, and SpotBugs exclude filters) used across the Cyborg Code utilities ecosystem. Shipping them as a versioned artifact keeps every module and IDE on the same rule set, and lets the `parent-pom` reference the resources via classpath lookups instead of hardcoding file paths.

## Structure

All files live under `io/cyborgcode/static-data/` so Maven plugins and IDEs can resolve them from the classpath:

<details>
<summary>Show files</summary>

| File | Purpose |
| --- | --- |
| `roa_checks.xml` | Primary Checkstyle ruleset executed by `maven-checkstyle-plugin` and CI gates. |
| `roa_codestyle.xml` | IntelliJ-based codestyle preset so editors format code consistently with the Checkstyle expectations. |
| `spotbugs-exclude.xml` | Minimal SpotBugs filter that suppresses `EI_EXPOSE_REP*` false positives when the exposure is intentional. |

</details>

## Checkstyle rules (`roa_checks.xml`)

Highlights from the ruleset:

<details>
<summary>Show highlights</summary>

- Applies to Java, XML, and properties files with UTF-8 encoding.
- Enforces 120-character line length (imports, packages, and URLs are exempted).
- TreeWalker checks cover class/file naming, require braces, disallow star imports, and regulate curly brace placement through paired `LeftCurly` / `RightCurly` modules plus XPath-based suppressions for switch expressions.
- Style consistency checks include whitespace rules, empty-line separators, array style, modifier ordering, and one-statement-per-line enforcement.
- Code safety checks catch `fallthrough`, missing `default` cases, duplicate literals, cyclomatic complexity breaches, and improper exception handling patterns.
- Javadoc coverage is verified on public `CLASS_DEF`, `INTERFACE_DEF`, `ENUM_DEF`, `METHOD_DEF`, and `CTOR_DEF` elements.

</details>

Any additions should continue to prefer explicit modules with clear messages so CI failures are easy to interpret.

## IDE codestyle (`roa_codestyle.xml`)

<details>
<summary>Show highlights</summary>

- Configures IntelliJ IDEA (`.xml` export) with wide import thresholds (999) to discourage wildcard imports.
- Aligns binary/ternary/assignment wrapping to "wrap if long" (value `1`) with the operator on the next line to reduce merge conflicts.
- Specifies three-space indentation (and six-space continuation) to match the Checkstyle expectations for whitespace enforcement.
- Set `SPACE_BEFORE_ARRAY_INITIALIZER_LBRACE` to true so inline collections stay readable.

</details>

To use it, import the scheme via *Settings -> Editor -> Code Style -> Java -> Gear Icon -> Import Scheme*, then select the file from your local Maven repository (`~/.m2/repository/io/cyborgcode/utilities/shared-static-data/<version>/...`).
- Validate Checkstyle changes by running `mvn checkstyle:check` on at least one representative module to catch regressions early.

## SpotBugs excludes (`spotbugs-exclude.xml`)

The filter is intentionally tiny so new suppressions stand out in code review:

<details>
<summary>Show details</summary>

- Matches `EI_EXPOSE_REP` and `EI_EXPOSE_REP2`, allowing intentional defensive-copy opt-outs when immutable wrappers are unnecessary or performance-critical.
- Shared via the `spotbugs-maven-plugin.excludeFilterFile` property in `parent-pom/pom.xml`, so every module runs SpotBugs with the same baseline.

</details>

Keep additional exclusions scoped to the consuming module whenever possible; only promote them here when the same issue appears across multiple libraries.

## How to consume the resources

### Via the parent POM

The parent already configures everything for you:

- Checkstyle plugin reads `io/cyborgcode/static-data/roa_checks.xml` from this artifact on the classpath.
- SpotBugs plugin points to `io/cyborgcode/static-data/spotbugs-exclude.xml`.
- `shared-static-data.version` (defined in the parent) should match the artifact version you depend on.

### Direct dependency (if not using the parent)

<details>
<summary>Show dependency snippet</summary>

```xml
<dependency>
  <groupId>io.cyborgcode.utilities</groupId>
  <artifactId>shared-static-data</artifactId>
  <version>RELEASE</version>
</dependency>
```

</details>

Then reference the resources via `classpath:io/cyborgcode/static-data/...` in your plugin configuration.

## Local validation

<details>
<summary>Show commands</summary>

- `mvn -pl shared-static-data clean package` – repackages the resources and ensures the module stays consumable as a JAR.
- `mvn -pl parent-pom checkstyle:check` – exercises the Checkstyle rules against the codebase using the packaged file.
- `mvn -pl parent-pom spotbugs:check` – confirms the SpotBugs exclude file is reachable and correctly formatted.

</details>
