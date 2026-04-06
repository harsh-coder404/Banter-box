# BanterBox Backend (Spring Boot)

Java Spring Boot backend for BanterBox with JWT auth, contacts, message history, and realtime chat.

## Features

- User auth with JWT (`/api/auth/register`, `/api/auth/login`)
- Contact management (`/api/contacts`)
- Message send, history, and delete APIs
- Realtime chat support over WebSocket
- H2 database for local development
- Dummy account seeding for quick testing

## Default Runtime

- Base URL: `http://localhost:8081`
- H2 console: `http://localhost:8081/h2-console`

## Run

From project root:

```powershell
Set-Location "<project_folder>"
.\gradlew.bat :backend:bootRun --console=plain
```

## Test / Verify

```powershell
Set-Location "<project_folder>"
.\gradlew.bat :backend:test --console=plain
```

## Seeded Dummy Accounts

These are created/updated at startup by `DummyDataInitializer`:

| Name | Phone Number | OTP/Password |
|------|--------------|--------------|
| Golu | `9890989098` | `0000`       |
| Monu | `6262626262` | `0000`       |
| Sonu | `8787878787` | `0000`       |

## REST API

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

Login request example:

```json
{
  "phoneNumber": "6262626262",
  "password": "0000"
}
```

### Contacts (JWT required)

- `GET /api/contacts`
- `POST /api/contacts`

Add contact request example:

```json
{
  "contactPhoneNumber": "9890989098"
}
```

### Messages (JWT required)

- `POST /api/messages`
- `GET /api/messages/{otherUserId}`
- `DELETE /api/messages/{messageId}`

Send message request example:

```json
{
  "senderId": 2,
  "receiverId": 1,
  "content": "hello"
}
```

## WebSocket

The Android app currently uses raw WebSocket endpoint:

- Endpoint: `/ws/chat`
- Query param: `userId`
- Example URL: `ws://localhost:8081/ws/chat?userId=2`

Message payload format:

```json
{
  "senderId": 2,
  "receiverId": 1,
  "content": "realtime message"
}
```

## Notes

- If API behavior seems stale after code changes, restart backend process.
- For Android emulator: app typically reaches host via `10.0.2.2`.
- For physical devices, use ADB reverse when testing local backend:

```powershell
adb reverse tcp:8081 tcp:8081
```
