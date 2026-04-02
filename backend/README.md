# WhatsApp Backend (Spring Boot)

This module is a Java Spring Boot backend for the app, with REST APIs, JWT auth, and WebSocket chat events.

## Features

- User register/login with JWT
- Contact management
- Message send and history APIs
- WebSocket STOMP for realtime chat updates
- H2 database for local development

## Run

From repo root:

```powershell
.\gradlew.bat :backend:bootRun
```

Backend starts on `http://localhost:8080`.

## Test

```powershell
.\gradlew.bat :backend:test
```

## REST API

### Register
`POST /api/auth/register`

```json
{
  "name": "MSD",
  "phoneNumber": "9000000001",
  "password": "secret123"
}
```

### Login
`POST /api/auth/login`

```json
{
  "phoneNumber": "9000000001",
  "password": "secret123"
}
```

Returns JWT token.

### Add contact
`POST /api/contacts`
Header: `Authorization: Bearer <token>`

```json
{
  "contactPhoneNumber": "9000000002"
}
```

### Get contacts
`GET /api/contacts`
Header: `Authorization: Bearer <token>`

### Send message (REST)
`POST /api/messages`
Header: `Authorization: Bearer <token>`

```json
{
  "senderId": 1,
  "receiverId": 2,
  "content": "hello"
}
```

### Message history
`GET /api/messages/{otherUserId}`
Header: `Authorization: Bearer <token>`

## WebSocket

- Endpoint: `/ws`
- App destination: `/app/chat.send`
- Topic: `/topic/chat.{minUserId}_{maxUserId}`

Send frame payload:

```json
{
  "senderId": 1,
  "receiverId": 2,
  "content": "realtime message"
}
```

Subscribe to:

`/topic/chat.1_2`

## Notes for Android migration

- Replace Firebase auth calls with `/api/auth/register` and `/api/auth/login`
- Replace chat list/search/add operations with `/api/contacts` and `/api/messages`
- Use STOMP over WebSocket for realtime receive

