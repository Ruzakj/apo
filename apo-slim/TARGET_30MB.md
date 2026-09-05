# APO Slim — Target 30 MiB

## Baseline audit

APK: `apo-4-9-0.apk`

- Baseline: **89.19 MiB**
- Target: **30.00 MiB**
- Required reduction: **59.19 MiB**
- SHA256: `db8ea7bbc5c16fa042f080e214b318e2c597e5f5e76d4c0efe29ea34ad4b46bf`

## Size composition

| Category | Compressed size |
|---|---:|
| Native libraries (`lib/`) | 67.00 MiB |
| Resources (`res/`) | 14.14 MiB |
| DEX | 5.59 MiB |
| `resources.arsc` | 1.07 MiB |
| Assets | 0.89 MiB |
| Other | 0.09 MiB |

## ABI analysis

| ABI | Native payload |
|---|---:|
| x86_64 | 19.56 MiB |
| x86 | 19.12 MiB |
| arm64-v8a | 17.38 MiB |
| armeabi-v7a | 10.94 MiB |

Target device is iQOO Z9x / arm64-v8a.

Removing non-arm64 native payload has a theoretical saving of **49.62 MiB**, resulting in approximately **39.56 MiB**.

## Path from 39.56 MiB to ~30 MiB

The largest remaining arm64 native payload is:

- `lib/arm64-v8a/libjingle_peerconnection_so.so` — **10.85 MiB**

This is a WebRTC/peer-connection native library. If voice/video/WebRTC functionality is confirmed to be isolated and not required by launch, auth/session, order flows, packing, Ready to Ship, confirmation, or text-only chat, excluding this library would produce a theoretical payload of approximately:

**39.56 MiB - 10.85 MiB = 28.71 MiB**

This meets the size target with margin.

## Additional large payloads — REVIEW only

- `lib/arm64-v8a/libbarhopper_v3.so` — 4.72 MiB (barcode/vision related; likely relevant to scanning and therefore not a default removal candidate)
- `lib/arm64-v8a/libsqliteJni.so` — 1.25 MiB (database/session/state dependency risk; KEEP pending proof)
- `res/xF1.webp` — 2.42 MiB
- `res/eV.webp` — 1.48 MiB
- `res/7J.webp` — 1.06 MiB
- `res/zv.webp` — 1.05 MiB
- ML Kit barcode model assets — ~0.84 MiB total (barcode dependency; KEEP pending proof)

## Decision gates

### KEEP

- login / OTP / session
- endpoint/API configuration
- device / Play integrity
- location validation
- order list/detail
- packing
- Ready to Ship
- confirmation
- text chat
- barcode/scanning components unless proven unused by retained flows
- DEX / manifest / resources.arsc by default

### SAFE CANDIDATE

- non-target ABIs: x86, x86_64, armeabi-v7a, for an arm64-only experimental variant

### REVIEW CANDIDATE

- arm64 WebRTC library (`libjingle_peerconnection_so.so`) because voice/video are outside APO Slim scope
- oversized decorative WebP resources only after reference/use mapping

## Target stages

1. **Stage A — arm64-only:** ~39.56 MiB theoretical.
2. **Stage B — arm64-only + no WebRTC native payload:** ~28.71 MiB theoretical.
3. Validate launch and all retained workflows before considering any further resource removal.

Important: modifying the APK changes its package contents/signature state. A slimmed experimental APK must be treated as a separate test artifact unless it can be rebuilt and signed through an authorized release pipeline. Do not bypass signature, integrity, auth, or server-side security checks.
