# Banterbox

Android chat app with a Spring Boot backend, JWT auth, and realtime messaging.

## Modules

- `app`: Android app (Jetpack Compose)
- `backend`: Java Spring Boot backend (REST + WebSocket)

## Current Features

- Dummy login flow with backend session (`ChatSession`) and direct navigation to chats after OTP/password validation.
- Realtime chat over WebSocket with in-screen connection status (`Connected` / `Disconnected`).
- Chat bubbles with timestamps and single/double tick indicators.
- Chats list shows last message + last message time.
- Account-specific chat persistence: logout/login restores chat history for that account from backend.
- Message delete with confirmation dialog (delete-for-everyone behavior via backend).
- Delete icon appears on message hover (desktop) and on tap/expand (touch), with long-press fallback.
- Bottom tabs use single-top navigation (prevents duplicate destination stacking).

## Dummy Accounts (Development)

These users are auto-seeded by backend startup:

| Name | Phone Number | OTP/Password |
|------|--------------|--------------|
| Golu | `9890989098` | `0000`       |
| Monu | `6262626262` | `0000`       |
| Sonu | `8787878787` | `0000`       |

## How To Run

### 1) Start backend first

```powershell
Set-Location "<project_folder>"
.\gradlew.bat :backend:bootRun --console=plain
```

Backend base URL: `http://localhost:8081`

### 2) Build/run Android app

```powershell
Set-Location "<projct_folder>"
.\gradlew.bat :app:assembleDebug
```

Run the generated debug app from Android Studio or install the APK.

## How To Use (Quick Test)

1. Login with one dummy account (for example `9890989098` / `0000`).
2. Open chats and tap another dummy contact.
3. Send a message and verify:
   - bubble appears once,
   - local timestamp is shown,
   - receiver gets it in realtime,
   - chats list preview updates.
4. Delete test:
   - tap/hover a bubble to show delete icon (or long-press),
   - confirm delete,
   - message is removed from both participants after sync/realtime update.

## Notes

- If delete is not working, ensure backend was restarted after latest changes.
- Emulator networking defaults to `10.0.2.2`; fallback hosts are also configured in `network_security_config.xml`.
- For physical devices, if local backend is unreachable, use ADB reverse:

```powershell
adb reverse tcp:8081 tcp:8081
```

- Backend API/module notes: `backend/README.md`
