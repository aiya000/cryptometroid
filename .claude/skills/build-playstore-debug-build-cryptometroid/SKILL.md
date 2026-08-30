---
name: build-playstore-debug-build-cryptometroid
description: Build the playstore debug variant of CryptoMetroid, without installing it anywhere. Use when the user asks to build a debug APK, or as a prerequisite invoked by the install skill.
---

# Build a Playstore Debug Build of CryptoMetroid

Produces a debug APK of this fork at
`presentation/build/outputs/apk/playstore/debug/presentation-playstore-debug.apk`.
Does not touch any device — see `install-playstore-debug-build-cryptometroid` for that.

Unlike the release build, the debug build type carries `applicationIdSuffix ".debug"` (see
`presentation/build.gradle`), so it installs as `io.github.aiya000.cryptometroid.debug` and
coexists with the official `org.cryptomator` app and a locally installed release build.
It is also signed automatically by the Android Gradle Plugin using
`signingConfigs.debug` (`presentation/debug.keystore`) — no separate zipalign/sign step is
needed, unlike the release build.

## Environment

Every step below must run with `dangerouslyDisableSandbox: true`. This is not optional:
Gradle writes to `~/.gradle` (wrapper dists, caches, lock files), which the sandbox mounts
read-only. Inside the sandbox the build dies with `Read-only file system` before it starts.

`java` is not on `PATH` in a non-interactive shell (mise is only activated for interactive
shells), so `JAVA_HOME` must be exported explicitly. Resolve it from mise rather than
hardcoding a version:

```bash
export JAVA_HOME="$(mise where java@17)"
export PATH="$JAVA_HOME/bin:$PATH"
```

**Each Bash tool call is a fresh shell** — `export`ed variables from one call do not carry over
to the next. Set `JAVA_HOME` in the same Bash call that runs `./gradlew`, not in an earlier one.

## Preflight

Before building anything, confirm the checked-out branch actually carries the fork's
application ID:

```bash
grep -n 'androidApplicationId =' build.gradle
```

It must read `io.github.aiya000.cryptometroid`. If it still reads `org.cryptomator`, the
rebrand is not present on this branch. Stop, tell the user which branch they are on, and ask
whether to switch to the rebrand branch rather than proceeding.

## Procedure

1. Build the debug APK:

    ```bash
    ./gradlew assemblePlaystoreDebug
    ```

    The build prints a large number of Kotlin/Java deprecation warnings and
    `Multiple substitutions specified in non-positional format` resource warnings. These
    all come from upstream and are expected — only a non-zero exit code is a real failure.

    Note: pipe carefully when tailing the output. `./gradlew ... | tail -n` masks the
    build's exit code unless `set -o pipefail` is set first.

2. Report the built APK's path to whoever invoked this skill:

    ```
    presentation/build/outputs/apk/playstore/debug/presentation-playstore-debug.apk
    ```

## Notes

- Cloud provider API keys (`DROPBOX_API_KEY_DEBUG`, `ONEDRIVE_API_KEY_DEBUG`,
  `PCLOUD_CLIENT_ID_DEBUG`) are read from environment variables at build time. Without them
  the build still succeeds, but the corresponding cloud integrations will not authenticate
  at runtime
