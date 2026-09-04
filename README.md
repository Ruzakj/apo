# APO Location QA

Android QA utility for testing location-dependent flows using Android's official **Select mock location app** mechanism in Developer Options.

## What it does
- Enter latitude/longitude manually.
- Nudge the coordinate north/south/east/west.
- Start/stop a foreground mock-location session.
- Open Developer Options to select this app as the mock location app.
- Open the selected coordinate in a maps application.

## Setup
1. Install the debug APK.
2. Enable Android Developer Options.
3. Open **Select mock location app** and choose **APO Location QA**.
4. Return to the app, enter coordinates, and start the QA location.
5. Stop the session when finished to return to normal device location behavior.

## Build
GitHub Actions builds `app-debug.apk` on pushes to `main`.

This project is intended for QA/testing of software you are authorized to test. It does not attempt to hide Android's mock-location status or bypass integrity/anti-mock checks.
