# iOS Simulator Automation

The simulator has no ADB. UI automation uses the DeviceKit XCUITest runner
(`com.mobilenext.devicekit-iosUITests.xctrunner`, installed by Mobile MCP), which
serves JSON-RPC on `127.0.0.1:12004` of the Mac.

## Start

```bash
xcrun simctl boot <udid>            # skip when already booted
open -a Simulator
curl -s http://127.0.0.1:12004/health || \
  xcrun simctl launch booted com.mobilenext.devicekit-iosUITests.xctrunner
```

Poll `/health` with a deadline before sending commands.

## Commands

`scripts/ios.py` (copy it to the Mac) wraps the RPC calls:

```bash
python3 ios.py ui                 # accessibility tree: type, label, value, center (points)
python3 ios.py tap "Settings"     # tap by exact label, then by substring
python3 ios.py tapxy 196 440      # tap at points (393x852 on iPhone 15)
python3 ios.py longpress "label"
python3 ios.py text "hello"       # type into the focused field
python3 ios.py swipe 196 700 196 250
python3 ios.py key home
python3 ios.py shot name          # /tmp/name.png, scaled to 1000 px high
```

Raw call: `curl -X POST http://127.0.0.1:12004/rpc -d
'{"jsonrpc":"2.0","method":"device.io.tap","params":{"x":196,"y":478,"deviceId":"any"},"id":1}'`.
Other methods: `device.dump.ui`, `device.io.text`, `device.io.swipe`,
`device.clipboard.set`, `device.apps.launch` / `terminate` (`{"bundleId": ...}`).

## Pitfalls

- Coordinates are points, not screenshot pixels: divide screenshot pixels by the
  scale (`device.info`).
- `testTag`s are not exposed; target controls by accessibility label. Controls
  without one (e.g. a FAB with only an icon and text in a custom layout) may be
  missing from the tree: tap by coordinates and report the accessibility gap.
- `device.clipboard.set` briefly brings DeviceKit to the foreground; wait about
  3 s before tapping in the app, or the tap is lost in the transition.
- Typing a full pairing link character by character into the server URL field
  triggers the app's link detection midway. Type the base URL, move to the code
  field (keyboard "next"), then type the code; or paste the link.
- Launching the runner sends the app to the background; relaunch it with
  `device.apps.launch`.
- A freshly written screenshot may need a moment before another tool can read it.
