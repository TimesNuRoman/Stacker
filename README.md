# DevTalk Messenger

```
╔══════════════════════════════════════╗
║                                      ║
║         ██████╗ ████████╗            ║
║         ██╔══██╗╚══██╔══╝            ║
║         ██║  ██║   ██║               ║
║         ██║  ██║   ██║               ║
║         ██████╔╝   ██║               ║
║         ╚═════╝    ╚═╝               ║
║                                      ║
║          D E V T A L K               ║
║                                      ║
╚══════════════════════════════════════╝
```

**An ephemeral, IDE-themed Android messenger for developers.**

DevTalk is a zero-registration messenger with audio/video calls, designed to look and feel like an IDE (IntelliJ IDEA / Darcula theme). No emails, no passwords, no phone numbers — just pick a unique username and start chatting.

## Features

### Core
- **Zero Registration** — Pick a unique username on first launch. That's your identity.
- **Ephemeral Sessions** — Exit = account deleted permanently. No traces left.
- **Real-time Messaging** — Firebase Realtime Database powered instant messaging.
- **Audio & Video Calls** — WebRTC-based peer-to-peer calls with Firebase signaling.

### Sharing
- **QR Code Profile** — Generate and share your unique QR code for instant contact exchange.
- **Deep Link Sharing** — Share `devtalk://profile/username` links via any app.
- **QR Scanner** — Scan someone's QR code to add them instantly.

### IDE-Themed UI
- **Darcula Dark Theme** — Modeled after IntelliJ IDEA's iconic color scheme.
- **Monospace Typography** — All text in JetBrains Mono / system monospace.
- **Tab-based Navigation** — Chats open as editor tabs, just like code files.
- **File Tree Sidebar** — Contacts and chats organized as a project tree.
- **Line Numbers** — Messages displayed with gutter line numbers.
- **Terminal-style Input** — Type messages in a terminal prompt (`user@devtalk:~$`).
- **Code Syntax Highlighting** — Messages styled as Kotlin code blocks.
- **Status Bar** — IDE status bar with connection info, user status, and stats.
- **Boot Sequence** — Welcome screen features a terminal boot animation.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Hilt DI |
| Backend | Firebase (Auth, Realtime Database) |
| Calls | WebRTC (stream-webrtc-android) |
| QR Codes | ZXing + ML Kit Barcode Scanning |
| Camera | CameraX |
| Storage | DataStore Preferences |

## Project Structure

```
app/src/main/java/com/devtalk/messenger/
├── DevTalkApp.kt              # Application class (Hilt)
├── MainActivity.kt            # Entry point + navigation host
├── data/
│   ├── model/
│   │   ├── User.kt            # User data model
│   │   ├── Message.kt         # Message with terminal formatting
│   │   ├── Chat.kt            # Chat/conversation model
│   │   ├── Contact.kt         # Contact with tree entry
│   │   └── CallState.kt       # WebRTC call signaling model
│   └── repository/
│       ├── FirebaseRepository.kt   # All Firebase operations
│       └── UserPreferences.kt      # Local DataStore
├── di/
│   └── AppModule.kt           # Hilt dependency injection
├── ui/
│   ├── MainViewModel.kt       # Main state management
│   ├── theme/
│   │   └── IdeTheme.kt        # Darcula colors & typography
│   ├── components/
│   │   └── IdeComponents.kt   # Reusable IDE-style widgets
│   └── screens/
│       ├── WelcomeScreen.kt   # Boot sequence + username entry
│       ├── MainScreen.kt      # IDE layout (sidebar, tabs, editor)
│       ├── ProfileScreen.kt   # QR code + profile as code
│       ├── QrScannerScreen.kt # Camera-based QR scanning
│       ├── CallScreen.kt      # Audio/video call UI
│       └── LogoutDialog.kt    # Destructive logout confirmation
├── webrtc/
│   ├── WebRtcManager.kt       # WebRTC peer connection manager
│   └── CallService.kt         # Foreground service for calls
└── util/
    └── QrCodeUtils.kt         # QR generation & deep link parsing
```

## Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Firebase project

### Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project (or use existing)
3. Add an Android app with package name: `com.devtalk.messenger`
4. Download `google-services.json` and place it in `app/`
5. Enable **Anonymous Authentication**:
   - Firebase Console → Authentication → Sign-in method → Anonymous → Enable
6. Set up **Realtime Database**:
   - Firebase Console → Realtime Database → Create Database
   - Start in **test mode** (or configure rules below)

### Database Rules (Production)

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
        ".read": "auth != null && (data.child('participants').child(0).val() === auth.uid || data.child('participants').child(1).val() === auth.uid)",
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
# Clone the repository
git clone <repo-url>
cd DevTalk

# Place your google-services.json in app/
cp /path/to/google-services.json app/

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## How It Works

### Registration Flow
1. Launch app → Terminal boot sequence plays
2. Enter a unique username (3+ chars, alphanumeric + `_` + `-`)
3. Firebase Anonymous Auth creates a session
4. User profile stored in Realtime Database
5. Done! You're in the IDE.

### Adding Contacts
- **QR Code**: Open Profile → Show QR → Other person scans it
- **Share Link**: Copy `devtalk://profile/username` and send it
- **Manual**: In Scanner screen, type username directly

### Messaging
- Select a contact from the project tree (sidebar)
- Chat opens as an editor tab with `.chat` extension
- Messages appear as Kotlin function calls with line numbers
- Terminal-style input prompt at the bottom

### Calls
- Open a chat → Click phone/video icon in the breadcrumb bar
- WebRTC establishes P2P connection via Firebase signaling
- Foreground service keeps call alive in background

### Logout = Delete
- Click power button (top-right) or go to Profile → Danger Zone
- Confirmation dialog warns about permanent deletion
- All data (user, contacts, messages) is erased from Firebase
- Local session cleared, back to Welcome screen

## Design Philosophy

The UI is intentionally designed to look like IntelliJ IDEA:
- **Dark color scheme** matches Darcula theme exactly
- **Monospace everything** — because we're developers
- **File metaphors** — chats are `.chat` files, contacts are in folders
- **Terminal prompts** — message input feels like a command line
- **Code blocks** — messages formatted as Kotlin function calls
- **Status bar** — shows connection status like an IDE build status
- **Line numbers** — because every good editor has them

## License

MIT License. Built with ❤️ for developers.
