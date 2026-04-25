# UTS Mobile 1 — Seminar Registration

Aplikasi pendaftaran seminar berbasis Android dengan UI Bento Box / Material Design 3.

## Video Demo

[![Demo Aplikasi](https://img.youtube.com/vi/B_2cmsIN598/0.jpg)](https://www.youtube.com/watch?v=B_2cmsIN598)

## Fitur

| Halaman | Deskripsi |
|---------|-----------|
| **Login** | Form login dengan validasi email & password real-time |
| **Register** | Form registrasi dengan validasi nama, email, password (min 8 karakter), dan konfirmasi password |
| **Home** | Landing page dengan welcome card, seminar grid (Bento Box), dan newsletter subscription |
| **Seminar List** | List seminar dengan featured card, filter chips, dan navigasi ke form pendaftaran |
| **Form Pendaftaran** | Formulir lengkap (nama, email, HP, gender, seminar dropdown, checkbox agreement) dengan validasi per-field |
| **Result** | Halaman sukses dengan ringkasan pendaftaran dan ID registrasi otomatis |

## Teknologi

- **Kotlin** — bahasa utama
- **ViewBinding** — akses view tanpa `findViewById`
- **Material Design 3** — komponen UI (MaterialCardView, MaterialButton, TextInputLayout, dll.)
- **ConstraintLayout** — layout utama dengan Guideline untuk grid responsif
- **RecyclerView** — list seminar dengan adapter
- **Edge-to-Edge** — tampilan full-screen dengan WindowInsets

## Struktur Project

```
app/src/main/java/com/example/uts_mobile1/
├── auth/
│   ├── LoginActivity.kt          # Halaman login
│   └── RegisterActivity.kt       # Halaman registrasi
├── home/
│   └── HomeFragment.kt           # Halaman utama
├── seminar/
│   ├── SeminarFragment.kt        # List seminar
│   ├── SeminarAdapter.kt         # RecyclerView adapter
│   └── SeminarFormActivity.kt    # Form pendaftaran
├── result/
│   └── ResultActivity.kt         # Hasil pendaftaran
├── ui/
│   └── Constants.kt             # Data seminar & intent keys
├── util/
│   └── ValidationUtils.kt        # Validasi (sealed class ValidationResult)
└── MainActivity.kt               # Main activity + bottom navigation
```

## Dokumentasi

Dokumentasi teknis lengkap ada di folder [`docs/`](docs/):

| File | Isi |
|------|-----|
| [01-halaman-login.md](docs/01-halaman-login.md) | ViewBinding, Edge-to-Edge, TextWatcher, Intent Flags |
| [02-halaman-utama.md](docs/02-halaman-utama.md) | Fragment lifecycle, Fragment ViewBinding pattern, Guideline |
| [03-halaman-form-pendaftaran.md](docs/03-halaman-form-pendaftaran.md) | AutoCompleteTextView, RadioGroup, CheckBox error, AppBar |
| [04-validasi-realtime.md](docs/04-validasi-realtime.md) | Sealed class, object singleton, 2-layer validation |
| [05-dialog-konfirmasi.md](docs/05-dialog-konfirmasi.md) | MaterialAlertDialog, Intent extras, Constants |
| [06-halaman-hasil.md](docs/06-halaman-hasil.md) | Elvis operator, ID generation, ConstraintLayout percent |
| [07-rencana-video.md](docs/07-rencana-video.md) | Rencana video demo per bagian |

## Build & Run

```bash
./gradlew assembleDebug    # Build debug APK
./gradlew assembleRelease  # Build release APK
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`