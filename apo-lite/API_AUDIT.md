# APO Lite API Audit (from APO 4.9.0 APK)

Static string inspection of the original APK identified the following interfaces relevant to the Lite scope. This file intentionally records only endpoint names/models and does not copy credentials, tokens, signing material, or bypass logic.

## Observed service hosts

- https://apo-apps-gateway.alfagift.id/
- https://apo-apps.alfagift.id/
- https://chat-gateway.alfagift.id/
- https://beta-chat-gateway.alfagift.id/
- https://init-stg-chat-gateway.alfagift.id/

## Authentication-related paths

- `/v1/auth/login`
- `/v2/auth/token`

Authentication request/response contracts still need exact mapping before APO Lite can use them. Do not hardcode or extract private user credentials/tokens from the original app.

## Order / shipment paths observed

- `/v1/receipt-revamp/apo-online-active-shipment`
- `/v1/chat/path/detail-order`
- `/v2/order/product/scan`

Observed order/shipment model strings include:

- `ActiveShipmentResponse`
- `ActiveShipmentListResponse`
- `shipmentNumber`
- `shipmentNo`
- `shipmentActive`
- `deliveryEstimate`
- `orderLists`
- `startShipment`
- `endShipment`
- `onConfirmRequest`
- `confirmLabel`

Exact Packing / Ready-to-Ship / Confirmation HTTP contracts are not yet confirmed from static strings alone and must be mapped before production wiring.

## Text chat paths observed

- `/v1/app/chat-config`
- `/v1/channels/`
- `/v1/channels/me`
- `/v1/messages`
- `/v1/messages/`
- `/v1/messages/channels/`
- `/v1/chat/template`
- `/v2/chat/module`

Observed chat classes include `ChatListActivity`, `ChatRoomActivity`, `ChatSendMessageWorker`, `ChatRemoteDataSource`, and local `ChatDatabase` persistence.

## Excluded from APO Lite

Call/media interfaces are intentionally excluded:

- `/v1/call/*`
- `/v1/turn/credentials`
- `/v2/media/upload`
- bundled WebRTC/native call libraries

## Next mapping work

1. Resolve exact request/response DTO fields for login/token refresh.
2. Resolve active-shipment list/detail response fields.
3. Trace Packing / Ready-to-Ship / Confirmation methods to their HTTP calls.
4. Resolve text-message send/list/read contracts.
5. Wire only authorized production/staging interfaces into the Lite client while retaining server-side validation.
