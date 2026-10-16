# Changelog

## 0.3.0 (2026-10-16)

First public release. Earlier versions were developed privately; their notes are kept below.

### Features

* **chat:** Fork a session from a message, like the OpenCode TUI.
* **chat:** Add the `/fork` command to copy the whole session.

## 0.2.0 (2026-10-15)

### Features

* **connect:** Find OpenCode servers on the local network.
* **connect:** Warn before sending credentials over plain HTTP to a public address.
* **data:** Cache session lists per server and recover dead connections.
* **ui:** Add nine interface languages and Arabic RTL support.

### Bug fixes

* **connect:** Show the pairing command inline and highlighted.
* **release:** Refuse debug-signed release builds and verify the certificate.

## 0.1.0 (2026-10-15)

* First release: pairing by QR code, link or password, live sessions and chat, agent and
  model switching, permissions and questions, local transcript cache, signed APKs.
