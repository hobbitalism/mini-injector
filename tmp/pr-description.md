## Summary

Add CI/CD pipelines and Maven publishing configuration for automated snapshot/release deployment to GitHub Packages.

## Changes

- **`gradle.properties`** — centralized version (`1.0.0-SNAPSHOT`), group ID, and GPR URL
- **`build.gradle`** — removed inline group/version; added `maven-publish` + `signing` convention for publishable modules (`core`, `processor`, `runtime`); sources & javadoc JARs; GPG signing gated to CI only
- **`core/build.gradle`**, **`processor/build.gradle`**, **`runtime/build.gradle`** — each declares a `MavenPublication` with distinct artifact ID (`miniinjector-core`, `miniinjector-processor`, `miniinjector-runtime`) and full POM metadata (license, SCM, developer)
- **`processor/build.gradle`** — also fixed `auto-service` annotation processor self-registration (added `annotationProcessor` declaration)
- **`.github/workflows/ci.yml`** — builds on push/PR to `master`; publishes SNAPSHOT on push
- **`.github/workflows/release.yml`** — triggered by `v*` tags; extracts version from tag, signs + publishes release, creates GitHub Release

## How to release

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Required secrets

| Secret | Purpose |
|---|---|
| `GPG_SIGNING_KEY` | Armored PGP private key (optional — signing not mandatory for GitHub Packages) |
| `GPG_SIGNING_PASSWORD` | Key passphrase |

`GITHUB_TOKEN` is auto-provided by GitHub Actions.
