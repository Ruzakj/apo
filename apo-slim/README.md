# APO Slim

APO Slim adalah jalur optimasi APK asli APO 4.9.0 dengan fokus mengecilkan ukuran tanpa mengganti alur autentikasi, session, API, location validation, atau integrity validation.

## Baseline

- Input: `apo-4-9-0.apk`
- Ukuran baseline repository: 93,520,739 bytes (~89.19 MiB)
- Target awal realistis: 30–60 MB, ditentukan setelah audit isi APK.
- Prioritas perangkat: iQOO Z9x / arm64-v8a.

## Prinsip

Komponen kritis tidak disentuh pada fase awal:

- login / OTP / session
- konfigurasi endpoint dan API
- device / Play integrity
- location validation
- signing-related metadata
- order, packing, ready-to-ship, confirmation
- chat teks

Kandidat optimasi hanya dipilih setelah audit ukuran:

- ABI/native library yang tidak dibutuhkan target arm64-v8a
- media/asset besar yang tidak digunakan workflow utama
- density resource yang tidak relevan
- modul voice/video/WebRTC jika benar-benar terisolasi
- duplicate/unreferenced resources yang dapat dibuktikan aman

## Tahapan

1. Audit ZIP/APK tanpa modifikasi.
2. Buat laporan kontribusi ukuran per kategori, ABI, extension, dan file terbesar.
3. Tandai kandidat SAFE / REVIEW / KEEP.
4. Buat variant eksperimental terpisah; APK asli tetap disimpan sebagai baseline.
5. Verifikasi install, launch, login, OTP, session, order, packing, ready-to-ship, confirmation, chat teks.
6. Bila signature/integrity validation gagal, perubahan tersebut dianggap tidak layak untuk jalur production.

Jalankan audit lokal:

```bash
python3 apo-slim/audit_apk.py apo-4-9-0.apk
```

Script hanya membaca APK sebagai ZIP dan tidak mengekstrak kredensial, token, OTP seed, signing key, atau secret lain.
