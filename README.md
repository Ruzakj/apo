# APO Slim

Repository ini sekarang berfokus pada **APO Slim**: audit dan optimasi ukuran APK asli APO tanpa mengganti alur autentikasi, session, API, location validation, atau integrity validation.

## Baseline APK

- `apo-4-9-0.apk`
- Ukuran: 93,520,739 byte (~89.19 MiB)
- Target perangkat utama: iQOO Z9x / arm64-v8a

## Fokus yang dipertahankan

- Login / OTP / session asli
- API dan endpoint asli
- Device / Play integrity
- Location validation
- Pesanan
- Packing
- Ready to Ship
- Konfirmasi
- Chat teks

## Kandidat pengurangan ukuran

Optimasi hanya dilakukan setelah audit menunjukkan bahwa komponen dapat dipisahkan tanpa mengganggu workflow inti. Kandidat utama:

- ABI/native library non-target
- resource density non-target
- asset/media besar yang tidak dibutuhkan workflow utama
- voice/video/WebRTC jika terbukti terisolasi
- duplicate/unreferenced resource yang aman

## Audit

Tool audit read-only tersedia di:

```bash
python3 apo-slim/audit_apk.py apo-4-9-0.apk
```

Laporan menampilkan distribusi ukuran APK berdasarkan kategori, ABI, extension, dan file terbesar.

Lihat `apo-slim/README.md` untuk tahapan APO Slim.

## QA utility lama

Folder `app/` masih berisi APO Location QA untuk pengujian mock location melalui mekanisme resmi Android Developer Options. Utility tersebut terpisah dari APO Slim dan tidak digunakan untuk melewati integrity/anti-mock validation.
