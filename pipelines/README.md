# pipelines

## Table of contents

- [Overview](#overview)
- [Quick start](#quick-start)
- [Actions reference](#actions-reference)
  - [Deploy action](#deploy-action)
  - [Deploy to Maven Central action](#deploy-to-maven-central-action)
  - [PR validator action](#pr-validator-action)
  - [Shared actions](#shared-actions)
- [Usage examples](#usage-examples)
- [Configuration](#configuration)
- [Security considerations](#security-considerations)
- [Author](#author)

## Overview

The `pipelines` module provides reusable GitHub Actions workflows. This module contains composite actions that standardize CI/CD operations across all Cyborg Code Syndicate projects, ensuring consistent build, test, security scanning, and deployment processes.

*Note: This module is a packaging-only artifact and does not contain buildable Java code.*

**Key capabilities:**

- **Automated deployment** to GitHub Packages with semantic versioning
- **Maven Central deployment** for tagged releases
- **Comprehensive PR validation** including build, test, security scans, and quality gates
- **Shared utilities** for Maven setup and version management
- **Security-first approach** with OWASP dependency checking and static analysis
- **Quality assurance** through SonarCloud integration and code coverage reporting

## Quick start

Reference these actions in your GitHub workflows by pointing to the specific action path:

<details>
<summary>Show deploy workflow example</summary>

```yaml
# .github/workflows/deploy.yml
name: Deploy
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: CyborgCodeSyndicate/utilities/pipelines/deploy@main
        with:
          version_bump: minor
          github_token: ${{ secrets.GITHUB_TOKEN }}
          nvd_api_key: ${{ secrets.NVD_API_KEY }}
```

</details>

<details>
<summary>Show PR validator workflow example</summary>

```yaml
# .github/workflows/pr-validator.yml
name: PR Validation
on:
  pull_request:
    branches: [main]

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: CyborgCodeSyndicate/utilities/pipelines/pr-validator@main
        with:
          sonar_project_key: your-project-key
          sonar_org: your-org
          sonar_token: ${{ secrets.SONAR_TOKEN }}
          nvd_api_key: ${{ secrets.NVD_API_KEY }}
          github_token: ${{ secrets.GITHUB_TOKEN }}
```

</details>

## Actions reference

### Deploy action

**Path**: `pipelines/deploy/action.yml`

Builds and deploys Maven libraries to GitHub Packages with automated version management and Git tagging.

#### Key features

- Semantic version bumping (major, minor, patch)
- Automated Git tagging for releases
- Selective module deployment
- GitHub Packages integration
- Comprehensive deployment reporting

#### Required inputs

<details>
<summary>Show inputs</summary>

| Input | Description |
|-------|-------------|
| `github_token` | GitHub token for Git operations and package publishing |

</details>

#### Optional inputs

<details>
<summary>Show inputs</summary>

| Input | Default | Description |
|-------|---------|-------------|
| `version_bump` | `none` | Version bump type: `major`, `minor`, `patch`, or `none` |
| `modules` | `""` | Comma-separated Maven modules to deploy |
| `deploy_server_id` | `github` | Maven server ID for deployment |
| `java_version` | `"17"` | Java version to use |

</details>

### Deploy to Maven Central action

**Path**: `pipelines/deploy-maven-central/action.yml`

Deploys selected Maven modules to Maven Central using the `central` Maven profile.

#### Key features

- Deploys from an existing git tag (`v{version}`)
- Builds with `-P central` and then deploys
- Supports selective module deployment (`-pl ... -am`)
- Creates GitHub Releases as prereleases when the version contains `-rc-`

#### Required inputs

<details>
<summary>Show inputs</summary>

| Input | Description |
|-------|-------------|
| `version` | Version to deploy (must match an existing tag, without the `v` prefix) |
| `modules` | Comma-separated Maven modules to deploy |
| `maven_central_username` | Maven Central (Sonatype) username |
| `maven_central_token` | Maven Central (Sonatype) token |
| `gpg_private_key` | Base64-encoded GPG private key used for signing |
| `gpg_passphrase` | GPG passphrase |

</details>

### PR validator action

**Path**: `pipelines/pr-validator/action.yml`

Comprehensive PR validation with build verification, testing, security scanning, and quality reporting.

#### Key features

- Maven build and test execution
- OWASP dependency vulnerability scanning
- SpotBugs/FindSecBugs static analysis
- SonarCloud quality gate integration
- JaCoCo code coverage reporting
- Automated SARIF upload to GitHub Security

#### Required inputs

<details>
<summary>Show inputs</summary>

| Input | Description |
|-------|-------------|
| `sonar_project_key` | SonarCloud project identifier |
| `sonar_org` | SonarCloud organization |
| `sonar_token` | SonarCloud authentication token |
| `nvd_api_key` | NVD API key for OWASP dependency checking |
| `github_token` | GitHub token for PR comments and API access |

</details>

#### Optional inputs

<details>
<summary>Show inputs</summary>

| Input | Default | Description |
|-------|---------|-------------|
| `java_version` | `"17"` | Java version to use |
| `maven_profiles` | `pr-validator` | Maven profiles to activate |
| `skip_tests` | `false` | Skip test execution (not recommended) |

</details>

### Shared actions

#### Setup Maven (`shared/setup-maven`)

Configures Java environment and Maven authentication for GitHub Packages.

**Features:**
- Java/Maven environment setup with caching
- GitHub Packages authentication configuration
- Multi-server Maven settings generation

#### Version Management (`shared/version-management`)

Handles semantic versioning and POM updates.

**Features:**
- Semantic version calculation (major.minor.patch)
- Optional Release Candidate versions via `use_rc=true` (produces `x.y.z-rc-N`)
- Snapshot version generation with unique identifiers
- Branch-based release enforcement
- Automated POM version updates

## Usage examples

### Basic deployment workflow

<details>
<summary>Show example</summary>

```yaml
name: Deploy Library
on:
  push:
    branches: [main]
    paths-ignore: ['**.md', '.gitignore']

jobs:
  deploy:
    runs-on: ubuntu-latest
    permissions:
      contents: write
      packages: write
    steps:
      - uses: CyborgCodeSyndicate/utilities/pipelines/deploy@main
        with:
          version_bump: patch
          github_token: ${{ secrets.GITHUB_TOKEN }}
```

</details>

### Maven Central deployment workflow

<details>
<summary>Show example</summary>

```yaml
name: Deploy to Maven Central
on:
  workflow_dispatch:
    inputs:
      version:
        description: 'Version to deploy (must match an existing git tag. Example: 1.0.0)'
        required: true

jobs:
  deploy-central:
    runs-on: ubuntu-latest
    steps:
      - uses: CyborgCodeSyndicate/utilities/pipelines/deploy-maven-central@main
        with:
          version: ${{ inputs.version }}
          modules: "parent-pom,commons,shared-static-data"
          maven_central_username: ${{ secrets.MAVEN_CENTRAL_USERNAME }}
          maven_central_token: ${{ secrets.MAVEN_CENTRAL_TOKEN }}
          gpg_private_key: ${{ secrets.GPG_PRIVATE_KEY }}
          gpg_passphrase: ${{ secrets.GPG_PASSPHRASE }}
```

</details>

### Advanced PR validation

<details>
<summary>Show example</summary>

```yaml
name: Comprehensive PR Validation
on:
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  validate:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      pull-requests: write
      security-events: write
    steps:
      - uses: CyborgCodeSyndicate/utilities/pipelines/pr-validator@main
        with:
          sonar_project_key: ${{ vars.SONAR_PROJECT_KEY }}
          sonar_org: ${{ vars.SONAR_ORG }}
          sonar_token: ${{ secrets.SONAR_TOKEN }}
          nvd_api_key: ${{ secrets.NVD_API_KEY }}
          github_token: ${{ secrets.GITHUB_TOKEN }}
          maven_profiles: "pr-validator,integration-tests"
          java_version: "17"
```

</details>

## Configuration

### Required secrets

Configure these secrets in your repository settings:

<details>
<summary>Show required secrets</summary>

| Secret | Purpose | Required For |
|--------|---------|--------------|
| `SONAR_TOKEN` | SonarCloud authentication | PR validation |
| `NVD_API_KEY` | OWASP dependency checking | Both actions |
| `GITHUB_TOKEN` | Automatic (GitHub provides) | Both actions |
| `MAVEN_CENTRAL_USERNAME` | Maven Central username | Maven Central deployment |
| `MAVEN_CENTRAL_TOKEN` | Maven Central token | Maven Central deployment |
| `GPG_PRIVATE_KEY` | Base64-encoded GPG private key | Maven Central deployment |
| `GPG_PASSPHRASE` | Passphrase for the GPG key | Maven Central deployment |

</details>

### Maven profiles

The actions expect certain Maven profiles to be configured:

- **`pr-validator`**: Enables security scanning plugins (SpotBugs, OWASP dependency-check)
- **`integration-tests`**: Optional profile for extended testing

### Repository permissions

<details>
<summary>Show permissions snippet</summary>

```yaml
permissions:
  contents: write        # For Git tagging and commits
  packages: write        # For GitHub Packages deployment
  pull-requests: write   # For PR comments and status
  security-events: write # For SARIF uploads
```

</details>

## Security considerations

### Private vs public repositories

- **Public repositories**: Full security scanning with SARIF upload to GitHub Security tab
- **Private repositories**: Security scans run but SARIF results are not uploaded (GitHub limitation)

### API keys and tokens

- Store all sensitive tokens in GitHub Secrets
- Use repository-scoped tokens when possible
- Rotate NVD API keys regularly for rate limit optimization

### Dependency scanning

The OWASP dependency-check plugin requires an NVD API key for optimal performance. Without it, scans may be slower due to rate limiting.

*Quality gates enforce minimum 90% code coverage for both overall project and changed files.*

## Author

**Cyborg Code Syndicate 💍👨💻**
