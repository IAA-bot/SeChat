# Changelog

## [0.1.0.0] - 2026-07-20

### Added
- Anonymous Ed25519 identity generation with Android KeyStore storage
- QR code generation and CameraX/Zxing live scanning for peer discovery
- mDNS (NSD) LAN service discovery for nearby peers
- End-to-end encryption via ECDH key agreement + AES-256-GCM + HKDF
- TCP WireMessage protocol for P2P message transport
- MessageManager orchestrating encrypt → send → receive → decrypt pipeline
- Room database with sqlcipher full encryption for local storage
- WebRTC framework with ICE/STUN for NAT traversal
- Tor/Orbot SOCKS5 proxy integration with ProxySelector
- TransportManager supporting TCP-LAN / WebRTC / Tor triple-mode routing
- Connection status indicators (online/offline/E2EE/connecting/failed)
- Encrypted session expiration detection and re-scan prompt
- Contact list with online/offline status display
- Compose UI: identity page, contacts list, chat view, scanner screen
- Koin dependency injection with multi-module architecture
- Compose Navigation for screen routing
- GitHub Actions CI workflow (build + lint + test)
- DESIGN.md with full design system documentation

### Changed
- Settings.gradle.kts: corrected `dependencyResolution` → `dependencyResolutionManagement`
- Gradle wrapper added for build reproducibility
- compose-bom 2024.01.00 for Material3 stability

### Fixed
- Compose BOM version conflict causing `NoSuchMethodError` on startup
- APK size reduced from 70MB to 31MB via ABI splitting (arm64-v8a + armeabi-v7a)
- Identity page QR code alignment (centered in card container)
- Camera permission: real-time check at click time, removes stale state cache
- Permission flow: auto-navigates to scanner after grant without second tap
- Theme configuration for API 26+ compatibility
- sqlcipher ProGuard rules to prevent native library stripping
