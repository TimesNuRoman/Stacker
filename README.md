# DevTalk Messenger

```
 ██████╗ ███████╗██╗   ██╗████████╗ █████╗ ██╗     ██╗  ██╗
 ██╔══██╗██╔════╝██║   ██║╚══██╔══╝██╔══██╗██║     ██║ ██╔╝
 ██║  ██║█████╗  ██║   ██║   ██║   ███████║██║     █████╔╝ 
 ██║  ██║██╔══╝  ╚██╗ ██╔╝   ██║   ██╔══██║██║     ██╔═██╗ 
 ██████╔╝███████╗ ╚████╔╝    ██║   ██║  ██║███████╗██║  ██╗
 ╚═════╝ ╚══════╝  ╚═══╝     ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝
             ENCRYPTED · ANONYMOUS · EPHEMERAL
```

**An ephemeral, hacker-themed Android messenger with a cyberpunk aesthetic.**

DevTalk is a zero-registration messenger with audio/video calls. The entire UI is designed in a stereotypical hacker/cyberpunk style — neon green on pure black, Matrix rain effects, CRT scanlines, glitch text, ASCII art terminals, and surveillance-grade UI metaphors.

## Features

### Core
- **Zero Registration** — Pick a unique handle. No email, phone, or password.
- **Ephemeral Sessions** — Exit = total wipe. All data shredded. No recovery.
- **Real-time Messaging** — Firebase Realtime Database, encrypted channels.
- **Audio & Video Calls** — WebRTC P2P with Firebase signaling.

### User Acquisition & Sharing (10+ methods)
- **QR Code Profile** — Neon green QR on black background for agent identification
- **Deep Link Sharing** — `devtalk://profile/handle` and `https://devtalk.app/u/handle`
- **QR Scanner** — Camera scanner with neon corner crosshairs
- **System Share** — Share via any installed app
- **WhatsApp Share** — Direct share to WhatsApp contacts
- **Telegram Share** — Direct share to Telegram chats
- **SMS Share** — Send invite via text message
- **Email Share** — Send invite via email
- **Profile Card Image** — Generate a hacker-themed card with QR for social media
- **Clipboard** — One-tap copy link or handle
- **NFC** — Tap phones to exchange profiles
- **Home Screen Widget** — QR code widget for instant scanning
- **User Discovery** — Search agents, see who's online, browse recent joins
- **Invite CTA** — Appears when search returns no results (passive acquisition)

### Simplified Registration
- **Quick-Pick Handles** — 6 random hacker-style names (shadow_dev, cyber_ops...)
- **One-Tap Refresh** — Generate new suggestions instantly
- **Type Your Own** — Manual entry still available
- **One Button Launch** — Full-width "LAUNCH SESSION" button
- **Fast Boot** — Shortened animation for quicker entry

### Easy Contact Finding
- **Live Search** — Type 2+ characters for instant results with debounce
- **Online Users** — See all currently active agents
- **Recent Users** — Browse newly joined agents
- **One-Tap Add** — Add contact with a single tap
- **Invite Missing Users** — When search fails, invite prompt appears

### Hacker-Themed UI

The entire app looks like something straight out of a hacking movie:

- **Pure Black + Neon Green** — Matrix-inspired color scheme with cyan, magenta, and red accents
- **Matrix Rain** — Animated Japanese/binary character rain in the background
- **CRT Scanlines** — Horizontal scanline overlay + vignette effect on every screen
- **Glitch Text** — Logo and key text randomly glitches with RGB chromatic aberration
- **ASCII Art** — Block-letter logos, box-drawing characters for panels and borders
- **Neon Glow Borders** — All panels and inputs have glowing neon edge effects
- **Terminal Boot Sequence** — Welcome screen simulates a hacker system boot with Tor routing, encryption init, and key generation
- **Hacker Prompt** — `root@devtalk:~#` style input prompts
- **File Tree as "Nodes"** — Contacts are "agents", chats are "secure channels"
- **Line-Numbered Messages** — Every message has a gutter with line numbers
- **IRC-style Chat** — Messages shown as `[HH:mm:ss] <username>` format
- **Surveillance Call UI** — Calls show pulsing neon rings, signal indicators, and "CHANNEL ACTIVE" panels
- **Self-Destruct Dialog** — Logout shows a pulsing red-bordered warning with `rm -rf` command
- **Status Bar HUD** — Bottom bar shows encryption status, agent count, channel count
- **Agent Identity Card** — Profile displayed as ASCII box-art with encryption info

## Screenshots Layout

| Screen | Description |
|--------|-------------|
| Welcome | Terminal boot sequence with Matrix rain, ASCII logo, neon prompt |
| Main | Sidebar with nodes/agents tree, tabbed encrypted channels, IRC-style chat |
| Profile | Agent identity card, neon QR code, share link, danger zone |
| QR Scanner | Camera with neon corner targeting, crosshair overlay |
| Call | Pulsing signal rings, channel status panel, neon control buttons |
| Logout | Pulsing red self-destruct confirmation with wipe command |

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Hilt DI |
| Backend | Firebase (Anonymous Auth, Realtime Database) |
| Calls | WebRTC (stream-webrtc-android) |
| QR Codes | ZXing + ML Kit Barcode Scanning |
| Camera | CameraX |
| Storage | DataStore Preferences |

