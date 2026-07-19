# TODOS

## Phase 2 (post-MVP)

**Priority:** P0
- [ ] Google WebRTC `com.infobip:google-webrtc` — enable actual STUN hole punching (currently scaffolded)

**Priority:** P1
- [ ] Notification channel + foreground service for background message delivery
- [ ] Message send failure retry mechanism (queue failed sends, retry on reconnect)

**Priority:** P2
- [ ] Fingerprint verification UI (compare identity fingerprints via QR for MITM detection)
- [ ] Offline message queue (store-and-forward when peer is unreachable)
- [ ] TURN relay server deployment (fallback when STUN fails)

## Completed

- **v0.1.0** (2026-07-20): MVP release
  - Anonymous Ed25519 identity, QR scan, mDNS discovery
  - E2EE (ECDH + AES-256-GCM + HKDF)
  - TCP P2P messaging with Room + sqlcipher storage
  - WebRTC + Tor framework scaffolded
  - Compose UI: identity, contacts, chat, scanner
