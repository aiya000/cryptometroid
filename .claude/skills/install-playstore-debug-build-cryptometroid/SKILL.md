---
name: install-playstore-debug-build-cryptometroid
description: Build the playstore debug variant of CryptoMetroid and install it on the connected device with adb, then launch it. Use when the user asks to install, sideload, or test a debug build on their phone.
---

# Install a Playstore Debug Build of CryptoMetroid

Builds a debug APK of this fork (via the `build-playstore-debug-build-cryptometroid` skill)
and pushes it to the connected Android device. The debug build type carries
`applicationIdSuffix ".debug"`, so this installs as `io.github.aiya000.cryptometroid.debug`
and coexists with both the official `org.cryptomator` app and a locally installed release
build.

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

2. Invoke the `build-playstore-debug-build-cryptometroid` skill (via the Skill tool) to
   produce the debug APK. It handles the environment setup (`JAVA_HOME`), the application-id
   preflight check, and the build, and reports back the APK's path — expected to be
   `presentation/build/outputs/apk/playstore/debug/presentation-playstore-debug.apk`.

3. Install it:

    ```bash
    adb install -r presentation/build/outputs/apk/playstore/debug/presentation-playstore-debug.apk
    ```

4. Confirm coexistence:

    ```bash
    adb shell pm list packages | grep -iE 'cryptomator|cryptometroid'
    ```

5. Launch it, so the user can start testing immediately:

    ```bash
    adb shell am start -n io.github.aiya000.cryptometroid.debug/org.cryptomator.presentation.ui.activity.VaultListActivity
    ```

    If the user is actively debugging a specific issue, it's often useful to clear logcat
    right before this step (`adb logcat -c`) and watch for crashes afterward
    (`adb logcat -d -t 300 | grep -iE 'FATAL|AndroidRuntime|cryptometroid.*(Exception|Error)'`).

## Notes

- Reinstalling over an existing install keeps app data (Vaults, settings, SharedPreferences
  defaults already applied). To test a change to a *default* SharedPreferences value from a
  clean slate, uninstall first: `adb uninstall io.github.aiya000.cryptometroid.debug`, then
  install — `adb install -r` alone will not reset existing preference values
- Cloud provider API keys are read from `*_DEBUG`-suffixed environment variables; see
  `build-playstore-debug-build-cryptometroid` for details
