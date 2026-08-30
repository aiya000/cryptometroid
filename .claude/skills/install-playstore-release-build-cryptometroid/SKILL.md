---
name: install-playstore-release-build-cryptometroid
description: Build the playstore release variant of CryptoMetroid and install it on the connected device with adb. Use when the user asks to install, sideload, or deploy a release build to their phone.
---

# Install a Playstore Release Build of CryptoMetroid

Builds a signed release APK of this fork (via the `build-playstore-release-build-cryptometroid`
skill) and pushes it to the connected Android device. The `release` build type does **not**
get the `.debug` application ID suffix, so this installs as `io.github.aiya000.cryptometroid`
and coexists with both the official `org.cryptomator` app and a locally installed
`...cryptometroid.debug` build.

## Environment

`adb` inside the sandbox starts its **own** daemon in an isolated network namespace, so
`adb devices` reports an empty list even when the device is connected. Every `adb` call below
must run with `dangerouslyDisableSandbox: true`.

## Procedure

1. Confirm a device is connected:

    ```bash
    adb devices -l
    ```

    If the list is empty, stop and ask the user to connect the device (or re-pair it, for
    a wireless connection) before continuing.

2. Invoke the `build-playstore-release-build-cryptometroid` skill (via the Skill tool) to
   produce the signed APK. It handles the environment setup (`JAVA_HOME`, build-tools),
   the application-id preflight check, the build, zipalign, and signing, and reports back the
   signed APK's path — expected to be
   `presentation/build/outputs/apk/playstore/release/presentation-playstore-release-signed.apk`.

3. Install it:

    ```bash
    adb install -r "$OUT/presentation-playstore-release-signed.apk"
    ```

4. Confirm coexistence:

    ```bash
    adb shell pm list packages | grep -iE 'cryptomator|cryptometroid'
    ```

    Report which packages are present to the user.

## Notes

- This installs a *release* build signed with the *debug* keystore. That is fine for
  sideloading onto the user's own device, but such an APK must never be published to any
  distribution channel
- Reinstalling over an existing install keeps app data. If the signature ever changes
  (e.g. switching to a real keystore), `adb install -r` fails with
  `INSTALL_FAILED_UPDATE_INCOMPATIBLE` and the old install has to be uninstalled first