## Project Structure

```
app/src/main/java/com/devtalk/messenger/
├── DevTalkApp.kt                    # Hilt application
├── MainActivity.kt                  # Navigation + deep link handler
├── data/
│   ├── model/
│   │   ├── User.kt                  # Agent model (ONLINE/GHOST/STEALTH/DARK)
│   │   ├── Message.kt               # Message with IRC formatting
│   │   ├── Chat.kt                  # Encrypted channel model
│   │   ├── Contact.kt               # Agent contact
│   │   └── CallState.kt             # Call signaling model
│   └── repository/
│       ├── FirebaseRepository.kt     # CRUD + search + online/recent queries
│       └── UserPreferences.kt        # Local session DataStore
├── di/
│   └── AppModule.kt                 # Hilt DI module
├── ui/
│   ├── MainViewModel.kt             # State + search/discovery logic
│   ├── theme/
│   │   └── IdeTheme.kt              # Hacker color palette + neon typography
│   ├── components/
│   │   └── IdeComponents.kt         # CRT overlay, Matrix rain, glitch text,
│   │                                  neon borders, hacker panels, blinking cursor
│   └── screens/
│       ├── WelcomeScreen.kt         # Quick-pick handles + boot sequence
│       ├── MainScreen.kt            # Hacker layout (search/invite in toolbar)
│       ├── UserSearchScreen.kt      # Live search + online/recent discovery
│       ├── InviteScreen.kt          # All 10+ sharing methods
│       ├── ProfileScreen.kt         # Identity card + invite CTA
│       ├── QrScannerScreen.kt       # Targeting scanner with crosshairs
│       ├── CallScreen.kt            # Surveillance-style call UI
│       └── LogoutDialog.kt          # Self-destruct confirmation
├── webrtc/
│   ├── WebRtcManager.kt             # WebRTC peer connection
│   └── CallService.kt               # Foreground call service
├── widget/
│   └── QrWidgetProvider.kt          # Home screen QR code widget
└── util/
    ├── QrCodeUtils.kt               # Neon QR generation + deep links
    ├── InviteManager.kt             # All sharing methods + profile card
    └── UsernameGenerator.kt         # Hacker-style handle generator
```

## Custom Visual Effects

### Matrix Rain (`MatrixRain`)
Animated columns of Japanese katakana and binary digits falling like in The Matrix. Configurable density, speed, and opacity.

### CRT Overlay (`CrtOverlay`)
Horizontal scanline effect + radial vignette darkening, simulating an old CRT monitor.

### Glitch Text (`GlitchText`)
Text that randomly glitches — characters replaced with `█▓▒░╠╣╦╩═║` symbols. Includes RGB chromatic aberration with cyan/magenta offset layers.

### Neon Glow Borders (`neonBorder`)
All panels, inputs, and interactive elements have a soft outer glow + crisp neon border line.

### Blinking Cursor (`BlinkingCursor`)
Classic terminal blinking block cursor `█`.

### Typewriter Effect (`TypewriterText`)
Text that types out character by character with a blinking cursor.

## Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Firebase project

### Firebase Setup

1. [Firebase Console](https://console.firebase.google.com/) → Create project
2. Add Android app: `com.devtalk.messenger`
3. Download `google-services.json` → place in `app/`
4. Enable **Anonymous Authentication**
5. Create **Realtime Database** (test mode for development)

### Database Rules

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": true,
        ".write": "$uid === auth.uid"
      }
    },
    "contacts": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "auth != null"
      }
    },
    "chats": {
      "$chatId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    },
    "messages": {
      "$chatId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    },
    "calls": {
      "$callId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

### Build & Run

```bash
git clone <repo-url>
cd DevTalk
cp /path/to/google-services.json app/
./gradlew assembleDebug
./gradlew installDebug
```

## How It Works

### Registration
1. Launch → Terminal boot sequence (Tor routing, crypto init)
2. Enter unique handle at `root@devtalk:~#` prompt
3. Firebase Anonymous Auth → session created
4. You're in. No traces.

### Adding Agents
- **QR Scan**: Profile → Show neon QR → Other person scans with crosshair scanner
- **Share Link**: Copy `devtalk://profile/handle`
- **Manual**: Scanner → type handle → LOCATE

### Messaging
- Select agent from "NODES" tree → channel opens as tab
- Messages displayed IRC-style with timestamps and line numbers
- Terminal prompt input: `handle@dtalk:~#`

### Calls
- Open channel → Voice/Video icons in breadcrumb bar
- WebRTC P2P through Firebase signaling
- Pulsing neon ring visualization during call
- Foreground service for background persistence

### Self-Destruct
- Click ☠ power button or Profile → Danger Zone
- Pulsing red warning dialog with `rm -rf` command
- Confirm → all data wiped from Firebase + local storage
- Back to boot screen. No traces.

## License

MIT. Built for developers who live in the terminal. ☠
