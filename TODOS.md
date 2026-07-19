# TODOS

## Phase 2 (post-MVP)

**Priority:** P2
- [ ] Fingerprint verification UI (compare identity fingerprints via QR for MITM detection)
- [ ] TURN relay server deployment (fallback when STUN fails)

## Completed

- **v0.1.0** (2026-07-20): MVP release
  - Anonymous Ed25519 identity, QR scan, mDNS discovery
  - E2EE (ECDH + AES-256-GCM + HKDF)
  - TCP P2P messaging with Room + sqlcipher storage
  - WebRTC + Tor framework, STUN hole punching
  - Foreground service + notification permission
  - Message retry queue (outbox + backoff + auto-flush)
  - Dark theme (system-follow)
  - Release signing (15MB APK, R8 optimized)
  - ktlint + detekt + Android Lint
  - Compose UI: identity, contacts, chat, scanner
