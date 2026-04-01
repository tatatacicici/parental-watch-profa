# Parental Watch

Aplikasi Android untuk pemantauan dan kontrol perangkat anak secara real-time.

## Deskripsi

**Parental Watch** adalah aplikasi parental control berbasis Android yang memungkinkan orang tua untuk memantau dan membatasi penggunaan perangkat oleh anak. Aplikasi ini menggunakan sistem dual-mode: **Mode Orang Tua** untuk pengelolaan dan **Mode Anak** untuk tampilan terpantau.

## Fitur Utama

- **Mode Orang Tua**: Akses panel kontrol penuh dengan autentikasi PIN
- **Mode Anak**: Tampilan terbatas dengan notifikasi bahwa perangkat sedang dipantau
- **Keamanan PIN**: Orang tua dapat mengatur PIN 4–8 digit (di-hash menggunakan SHA-256)
- **Pemblokiran Otomatis**: Setelah 5 kali gagal login, akses dikunci hingga aplikasi di-restart
- **Pemantauan Aplikasi**: Kelola daftar aplikasi yang diizinkan (whitelist)
- **Log Aktivitas**: Lihat riwayat deteksi aktivitas perangkat
- **Toggle Layanan**: Aktifkan/nonaktifkan layanan pemantauan secara langsung dari dashboard

## Teknologi yang Digunakan

| Komponen | Teknologi |
|---|---|
| Bahasa | Kotlin |
| UI | Jetpack Compose + Material3 |
| Navigasi | Navigation Compose 2.7.7 |
| Database lokal | Room 2.6.1 |
| HTTP Client | Retrofit 2.9.0 + Gson Converter |
| Async | Kotlin Coroutines 1.7.3 |
| Build | Gradle dengan KSP |

## Persyaratan Sistem

- **Android minimum**: 8.0 (API level 26)
- **Android target**: API level 36
- **Android Studio**: Versi terbaru yang mendukung Kotlin & Jetpack Compose

## Cara Menjalankan

1. Clone repositori ini:
   ```bash
   git clone https://github.com/tatatacicici/parental-watch-profa.git
   ```
2. Buka proyek di **Android Studio**.
3. Sync Gradle dengan klik **File → Sync Project with Gradle Files**.
4. Jalankan aplikasi pada emulator atau perangkat fisik melalui tombol **Run**.

## Alur Penggunaan

```
Layar Utama
├── Mode Orang Tua
│   ├── (PIN belum diatur) → Layar Pengaturan PIN → Dashboard Orang Tua
│   └── (PIN sudah diatur) → Layar Login PIN → Dashboard Orang Tua
│       ├── Toggle layanan pemantauan
│       ├── Kelola aplikasi yang dipantau
│       ├── Lihat log aktivitas
│       └── Ganti PIN
└── Mode Anak
    └── Layar Mode Anak (tampilan perlindungan aktif)
```

## Izin Aplikasi

| Izin | Kegunaan |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Menampilkan overlay di atas aplikasi lain |
| `BIND_ACCESSIBILITY_SERVICE` | Memantau aktivitas aplikasi anak |
| `RECEIVE_BOOT_COMPLETED` | Memulai layanan pemantauan saat perangkat menyala |
| `INTERNET` | Komunikasi dengan server (jika diperlukan) |

## Struktur Proyek

```
app/src/main/
├── java/com/example/parental_watch/
│   ├── data/
│   │   └── preference/     # Manajemen preferensi (PIN, pengaturan)
│   └── ui/
│       ├── screens/        # Layar-layar aplikasi
│       │   ├── HomeScreen.kt
│       │   ├── PinSetupScreen.kt
│       │   ├── ParentLoginScreen.kt
│       │   ├── ParentDashboardScreen.kt
│       │   └── ChildModeScreen.kt
│       ├── theme/          # Tema, warna, tipografi
│       ├── MainActivity.kt
│       └── Routes.kt       # Definisi rute navigasi
└── res/                    # Resource (string, warna, tema, gambar)
```

## Kontribusi

Proyek ini dikembangkan sebagai bagian dari tugas pemrograman aplikasi framework Android. Kontribusi dan saran pengembangan sangat disambut.

## Lisensi

Proyek ini dibuat untuk keperluan akademik.
