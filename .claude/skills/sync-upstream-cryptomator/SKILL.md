---
name: sync-upstream-cryptomator
description: Sync this fork's develop branch with upstream cryptomator/android's develop branch by fetching and merging, resolving conflicts in favor of this fork's customizations. Use when the user asks to sync, merge, or catch up with upstream.
---

# Upstream Sync

This repository (`aiya000/cryptometroid`, formerly `cryptomator-android`) is a fork of
`cryptomator/android` (remote name: `upstream`). It carries local customizations —
application ID (`io.github.aiya000.cryptometroid`), app display name (`CryptoMetroid`),
and related project-name references — that upstream does not have and will keep
conflicting with as upstream evolves.

## Procedure

1. `git fetch upstream`
2. Confirm the current branch is `develop` and the working tree is clean
   (`git status --short`). If not clean, stop and ask the user how to proceed.
3. `git merge upstream/develop`
4. If the merge is clean, stop here and report what came in (`git log
   --oneline HEAD@{1}..HEAD` or similar) — no further action needed.
5. If there are conflicts:
   - **Priority: keep this fork working.** Never resolve a conflict in a way
     that breaks the build or reintroduces the original `org.cryptomator`
     application ID / app name into files this fork intentionally customized
     (currently: `build.gradle`'s `androidApplicationId`,
     `presentation/src/main/res/values/strings.xml`'s `app_name`, `README.md`,
     `fastlane/Fastfile`, `buildsystem/Dockerfile`, the renamed
     `cryptometroid.png` / `fastlane/metadata/io.github.aiya000.cryptometroid.yml`).
   - For conflicts in those customization points, keep this fork's side but
     fold in any *substantive* unrelated changes upstream made in the same
     hunk (e.g. version bumps, unrelated build config) rather than blindly
     taking "ours".
   - For conflicts elsewhere (real feature/logic changes), inspect whether
     upstream's new code overlaps — partially or fully — with a feature this
     fork has already added on top. If it does NOT overlap, take upstream's
     change normally like a regular merge conflict resolution.
   - If it DOES overlap with fork-specific work, **stop and discuss with the
     user before resolving** — describe the overlap and propose a plan
     (e.g. rebase the fork's feature on top of upstream's version, merge the
     two, or keep the fork's version and drop upstream's) rather than
     resolving unilaterally.
6. After conflicts are resolved and the merge is committed, rebuild
   (`./gradlew :presentation:tasks --offline -q` or a fuller build) to verify
   nothing broke before considering the sync done.

## Notes

- Do not touch the OAuth redirect scheme/host (`android:host="org.cryptomator"`
  in the manifests, `appAuthRedirectScheme: 'org.cryptomator.android'` in
  `presentation/build.gradle`) even during conflict resolution — these are
  registered with external OAuth providers for cloud storage login and must
  stay as-is regardless of upstream changes.
- Do not touch the Java package structure (`org.cryptomator.*` across all
  modules) — this fork deliberately keeps it unchanged; only the Android
  `applicationId` and app display name are forked.
- This sync is run on request only, not on a schedule.
