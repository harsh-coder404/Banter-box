# Banterbox

Android chat application with a Spring Boot backend and realtime messaging.

## App Version

- `versionCode`: `1`
- `versionName`: `1.0`

## Modules

- `app`: Android application (Jetpack Compose)
- `backend`: Java Spring Boot backend (REST + WebSocket)

## What's New (Current Build)

- Backend-driven session/navigation flow (`ChatSession`) from splash to chats.
- Realtime chat screen with websocket status (`Connected` / `Disconnected`).
- Chat bubbles with timestamp and tick indicators (`SENT` / `DELIVERED`).
- Chats list shows latest message + latest message timestamp.
- Bottom tab navigation uses single-top behavior to avoid duplicate stacks.
- Logout added on chats top menu.
- OTP success now opens chats directly (profile setup is bypassed in main flow).
- Branding refreshed to `Banterbox` and green accent palette changed to light orange shades.

## Dummy Login Accounts (for Testing)

Use these accounts to access app and test chat quickly:

| Name | Phone Number | OTP / Password |
|------|--------------|----------------|
| Golu | `9890989098` | `0000` |
| Monu | `6262626262` | `0000` |
| Sonu | `8787878787` | `0000` |

> These dummy accounts are seeded by backend startup logic and intended for development/testing.

## Run the Project

### 1) Start backend

```powershell
Set-Location "D:\WhatsApp"
.\gradlew.bat :backend:bootRun
```

Backend runs on `http://localhost:8081`.

### 2) Build or run Android app

```powershell
Set-Location "D:\WhatsApp"
.\gradlew.bat :app:assembleDebug
```

Then run the app from Android Studio on emulator/device.

## Quick Chat Test Flow

1. Login on Device A with `9890989098` + OTP `0000`.
2. Login on Device B with `6262626262` (or `8787878787`) + OTP `0000`.
3. Open chats, tap a contact, send text messages.
4. Verify receiver side updates and chat preview timestamps.

## Notes

- If you get a stale network/security issue on emulator, reinstall app once after clean build.
- Backend API details are documented in `backend/README.md`.

