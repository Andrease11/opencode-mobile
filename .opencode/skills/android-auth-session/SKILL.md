---
name: android-auth-session
description: Use when changing opencode-mobile server connection, QR/link pairing, password login, saved servers, credential storage, active-server switching, or 401 handling.
---

# Android Server Connection And Credentials

The app has no user accounts. A "session" with a server is a saved
`ServerProfile`: base URL + Basic auth secret, owned by `ServerRepository`.

## Protocol

- Every request uses HTTP Basic `opencode:<secret>`.
- The secret is either the server password (`CredentialKind.Password`) or a
  pairing token (`CredentialKind.PairingToken`).
- Pairing: desktop runs `POST /api/pair` (authenticated) and shows
  `http(s)://host:port/auth/connect/<code>` as a QR (`opencode pair`).
  The app redeems it with an unauthenticated `GET /auth/connect/<code>` and
  `Accept: application/json`, receiving `{ "token" }`. Codes are single-use and
  expire after about five minutes.
- `ConnectLinkParser` (core:network) is the only parser for scanned, pasted or
  typed input. Bare hosts default to port 4096.

## Ownership

- `ServerRepository` owns saved profiles, the active server and validation
  (`probe`, `pair`, `addWithPassword`). Nothing else persists secrets.
- Secrets live in encrypted storage in `core:data`; UI state only holds what the
  user is typing until submit.
- The network client for the active server is rebuilt when the active profile
  changes; event streams restart on that change.

## Rules

- Validate before saving: a profile is persisted only after a successful
  authenticated `GET /api/info`.
- Pairing codes are redeemed exactly once; never retry a redeem automatically.
- A 401 on a saved profile is surfaced as `OpenCodeError.Unauthorized` and
  leads the user to re-pair or update the password. Network failures stay
  retryable and never delete a profile.
- Removing the active server activates the next saved server or returns to the
  connect flow.
- Never log, toast, screenshot or put in analytics: passwords, tokens, pairing
  codes or full pairing links. Show only scheme/host/port.

Load `android-security` for storage, clipboard and camera rules and
`android-opencode-api` for interceptor changes.

## Verification

Cover pairing success, expired/used code, wrong password, unreachable host,
switching servers while a stream is open, removing the active server and app
restart with a saved server.
