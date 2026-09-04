# APO Lite

Minimal Android client focused on the operational workflow needed on iQOO Z9x / arm64-v8a.

## Scope

- Login / authorized session
- Order list
- Order detail
- Packing
- Ready to Ship
- Confirmation
- Pure text chat

## Explicitly excluded

- Voice call
- Video call
- WebRTC
- Voice notes
- Media/attachment chat
- Unrelated APO modules

## Size target

Target APK: 5–10 MB where practical. Native Android UI, minimal dependencies, arm64-v8a target, no bundled WebRTC/ML models unless a required workflow proves they are necessary.

## Integration boundary

APO Lite should use authorized backend interfaces for login, order state changes, confirmation, and chat. Production authentication, server validation, and production location validation are retained. Do not copy private credentials or bypass integrity/location controls from the original APK.

## Planned screens

1. Login
2. Orders
3. Order Detail
4. Packing
5. Ready to Ship
6. Confirmation
7. Text Chat

## Implementation plan

1. Audit the original APO APK to identify models and authorized interfaces used by the selected workflow.
2. Document request/response contracts without embedding credentials.
3. Build a minimal native Android shell.
4. Implement authenticated order read/write flows against an authorized environment.
5. Implement text-only chat.
6. Produce an arm64-focused release APK and measure final size.
