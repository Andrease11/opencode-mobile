# Security

## Reporting a vulnerability

Please report vulnerabilities privately through GitHub: open the **Security** tab of this
repository and choose **Report a vulnerability**. Do not open a public issue. You will get an
answer as soon as possible, and credit in the release notes if you want it.

## How the app handles your server

- The app talks to your own OpenCode server with HTTP Basic auth, using the server password
  or a token obtained by pairing (`opencode pair`).
- Credentials are stored encrypted on the device (Android Keystore) and are never logged.
- While the connect screen is open, the app looks for OpenCode on port 4096 of the local
  Wi-Fi or Ethernet subnet (at most 254 addresses). It only requests the public web manifest
  and never sends credentials during the scan.
- Transcripts are cached on the device so chats open quickly; uninstalling the app or removing
  the server deletes them.
- OpenCode servers usually run over **plain HTTP**, so the app allows cleartext traffic.
  Anyone on the same network path can read the traffic, including the credentials. Reach the
  server over a network you trust (home LAN or a VPN such as WireGuard or Tailscale), or put
  it behind HTTPS. **Never expose the OpenCode port directly to the internet.**

## Supported versions

Only the latest release receives fixes.
