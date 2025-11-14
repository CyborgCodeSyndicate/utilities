# pipelines

## Table of contents

- [Overview](#overview)
- [Quick start](#quick-start)
- [Actions reference](#actions-reference)
  - [Deploy action](#deploy-action)
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
- **Comprehensive PR validation** including build, test, security scans, and quality gates
- **Shared utilities** for Maven setup and version management
- **Security-first approach** with OWASP dependency checking and static analysis
- **Quality assurance** through SonarCloud integration and code coverage reporting

## Quick start

Reference these actions in your GitHub workflows by pointing to the specific action path:

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

| Input | Description |
|-------|-------------|
| `github_token` | GitHub token for Git operations and package publishing |

#### Optional inputs

| Input | Default | Description |
|-------|---------|-------------|
| `version_bump` | `none` | Version bump type: `major`, `minor`, `patch`, or `none` |
| `modules` | `""` | Comma-separated Maven modules to deploy |
| `deploy_server_id` | `github` | Maven server ID for deployment |
| `java_version` | `"17"` | Java version to use |

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

| Input | Description |
|-------|-------------|
| `sonar_project_key` | SonarCloud project identifier |
| `sonar_org` | SonarCloud organization |
| `sonar_token` | SonarCloud authentication token |
| `nvd_api_key` | NVD API key for OWASP dependency checking |
| `github_token` | GitHub token for PR comments and API access |

#### Optional inputs

| Input | Default | Description |
|-------|---------|-------------|
| `java_version` | `"17"` | Java version to use |
| `maven_profiles` | `pr-validator` | Maven profiles to activate |
| `skip_tests` | `false` | Skip test execution (not recommended) |

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
- Snapshot version generation with unique identifiers
- Branch-based release enforcement
- Automated POM version updates

## Usage examples

### Basic deployment workflow

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

### Advanced PR validation

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

### Multi-module selective deployment

```yaml
- uses: CyborgCodeSyndicate/utilities/pipelines/deploy@main
  with:
    version_bump: minor
    modules: "commons,shared-static-data"
    github_token: ${{ secrets.GITHUB_TOKEN }}
```

## Configuration

### Required secrets

Configure these secrets in your repository settings:

| Secret | Purpose | Required For |
|--------|---------|--------------|
| `SONAR_TOKEN` | SonarCloud authentication | PR validation |
| `NVD_API_KEY` | OWASP dependency checking | Both actions |
| `GITHUB_TOKEN` | Automatic (GitHub provides) | Both actions |

### Maven profiles

The actions expect certain Maven profiles to be configured:

- **`pr-validator`**: Enables security scanning plugins (SpotBugs, OWASP dependency-check)
- **`integration-tests`**: Optional profile for extended testing

### Repository permissions

Ensure your workflow has appropriate permissions:

```yaml
permissions:
  contents: write        # For Git tagging and commits
  packages: write        # For GitHub Packages deployment
  pull-requests: write   # For PR comments and status
  security-events: write # For SARIF uploads
```

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
