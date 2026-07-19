# SeChat Design System

## Brand
SeChat — anonymous encrypted messenger. Clean, neutral, functional.
No tracking, no accounts, no servers.

## Color System

| Token | Value | Usage |
|-------|-------|-------|
| Primary | `#007AFF` (Blue) | Action buttons, links, navigation |
| Secure Green | `#4CAF50` | E2EE indicator, online status |
| Error Red | `#F44336` | Error banners, destructive actions |
| Background | `#FFFFFF` | Page backgrounds |
| Surface | `#F5F5F5` | Card backgrounds, dividers |
| Text Primary | `#000000` | Primary content |
| Text Secondary | `#666666` | Secondary information |
| Text Hint | `#999999` | Placeholders, metadata |
| Background (Dark) | `#121212` | Dark mode page backgrounds |
| Surface (Dark) | `#1E1E1E` | Dark mode card backgrounds |
| Text Primary (Dark) | `#E0E0E0` | Dark mode primary content |
| Primary (Dark) | `#0A84FF` | Dark mode accent |

## Theme

- Light: default Material3 light scheme
- Dark: auto-detects system dark mode (`isSystemInDarkTheme()`)
- All interactive states (loading/empty/error/success) follow theme

## Typography

- Primary font: **Noto Sans CJK** (system default on Android)
- Code/monospace: system monospace for fingerprints
- Scale: Material3 type scale defaults

## Spacing

- Screen padding: 24dp
- Content padding: 16dp
- Card padding: 24dp
- List item padding: 12dp vertical, 16dp horizontal

## Shape

| Component | Radius |
|-----------|--------|
| Chat bubble (sent) | 16dp top L, 4dp top R, 16dp bot L+R |
| Chat bubble (received) | 4dp top L, 16dp top R, 16dp bot L+R |
| Cards | 16dp |
| Input field | 20dp |
| Avatars | Circle (50%) |

## Interactive States

| Feature | Loading | Empty | Error | Success |
|---------|---------|-------|-------|---------|
| Identity | Spinner + "Generating..." | "Create Anonymous Identity" button | Red text + Retry button | QR + fingerprint |
| Contacts | "Discovering nearby peers..." | "No contacts yet" + Discover button | Discovery failed message | Contact list |
| Chat | Yellow dot "Connecting..." | "Beginning of encrypted conversation" | Red banner + "Re-scan" | Green dot "E2EE ✓" |

## Touch Targets

Minimum 44dp for all interactive elements.

## Privacy

- Online/offline status only (no timestamps)
- Ephemeral identities — no recovery mechanism by design
- E2EE confirmation shown once on first encrypted message
