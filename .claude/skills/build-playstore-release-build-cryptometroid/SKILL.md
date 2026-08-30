---
name: build-playstore-release-build-cryptometroid
description: Build, zipalign, and sign the playstore release variant of CryptoMetroid with the debug keystore, without installing it anywhere. Use when the user asks to build a release APK, or as a prerequisite invoked by the install skill.
---

# Build a Playstore Release Build of CryptoMetroid

Produces a signed, installable release APK of this fork at
`presentation/build/outputs/apk/playstore/release/presentation-playstore-release-signed.apk`.
Does not touch any device — see `install-playstore-release-build-cryptometroid` for that.

The `release` build type does **not** get the `.debug` application ID suffix, so the result
installs as `io.github.aiya000.cryptometroid` and coexists with both the official
`org.cryptomator` app and a locally installed `...cryptometroid.debug` build.

## Environment

Every step below must run with `dangerouslyDisableSandbox: true`. This is not optional:

- Gradle writes to `~/.gradle` (wrapper dists, caches, lock files), which the sandbox mounts read-only.
  Inside the sandbox the build dies with `Read-only file system` before it starts

`java` is not on `PATH` in a non-interactive shell (mise is only activated for interactive
shells), so `JAVA_HOME` must be exported explicitly. Resolve it from mise rather than
hardcoding a version:

```bash
export JAVA_HOME="$(mise where java@17)"
export PATH="$JAVA_HOME/bin:$PATH"
```

The Android build tools are needed for `zipalign` and `apksigner`. **Do not hardcode the
version** — it changes when the SDK updates, and a wrong path fails with a bare
`no such file or directory`:

```bash
BUILD_TOOLS="$(ls -d ~/Android/Sdk/build-tools/*/ | sort -V | tail -1)"
export PATH="$BUILD_TOOLS:$PATH"
```

**Each Bash tool call is a fresh shell** — `export`ed variables from one call do not carry over
to the next. `apksigner` is itself a Java program, so if it is invoked from a separate Bash
call than the one that set `JAVA_HOME`, it fails with `exec: java: not found` even though
`JAVA_HOME` was exported correctly earlier in the same skill run. Re-export both `JAVA_HOME`
and the build-tools `PATH` entry in every Bash call that runs `zipalign` or `apksigner`, not
just once at the start — or run steps 1-4 as a single Bash call so the exports stay in scope.

## Preflight

Before building anything, confirm the checked-out branch actually carries the fork's
application ID:

```bash
grep -n 'androidApplicationId =' build.gradle
```

It must read `io.github.aiya000.cryptometroid`. If it still reads `org.cryptomator`, the
rebrand is not present on this branch — building here produces an APK that collides with the
official Cryptomator app instead of coexisting with it. Stop, tell the user which branch they
are on, and ask whether to switch to the rebrand branch rather than proceeding.

## Procedure

1. Build the release APK:

    ```bash
    ./gradlew assemblePlaystoreRelease
    ```

    The build prints a large number of Kotlin/Java deprecation warnings and
    `Multiple substitutions specified in non-positional format` resource warnings. These
    all come from upstream and are expected — only a non-zero exit code is a real failure.

    Note: pipe carefully when tailing the output. `./gradlew ... | tail -n` masks the
    build's exit code unless `set -o pipefail` is set first.

2. Align the unsigned APK:

    ```bash
    OUT=presentation/build/outputs/apk/playstore/release
    zipalign -v -p 4 "$OUT/presentation-playstore-release-unsigned.apk" "$OUT/presentation-playstore-release-aligned.apk"
    ```

3. Sign it with the debug keystore:

    ```bash
    apksigner sign \
      --ks presentation/debug.keystore \
      --ks-key-alias androiddebugkey \
      --ks-pass pass:android \
      --key-pass pass:android \
      --out "$OUT/presentation-playstore-release-signed.apk" \
      "$OUT/presentation-playstore-release-aligned.apk"
    ```

    **Always pass `--ks-pass` / `--key-pass`.** Without them `apksigner` prompts
    interactively, which a non-interactive shell cannot answer, and it fails with the
    misleading `Keystore was tampered with, or password was incorrect`.

    The credentials are not a secret and are not lost — they are the stock Android debug
    keystore values, declared in `presentation/build.gradle`'s `signingConfigs.debug`
    (`keyAlias 'androiddebugkey'`, `storePassword 'android'`, `keyPassword 'android'`).

4. Verify the signature:

    ```bash
    apksigner verify --print-certs "$OUT/presentation-playstore-release-signed.apk"
    ```

    Expect `Signer #1 certificate DN: C=US, O=Android, CN=Android Debug`.

5. Report the signed APK's path to whoever invoked this skill:

    ```
    presentation/build/outputs/apk/playstore/release/presentation-playstore-release-signed.apk
    ```

## Notes

- This signs a *release* build with the *debug* keystore. That is fine for sideloading onto
  the user's own device, but such an APK must never be published to any distribution channel
- Cloud provider API keys (`DROPBOX_API_KEY`, `ONEDRIVE_API_KEY`, `PCLOUD_CLIENT_ID`) are read
  from environment variables at build time. Without them the build still succeeds, but the
  corresponding cloud integrations will not authenticate at runtime

## TODO

- Decide whether a real (non-debug) release keystore should be used for this fork, and where
  it would be stored. If so, replace step 3's credentials with environment variables rather
  than literals
- Consider wrapping steps 1-4 in a script under the repository root, so the skill just calls it
