# SeChat

Lightweight, secure, privacy-respecting P2P encrypted messenger for Android.

## Features

- End-to-end encryption via ECDH + AES-256-GCM + HKDF
- Anonymous ephemeral identities (no phone/email required)
- Peer-to-peer messaging (no central server)
- LAN discovery via mDNS (NSD)
- WebRTC STUN hole punching for WAN connections
- Optional Tor/Orbot SOCKS5 routing
- Dark theme (follows system setting)
- Full local encryption (sqlcipher + Room)
- Message retry queue with auto-flush on reconnect
- Background foreground service for persistent connectivity
- ktlint + detekt + Android Lint enforced in CI

## Build

```bash
# Debug APK (31MB, arm64-v8a)
JAVA_HOME=/usr/lib/jvm/java-1.21.0-openjdk-amd64 ./gradlew assembleDebug

# Release APK (15MB, signed, R8 optimized)
JAVA_HOME=/usr/lib/jvm/java-1.21.0-openjdk-amd64 ./gradlew assembleRelease

# Install on device
adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
adb shell am start -n com.sechat.app/.MainActivity
```

## Project Structure

```
SeChat/
├── app/                  # Main module (Navigation + DI + Theme)
├── core/
│   ├── crypto/           # Encryption layer (ECDH, AES-GCM, HKDF)
│   ├── data/             # Data layer (Room + sqlcipher)
│   └── p2p/              # P2P layer (TCP, WebRTC, Tor, messaging)
├── feature/
│   ├── identity/         # Identity generation + QR code
│   ├── contacts/         # Contact list + discovery
│   └── chat/             # Chat UI + message management
├── DESIGN.md             # Design system documentation
├── CHANGELOG.md          # Release history
├── TODOS.md              # Roadmap
└── .github/workflows/    # CI (lint → detekt → lint → assemble → test)
```

## License

MIT
