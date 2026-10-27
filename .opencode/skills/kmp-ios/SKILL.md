---
name: kmp-ios
description: Use for the opencode-mobile iOS app — iosApp Xcode project, Info.plist and permissions, the Shared framework and MainViewController, Compose Multiplatform behavior on iOS (keyboard, insets, accessibility, lifecycle), Keychain and signing, ATS, Darwin Ktor engine, building on the Mac and runtime verification on the iOS simulator.
---

# iOS App

The iOS app is a thin SwiftUI host (`iosApp/iosApp/iOSApp.swift`) around the shared
Compose UI returned by `MainViewController()` in
`android/shared/src/iosMain`. Almost all behavior lives in Kotlin.

## Integration

- Direct integration: the Xcode build phase runs
  `./gradlew :shared:embedAndSignAppleFrameworkForXcode` from `android/` (needs
  `JAVA_HOME`). The framework is static, named `Shared`; nothing is exported to
  Swift. No CocoaPods, no SwiftPM, no XCFramework.
- Swift only hosts `ComposeView` with `.ignoresSafeArea()`; Compose handles insets.
  SKIE or Swift export are not used: add them only when Swift code must consume
  Kotlin `Flow` or `suspend` APIs.
- Targets: `iosArm64` (devices) and `iosSimulatorArm64` (Apple Silicon simulators).
  No `iosX64`.
- Koin starts once in `MainViewController()` with `dataModule` and `sharedModule`.
- `Info.plist` owns permission strings (camera, microphone, speech recognition,
  photo library, local network) and ATS: `NSAllowsArbitraryLoads` because servers
  are self-hosted over plain HTTP on LAN/VPN, like Android's cleartext policy.
- Simulator builds are ad-hoc signed (`CODE_SIGN_IDENTITY[sdk=iphonesimulator*] =
  "-"`): an unsigned app cannot use the Keychain (`errSecMissingEntitlement`,
  -34018). Device builds need `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`.
- `MARKETING_VERSION` lives in `Config.xcconfig` and is bumped by release-please with
  `version.txt`; do not set it in `project.pbxproj`.

## Compose On iOS

- Keyboard: `ComposeUIViewController(configure = { onFocusBehavior =
  OnFocusBehavior.DoNothing })` because screens use `imePadding()`; the default
  behavior shifts the focused field a second time.
- iOS has no Back key to close the keyboard: the app root clears focus on taps
  outside controls (`dismissKeyboardOnTap`, iOS actual only).
- Back navigation uses `OcBackHandler` (navigation-event back gesture on iOS).
- Lifecycle maps to Android states (background → `ON_STOP`); the event stream
  pauses in the background (`ApplicationStateVisibility`) and resumes on return.
- `testTag` does not become an accessibility identifier; UI automation and
  VoiceOver use labels. Every icon-only control needs a content description.
- Strings, fonts and plurals come from Compose resources and follow the app
  language (`AppleLanguages`); numbers and dates use `NSNumberFormatter` /
  `NSDateFormatter` / `NSRelativeDateTimeFormatter` actuals.

## Platform Services

| Concern | iOS implementation |
|---|---|
| Credentials | `KeychainServerStore` (multiplatform-settings `KeychainSettings`) |
| Settings, drafts | `NSUserDefaultsSettings` suites `settings`, `drafts` |
| Transcript cache | Room with `BundledSQLiteDriver`, file in Caches |
| Network changes | `NWPathMonitor` (`PathNetworkMonitor`) |
| LAN discovery | `getifaddrs` on `en*` interfaces |
| HTTP | Ktor Darwin engine; it ignores `connectTimeoutMillis`, only request and socket timeouts apply |
| Clipboard | `UIPasteboard` (iOS asks before the app pastes) |
| QR, dictation, picker | AVFoundation, Speech + AVAudioEngine, PHPicker / document picker |
| Terminal | xterm.js page from Compose resources in a `WKWebView` (`UIKitView`, non-cooperative touches); bridge over `WKScriptMessageHandler`, font injected as a user script |
| Links | `opencodemobile://session/<id>`, `opencodemobile://new-session` (`AppLinks.kt`, `.onOpenURL` in `iOSApp.swift`) |
| Home screen widget | WidgetKit extension `iosApp/SessionsWidget` (SwiftUI); the app writes a JSON snapshot to the App Group (`WidgetSnapshots.kt`) and calls `WidgetCenter.reloadAllTimelines` |
| Notifications | not supported; settings section hidden |

## Widget Extension

- The extension cannot reach the server or the Keychain: it only reads
  `widget.snapshot` from the App Group defaults (`OCAppGroup` in both Info.plists, from
  `APP_GROUP` in `Config.xcconfig`). Change the JSON contract in `WidgetSnapshots.kt` and
  `SessionsWidget.swift` together.
- The snapshot is written at launch and on `widgetRefreshes` (sessions start, end or are
  deleted, the server changes, the app goes to the background).
- Strings come from the Android widget strings: `bun tools/ios-widget-strings.ts`
  regenerates `Localizable.xcstrings`; `tools/ios-widget-strings.test.ts` fails when stale.
- Device builds need the App Group enabled for `TEAM_ID`; the simulator needs nothing.
- `iosApp.xcodeproj` is edited by hand (no generator): new files need a file reference, a
  group entry and a build phase entry in `project.pbxproj`.

## Build And Verify On The Mac

Linking needs macOS with Xcode; Linux only compiles klibs. Sync the worktree to
the Mac, then from `iosApp/`:

```bash
xcodebuild -project iosApp.xcodeproj -target iosApp -configuration Debug \
  -sdk iphonesimulator -arch arm64 SYMROOT=$HOME/ocm-build build
xcrun simctl install booted $HOME/ocm-build/Debug-iphonesimulator/OpenCodeMobile.app
xcrun simctl launch booted io.github.beppe2880.opencodemobile
```

- Only build the debug simulator framework while iterating; never
  `linkRelease*` or XCFrameworks locally. For slow builds load
  `kotlin-tooling-native-build-performance` (its audit takes `android` as the Gradle
  project root: `scripts/audit-native-build.sh android`).
- Shared tests run on the simulator with `./gradlew :<module>:iosSimulatorArm64Test`.
- Runtime checks: pair against a disposable OpenCode server on the Mac
  (`opencode pair --url http://localhost:<port>`), never the user's personal
  server; exercise the changed screen; read crashes in
  `~/Library/Logs/DiagnosticReports/OpenCodeMobile-*.ips` and logs with
  `xcrun simctl spawn booted log show --predicate 'process == "OpenCodeMobile"'`.
- Automation details (DeviceKit JSON-RPC, coordinates, screenshots) are in
  `references/simulator-automation.md`.
- Never show pairing codes or passwords in screenshots or reports.
