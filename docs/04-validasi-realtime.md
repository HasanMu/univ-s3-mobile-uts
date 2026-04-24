# Validasi Real-Time Error

## Gambaran Umum

Aplikasi mengimplementasikan sistem validasi real-time yang memberikan umpan balik langsung saat pengguna mengetik. Validasi menggunakan **sealed class** `ValidationResult` dan **singleton object** `ValidationUtils` yang terpusat, sehingga aturan validasi konsisten di seluruh halaman (Login, Register, dan Form Pendaftaran).

## Arsitektur

```
ValidationUtils.kt (Singleton Object — shared utility)
├── ValidationResult (Sealed Class)
│   ├── Valid   — input lolos validasi, tidak ada error
│   └── Error(message: String) — input gagal, pesan error tersedia
│
├── validateNama(nama)
├── validateEmail(email)
├── validateHp(hp)
├── validateGender(selected)
├── validateSeminar(seminar)
├── validateAgreement(checked)
├── validatePassword(password)
├── validatePasswordRequired(password)
└── validateConfirmPassword(password, confirmPassword)
```

## Konsep Teknis

### Sealed Class — `ValidationResult`

```kotlin
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

**Sealed class** adalah class khusus di Kotlin yang hanya bisa memiliki subclass yang didefinisikan di dalam file yang sama. Ini berbeda dari abstract class biasa karena compiler **tahu semua kemungkinan subclass** — membuat `when` expression menjadi **exhaustive** (wajib menangani semua kasus).

**Kenapa sealed class, bukan boolean atau null?**

```kotlin
// Pendekatan buruk: Boolean
fun validateEmail(email: String): Boolean
// Masalah: Tidak bisa membawa pesan error. Hanya tahu "valid" atau "tidak valid".

// Pendekatan buruk: String? (null = valid, string = error)
fun validateEmail(email: String): String?
// Masalah: Tidak type-safe. Bisa lupa null check, atau salah arti null.

// Pendekatan baik: Sealed class (kita pakai ini)
fun validateEmail(email: String): ValidationResult
// Keuntungan: Type-safe, wajib handle semua kasus, bisa bawa pesan error.
```

**Cara penggunaan dengan `when`:**

```kotlin
val result = ValidationUtils.validateEmail(email)
when (result) {
    is ValidationResult.Error -> {
        binding.tilEmail.error = result.message  // akses .message type-safe
        isValid = false
    }
    else -> {
        binding.tilEmail.error = null
    }
}
```

**Keuntungan sealed class:**
1. **Type-safe**: Compiler memastikan kita handle `Error` dan `Valid`.
2. **Exhaustive when**: Kalau kita tambah subclass baru (mis. `Warning`), compiler akan error di semua `when` yang belum menangani `Warning`.
3. **Bawa data**: `Error(message)` membawa pesan error tanpa perlu class tambahan.
4. **Null-free**: Tidak perlu null check — `Valid` dan `Error` keduanya non-null.

---

### `object` Keyword — `ValidationUtils` sebagai Singleton

```kotlin
object ValidationUtils {
    fun validateNama(nama: String): ValidationResult { ... }
    fun validateEmail(email: String): ValidationResult { ... }
    // ...
}
```

Keyword `object` di Kotlin mendeklarasikan **singleton** — hanya ada **satu instance** dari class ini di seluruh aplikasi. Kita tidak perlu membuat instance dengan `ValidationUtils()` — cukup panggil langsung:

```kotlin
ValidationUtils.validateEmail("test@email.com")  // langsung callable
```

Kenapa singleton? Karena `ValidationUtils` **tidak punya state** — semua method menerima input dan mengembalikan output tanpa menyimpan data apapun. Singleton lebih efisien karena:
1. Tidak perlu bikin object baru setiap kali validasi dipanggil
2. Semua Activity/Fragment berbagi instance yang sama
3. Method bisa dipanggil dari mana saja tanpa dependency injection

**Perbandingan tanpa `object`:**

```kotlin
// Tanpa object — harus buat instance setiap kali (tidak perlu karena tidak ada state)
val validator = ValidationUtils()     // bikin instance
validator.validateEmail("a@b.com")    // panggil method

