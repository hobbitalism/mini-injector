# MiniInjector — Maven Deployment Plan

**Target registry:** GitHub Packages  
**Date drafted:** 2026-07-15

---

## What gets published

Only the 3 library modules. The `examples` subprojects are not published.

| Artifact ID | Module | Description |
|---|---|---|
| `miniinjector-core` | `core/` | Annotations (`@Component`, `@Inject`, `@Singleton`) and interfaces |
| `miniinjector-processor` | `processor/` | Annotation processor (APT) that generates `*Provider` classes |
| `miniinjector-runtime` | `runtime/` | DI container, `ContainerBuilder`, `ComponentScanner` |

Group ID: `com.github.playernguyen`

---

## Phase 1 — Build file changes

### 1a. Root `build.gradle`
Apply `maven-publish` and `signing` to all publishable subprojects via a shared convention block that excludes `examples:*`.

### 1b. `core/`, `processor/`, `runtime/` — each `build.gradle`
Declare a `MavenPublication` with:
- Distinct artifact ID per module
- Sources JAR (`sourcesJar` task)
- Javadoc JAR (`javadocJar` task)
- Full POM metadata: description, license (MIT), SCM block, developer info

### 1c. `processor/build.gradle` — fix `auto-service` registration
`auto-service` is currently declared as `implementation` only. It must also be listed under `annotationProcessor` so the processor self-registers via `META-INF/services`.

```groovy
// Before
implementation 'com.google.auto.service:auto-service:1.0.1'

// After
implementation 'com.google.auto.service:auto-service:1.0.1'
annotationProcessor 'com.google.auto.service:auto-service:1.0.1'
```

### 1d. `gradle.properties` (new file at root)
Stores group, version, and repository URL. Secrets stay out and are injected via environment variables at CI time.

```properties
group=com.github.playernguyen
version=1.0.0-SNAPSHOT
gpr.url=https://maven.pkg.github.com/playernguyen/MiniInjector
```

---

## Phase 2 — Versioning strategy

Adopt **SemVer**. The version in `gradle.properties` is the default.  
CI overrides it with `-Pversion=x.y.z` on tagged release runs.

| Branch / event | Version format | Example |
|---|---|---|
| Push to `master` | `x.y.z-SNAPSHOT` | `1.0.0-SNAPSHOT` |
| Push of `v*` tag | `x.y.z` (release) | `1.0.0` |

---

## Phase 3 — GitHub Actions CI/CD

### `ci.yml` — runs on every push to `master` and every PR

1. Checkout code
2. Set up JDK 17
3. Run `./gradlew build` (compile + test)
4. Publish SNAPSHOT: `./gradlew publish` with `GITHUB_TOKEN` injected

### `release.yml` — runs on push of a `v*` tag (e.g. `v1.0.0`)

1. Checkout code
2. Set up JDK 17
3. Parse version from tag (`v1.0.0` → `1.0.0`)
4. Run `./gradlew build`
5. Sign and publish release: `./gradlew publish -Pversion=<tag-version>`
6. Create a GitHub Release via `gh release create`

---

## Phase 4 — GPG signing (release only)

GitHub Packages does not mandate signing, but it is recommended for release artifacts.  
The Gradle `signing` plugin reads these from GitHub Actions secrets:

| Secret name | Purpose |
|---|---|
| `GPG_SIGNING_KEY` | Armored (`--armor`) exported private key |
| `GPG_SIGNING_PASSWORD` | Key passphrase |

Signing is gated on the `CI` environment variable so that local `./gradlew publish` (e.g. `publishToMavenLocal`) works without a GPG key.

```groovy
signing {
    required { System.getenv("CI") != null }
    useInMemoryPgpKeys(
        System.getenv("GPG_SIGNING_KEY"),
        System.getenv("GPG_SIGNING_PASSWORD")
    )
    sign publishing.publications
}
```

---

## Phase 5 — Consumer usage

After publishing, consumers add to their `build.gradle`:

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/playernguyen/MiniInjector")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'com.github.playernguyen:miniinjector-runtime:1.0.0'
    annotationProcessor 'com.github.playernguyen:miniinjector-processor:1.0.0'
}
```

> Note: GitHub Packages requires authentication even for public packages. Consumers need a GitHub personal access token with `read:packages` scope stored in their local `~/.gradle/gradle.properties` as `gpr.token`.

---

## Execution order

| Step | Task | Files affected |
|---|---|---|
| 1 | Fix `auto-service` APT registration | `processor/build.gradle` |
| 2 | Add `gradle.properties` | `gradle.properties` (new) |
| 3 | Add `maven-publish` + POM config | root `build.gradle`, `core/`, `processor/`, `runtime/` `build.gradle` |
| 4 | Add CI snapshot workflow | `.github/workflows/ci.yml` (new) |
| 5 | Add release workflow | `.github/workflows/release.yml` (new) |
| 6 | Verify locally | `./gradlew publishToMavenLocal` |
| 7 | Push and verify | GitHub → Packages tab shows artifacts |