// Dengan object — langsung panggil, tidak perlu instance
ValidationUtils.validateEmail("a@b.com")
```

---

### `Patterns.EMAIL_ADDRESS` — Regex Built-in Android

```kotlin
!Patterns.EMAIL_ADDRESS.matcher(email).matches()
```

Android menyediakan `Patterns.EMAIL_ADDRESS` — sebuah regex pattern bawaan yang memvalidasi format email sesuai RFC. Ini lebih reliable daripada regex buatan sendiri karena sudah ditest dan di-maintain oleh Android team.

Validasi email kita punya 3 layer:
```kotlin
fun validateEmail(email: String): ValidationResult {
    return when {
        email.isBlank() → Error("Email tidak boleh kosong")
        !email.contains("@") → Error("Format email tidak valid")    // cek cepat
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() → Error("Format email tidak valid")  // cek ketat
        else → Valid
    }
}
```

Kenapa ada `contains("@")` sebelum `Patterns.EMAIL_ADDRESS`? Karena `Patterns.EMAIL_ADDRESS` agak lambat (regex matching), dan cek `contains("@")` adalah **fast path** — kalau email tidak punya `@`, kita langsung return error tanpa perlu menjalankan regex.

---

### Dua Lapis Validasi — Real-time dan Submit

Validasi di aplikasi bekerja di **2 lapisan**:

| Layer | Trigger | Perilaku | Tujuan |
|-------|---------|----------|--------|
| **Real-time** | Setiap ketik (`afterTextChanged`) | Validasi field yang sedang diketik | Umpan balik langsung saat user memperbaiki error |
| **Submit** | Klik tombol submit/login/register | Validasi SEMUA field sekaligus | Memastikan tidak ada yang lolos saat submit |

**Kenapa butuh keduanya?**

Kalau hanya ada validasi submit, user harus tekan tombol dulu baru tahu ada error — UX yang buruk. Kalau hanya ada validasi real-time, user bisa submit tanpa validasi menyeluruh (mis. field yang belum diketik sama sekali belum pernah divalidasi).

**Pola di Login (clear-on-type):**

```kotlin
// Saat user mulai mengetik → hapus SEMUA error (bukan divalidasi)
private val textWatcher = object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        clearError()  // hapus error di semua field
    }
}
```

**Pola di Register/SeminarForm (per-field):**

```kotlin
// Saat user mengetik di field Nama → hanya validasi field Nama
binding.etNama.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        val result = ValidationUtils.validateNama(s?.toString() ?: "")
        binding.tilNama.error = if (result is ValidationResult.Error) result.message else null
    }
})
```

---

### Error Display Per Komponen — Mengapa Berbeda?

Setiap jenis komponen punya cara menampilkan error yang berbeda:

| Komponen | Cara Error | Mengapa? |
|----------|-----------|----------|
| TextInputLayout | `tilField.error = "message"` | Punya built-in error display — animasi merah di bawah field |
| TextView (gender label) | `tvGenderLabel.setTextColor(getColor(R.color.error))` | RadioGroup bukan TextInputLayout — tidak punya `.error`. Alternatif: ubah warna label. |
| MaterialCheckBox | `cbAgreement.error = "message"` | MaterialCheckBox punya property `.error` bawaan dari Material Design. |

**Reset error pun berbeda:**

| Komponen | Cara Reset |
|----------|-----------|
| TextInputLayout | `tilField.error = null` |
| TextView (gender label) | `tvGenderLabel.setTextColor(getColor(R.color.on_surface_variant))` |
| MaterialCheckBox | `cbAgreement.error = null` atau via `setOnCheckedChangeListener` |

---

## Aturan Validasi Detail

### 1. `validateNama(nama: String)`

| Kondisi | Hasil | Pesan Error |
|---------|-------|-------------|
| `nama.isBlank()` | Error | "Nama tidak boleh kosong" |
| lainnya | Valid | — |

Digunakan di: `RegisterActivity`, `SeminarFormActivity`

### 2. `validateEmail(email: String)`

| Kondisi | Hasil | Pesan Error |
|---------|-------|-------------|
| `email.isBlank()` | Error | "Email tidak boleh kosong" |
| `!email.contains("@")` | Error | "Format email tidak valid" |
| `!Patterns.EMAIL_ADDRESS.matcher(email).matches()` | Error | "Format email tidak valid" |
| lainnya | Valid | — |

Digunakan di: `LoginActivity`, `RegisterActivity`, `SeminarFormActivity`, `HomeFragment` (newsletter)

### 3. `validateHp(hp: String)`

| Kondisi | Hasil | Pesan Error |
|---------|-------|-------------|
| `hp.isBlank()` | Error | "Nomor HP tidak boleh kosong" |
| `!hp.all { it.isDigit() }` | Error | "Nomor HP harus hanya angka" |
| `hp.length < 10 \|\| hp.length > 13` | Error | "Nomor HP harus 10-13 digit" |
| `!hp.startsWith("08")` | Error | "Nomor HP harus dimulai dengan 08" |
| lainnya | Valid | — |

**Urutan validasi penting!** Cek `isBlank()` dulu (sebelum `all { isDigit() }`) karena string kosong akan pass `all { isDigit() }` (vacuous truth). Cek `isDigit()` sebelum cek panjang agar pesan error lebih spesifik.

### 4. `validateGender(selected: Boolean)`

| Kondisi | Hasil | Pesan Error |
|---------|-------|-------------|
| `selected == false` | Error | "Pilih jenis kelamin" |
| `selected == true` | Valid | — |

Note: Validasi gender mengubah `textColor` label, bukan menggunakan `TextInputLayout.error`.

### 5. `validateSeminar(seminar: String?)`

| Kondisi | Hasil | Pesan Error |
|---------|-------|-------------|
| `seminar.isNullOrBlank()` | Error | "Pilih seminar" |
| lainnya | Valid | — |

Note: Parameter bertipe `String?` (nullable) karena `AutoCompleteTextView.text` bisa null.

### 6. `validateAgreement(checked: Boolean)`

| Kondisi | Hasil | Pesan Error |
|---------|-------|-------------|
| `checked == false` | Error | "Anda harus menyetujui syarat dan ketentuan" |
| `checked == true` | Valid | — |

### 7. `validatePassword(password: String)`

| Kondisi | Hasil | Pesan Error |
|---------|-------|-------------|
| `password.isBlank()` | Error | "Password tidak boleh kosong" |
| `password.length < 8` | Error | "Password minimal 8 karakter" |
| lainnya | Valid | — |

Digunakan di: `RegisterActivity` (registrasi memerlukan minimal 8 karakter)

### 8. `validatePasswordRequired(password: String)`

| Kondisi | Hasil | Pesan Error |
|---------|-------|-------------|
| `password.isBlank()` | Error | "Password tidak boleh kosong" |
| lainnya | Valid | — |

Digunakan di: `LoginActivity` (login hanya memerlukan field tidak kosong, tidak cek panjang karena password sudah dibuat saat registrasi)

**Kenapa ada 2 versi validasi password?** Karena konteksnya berbeda:
- **Registrasi**: User MEMBUAT password baru → harus minimal 8 karakter (keamanan)
- **Login**: User MEMASUKKAN password yang sudah ada → tidak perlu cek panjang (user sudah buat password panjangnya berapa)

### 9. `validateConfirmPassword(password: String, confirmPassword: String)`

| Kondisi | Hasil | Pesan Error |
|---------|-------|-------------|
| `confirmPassword.isBlank()` | Error | "Konfirmasi password tidak boleh kosong" |
| `password != confirmPassword` | Error | "Password tidak cocok" |
| lainnya | Valid | — |

## Activity-Specific Validation Summary

### LoginActivity
| Field | Validasi | Real-time? | Pola |
|-------|----------|------------|------|
| Email | `validateEmail` | Clear-on-type | Hapus semua error saat mengetik |
| Password | `validatePasswordRequired` | Clear-on-type | Hapus semua error saat mengetik |

### RegisterActivity
| Field | Validasi | Real-time? | Pola |
|-------|----------|------------|------|
| Nama | `validateNama` | ✅ Per-field | Validasi hanya field yang diketik |
| Email | `validateEmail` | ✅ Per-field | Validasi hanya field yang diketik |
| Password | `validatePassword` (min 8) | ✅ Per-field | Validasi hanya field yang diketik |
| Confirm Password | `validateConfirmPassword` | ✅ Per-field | Validasi hanya field yang diketik |

### SeminarFormActivity
| Field | Validasi | Real-time? | Pola |
|-------|----------|------------|------|
| Nama | `validateNama` | ✅ Per-field | Validasi hanya field yang diketik |
| Email | `validateEmail` | ✅ Per-field | Validasi hanya field yang diketik |
| Nomor HP | `validateHp` | ✅ Per-field | Validasi hanya field yang diketik |
| Gender | Color change on label | ✅ OnCheckedChangeListener | Update warna saat pilih |
| Seminar | Clear error on text change | ✅ Per-field | Hapus error saat mulai mengetik |
| Agreement | `validateAgreement` | ✅ OnCheckedChangeListener | Hapus error saat centang |

## File Terkait

| File | Path |
|------|------|
| ValidationUtils + ValidationResult | `util/ValidationUtils.kt` |
| LoginActivity | `auth/LoginActivity.kt` |
| RegisterActivity | `auth/RegisterActivity.kt` |
| SeminarFormActivity | `seminar/SeminarFormActivity.kt` |