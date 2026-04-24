# Halaman Hasil (Result Activity)

## Gambaran Umum

Halaman hasil ditampilkan setelah pengguna berhasil mendaftar seminar. Halaman ini menampilkan ringkasan pendaftaran dalam card terstruktur, termasuk informasi seminar, jadwal, lokasi, dan ID registrasi yang di-generate otomatis. Setelah melihat hasil, user bisa kembali ke Home.

## Arsitektur

```
ResultActivity (Activity)
├── activity_result.xml (Layout — ConstraintLayout + NestedScrollView + fixed button)
├── ResultActivity.kt (Logika)
└── Constants.IntentKeys (Data diterima via Intent extras)
```

## Alur Navigasi

```
SeminarFormActivity ──[dialog "Ya"]──▶ ResultActivity (dengan Intent extras)
ResultActivity ──[btnPrimary "Kembali ke Home"]──▶ MainActivity (FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK)
```

> **Catatan**: `finish()` dipanggil di `SeminarFormActivity` setelah navigasi ke ResultActivity, sehingga user TIDAK bisa tekan back untuk kembali ke form. Tombol "Download E-Tiket" sudah dihapus — hanya tombol "Kembali ke Home" yang tersisa.

## Konsep Teknis

### Intent Data Extraction — `getStringExtra()` dengan Null Coalescing

```kotlin
val nama = intent.getStringExtra(Constants.IntentKeys.NAMA) ?: ""
val email = intent.getStringExtra(Constants.IntentKeys.EMAIL) ?: ""
val hp = intent.getStringExtra(Constants.IntentKeys.HP) ?: ""
val gender = intent.getStringExtra(Constants.IntentKeys.GENDER) ?: ""
val seminar = intent.getStringExtra(Constants.IntentKeys.SEMINAR) ?: ""
```

**`intent.getStringExtra()`** mengembalikan **`String?`** (nullable) — bisa null kalau:
1. Key tidak ditemukan di Intent
2. Value memang null
3. Activity dibuka tanpa Intent extras (mis. dari deep link)

Operator **`?: ""`** (Elvis operator) memberikan default value `""` (string kosong) kalau hasilnya null. Ini mencegah crash dari `NullPointerException` dan memastikan UI tidak menampilkan "null" sebagai teks.

**Tanpa Elvis operator:**
```kotlin
val nama: String? = intent.getStringExtra("nama")
binding.tvNama.text = nama  // Kalau null, TextView akan tampilkan "null" — tidak diinginkan
```

**Dengan Elvis operator:**
```kotlin
val nama: String = intent.getStringExtra("nama") ?: ""
binding.tvNama.text = nama  // Kalau null, pakai "" — aman
```

---

### ID Registrasi Generation

```kotlin
val regId = "OC-${System.currentTimeMillis().toString().takeLast(8)}"
binding.tvId.text = regId
```

Cara kerjanya:

1. **`System.currentTimeMillis()`**: Mengembalikan waktu saat ini dalam milidetik sejak 1 Januari 1970 (Unix epoch). Contoh: `1713958392745`
2. **`.toString()`**: Konversi ke string: `"1713958392745"`
3. **`.takeLast(8)`**: Ambil 8 karakter terakhir: `"95839274"`
4. **`"OC-${...}"`**: String template — tambah prefix "OC-"

Hasil: **`OC-95839274`** — format ID registrasi yang unik berdasarkan waktu.

Kenapa `takeLast(8)` bukan seluruh timestamp?
- Timestamp lengkap terlalu panjang (`OC-1713958392745`) — sulit dibaca
- 8 digit terakhir cukup unik dalam konteks waktu dekat (collision hanya terjadi jika 2 pendaftaran terjadi di milidetik yang sama)
- Format yang lebih pendek lebih mudah diingat dan dikirim

---

### When Expression untuk Gender Mapping

```kotlin
val genderId = binding.rgGender.checkedRadioButtonId
val gender = when (genderId) {
    R.id.rbLaki -> getString(R.string.laki_laki)     // "Laki-laki"
    R.id.rbPerempuan -> getString(R.string.perempuan)  // "Perempuan"
    else -> ""
}
putExtra(Constants.IntentKeys.GENDER, gender)
```

Ini terjadi di `SeminarFormActivity`, bukan ResultActivity. Penjelasannya:

1. **`checkedRadioButtonId`**: Mengembalikan ID resource dari RadioButton yang dipilih, atau `-1` kalau belum ada yang dipilih
2. **`when` expression**: Kotlin memetakan ID → string yang sesuai
3. **`getString(R.string.laki_laki)`**: Mengambil string dari resource (bukan hardcoded) agar mendukung multiple language (i18n)
4. **`else -> ""`**: Fallback — seharusnya tidak tercapai karena validasi sudah memastikan gender dipilih

---

### ConstraintLayout — Tanggal & Waktu Side by Side

```xml
<TextView
    android:id="@+id/tvLabelTanggal"
    android:layout_width="0dp"
    android:layout_constraintWidth_percent="0.5" />

<TextView
    android:id="@+id/tvLabelWaktu"
    android:layout_width="0dp"
    android:layout_constraintStart_toEndOf="@id/tvLabelTanggal"
    android:layout_constraintWidth_percent="0.5" />
```

Layout tanggal dan waktu menggunakan **`layout_constraintWidth_percent="0.5"`** — masing-masing mengambil 50% lebar parent. Ini membuat dua kolom yang seimbang:

```
┌─────────────────────────────────────────┐
│  TANGGAL            │  WAKTU            │
│  24 Oktober 2024    │  14:00 - 16:30 WIB │
└─────────────────────────────────────────┘
```

Kenapa `0dp` untuk `layout_width`? Karena di ConstraintLayout, kalau view di-constrain ke kedua sisi (start dan end), lebar harus `0dp` (MATCH_CONSTRAINT) agar constraint bisa mengontrol lebar. `0dp` berarti "lebar ditentukan oleh constraint, bukan oleh width sendiri".

---

### MaterialCardView Stroke

```xml
<com.google.android.material.card.MaterialCardView
    app:strokeWidth="1dp"
    app:strokeColor="#1A747D75" ...>
```

- **`strokeWidth="1dp"`**: Ketebalan garis border card. 1dp memberikan garis tipis yang subt tetapi terlihat.
- **`strokeColor="#1A747D75"`**: Warna border dalam format ARGB (Alpha-Red-Green-Blue).
  - `1A` = Alpha (26/255 ≈ 10% opacity) — sangat transparan
  - `747D75` = Warna abu-abu

Mengapa stroke transparan? Karena kita ingin border yang **sangat subtle** — terlihat saat background terang tapi tidak mengganggu desain. Ini teknik Material Design untuk membuat card terlihat "terpisah" tanpa shadow yang berat.

---

### `fontFamily="monospace"` — Font untuk ID Registrasi

```xml
<TextView
    android:id="@+id/tvId"
    android:fontFamily="monospace" ...>
```

Font **monospace** (setiap karakter memiliki lebar yang sama) digunakan untuk ID registrasi karena:
1. **Readability**: Kode seperti `OC-95839274` lebih mudah dibaca kalau setiap karakter seimbang
2. **Professional look**: Kode/ID biasanya menggunakan monospace di aplikasi tiket/reservasi
3. **Alignment**: Karakter seperti `1` dan `W` memiliki lebar sama — tidak ada pergeseran visual

---

### `letterSpacing` dan `textAllCaps` — Label Styling

```xml
<TextView
    android:textAllCaps="true"
    android:letterSpacing="0.2"
    android:text="ID REGISTRASI" ...>
```

- **`textAllCaps="true"`**: Memaksa semua karakter jadi huruf besar. Digunakan untuk label yang seharusnya uppercase, sehingga kita bisa menulis `R.string.id_registrasi` dengan value biasa dan Android yang mengkonversi.
- **`letterSpacing="0.2"`**: Jarak antar karakter sebesar 0.2em. Ini memberikan efek "expanded" yang sering digunakan untuk label/caption — membuat teks terlihat lebih stylish dan lebih mudah dibaca sebagai heading.

Contoh visual:
```
Tanpa letterSpacing:  IDREGISTRASI
Dengan 0.2:           I D   R E G I S T R A S I
```

---

### Bottom-Fixed Button Pattern

```xml
<!-- Primary Button — constrained to parent bottom -->
<MaterialButton
    android:id="@+id/btnPrimary"
    app:layout_constraintBottom_toBottomOf="parent" />

<!-- ScrollView — fills remaining space -->
<NestedScrollView
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

Button "Kembali ke Home" di-constrain ke `bottom` dari parent, sehingga ia selalu berada di bawah layar terlepas dari seberapa banyak konten yang di-scroll. NestedScrollView mengisi sisa ruang di atasnya.

Kenapa tidak menaruh button di dalam ScrollView? Karena kalau konten sangat panjang dan user scroll ke bawah, button bisa tidak terlihat (tersembunyi di bawah fold). Dengan pattern ini, button selalu visible di bottom.

---

### NestedScrollView Constraint — Bottom

```xml
<NestedScrollView
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintTop_toTopOf="parent">
```

Sebelumnya, `NestedScrollView` di-constrain ke `Bottom_toTopOf="@id/btnSecondary"` (yang sudah dihapus). Sekarang di-constrain ke `Bottom_toBottomOf="parent"` karena hanya ada satu button (btnPrimary) yang menggunakan ConstraintLayout bottom positioning.

ScrollView mengisi seluruh layar, dan button ditempatkan di atas bottom menggunakan constraint positioning — bukan di dalam ScrollView.

## Struktur Layout

```
ConstraintLayout (root, bg=background)
├── NestedScrollView (scrollView, bottom=parent)
│   └── ConstraintLayout (padded, screen_padding_horizontal)
│       ├── ImageView (ivSuccess, 96dp, ic_check_circle, bg_circle_primary)
│       ├── TextView (tvSuccessTitle, "Pendaftaran Berhasil")
│       ├── TextView (tvSuccessSubtitle, "Terima kasih...")
│       └── MaterialCardView (cardSummary, stroke=1dp)
│           └── ConstraintLayout (padded, 32dp)
│               ├── [CARD TITLE] "RINGKASAN PENDAFTARAN"
│               ├── [SEMINAR] Label + Value
│               ├── [TANGGAL + WAKTU] side by side (50% each)
│               ├── [LOKASI] with ic_videocam icon
│               ├── [DIVIDER] 1dp line
│               ├── [ID REGISTRASI] Label + Value (monospace)
│               └── [CONFIRMED BADGE] "CONFIRMED"
│
└── MaterialButton (btnPrimary, "Kembali ke Home", constrained to bottom)
```

## Logika Kotlin (`ResultActivity.kt`)

### Setup Data dari Intent

```kotlin
private fun setupViews() {
    val nama = intent.getStringExtra(Constants.IntentKeys.NAMA) ?: ""
    val email = intent.getStringExtra(Constants.IntentKeys.EMAIL) ?: ""
    val hp = intent.getStringExtra(Constants.IntentKeys.HP) ?: ""
    val gender = intent.getStringExtra(Constants.IntentKeys.GENDER) ?: ""
    val seminar = intent.getStringExtra(Constants.IntentKeys.SEMINAR) ?: ""

    binding.tvSeminar.text = seminar.ifBlank { "-" }

    val regId = "OC-${System.currentTimeMillis().toString().takeLast(8)}"
    binding.tvId.text = regId

    // Placeholder data (belum di-lookup dari Constants.SeminarList)
    binding.tvTanggal.text = "24 Oktober 2024"
    binding.tvWaktu.text = "14:00 - 16:30 WIB"
    binding.tvLokasi.text = "Virtual via Curator Live Stream"
}
```

**`seminar.ifBlank { "-" }`**: Extension function Kotlin yang menampilkan `"-"` kalau string kosong. Ini lebih baik daripada menampilkan string kosong di UI.

**Placeholder data**: Nilai tanggal, waktu, dan lokasi saat ini masih hardcoded. Idealnya, data ini di-lookup dari `Constants.SeminarList` berdasarkan seminar yang dipilih, atau diteruskan melalui Intent extras.

### Navigasi Kembali ke Home

```kotlin
private fun navigateToHome() {
    val intent = Intent(this, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
}
```

Sama seperti navigasi login → main: flags `NEW_TASK | CLEAR_TASK` menghapus seluruh back stack sehingga user tidak bisa kembali ke halaman form atau result setelah menekan tombol back.

## Data Seminar (Constants.SeminarList)

| ID | Judul | Tanggal | Lokasi | Harga | Kuota |
|----|-------|---------|--------|-------|-------|
| 1 | Seminar Android | 12 Okt 2023 | Jakarta, ID | Rp 250.000 | 45 |
| 2 | Seminar UI/UX | 15 Okt 2023 | Online | Gratis | 100 |
| 3 | Seminar AI | 15 Okt 2024 | Jakarta & Online | Rp 500.000 | 50 |
| 4 | Seminar Cyber Security | 20 Okt 2024 | Jakarta | Rp 300.000 | 30 |
| 5 | Seminar Data Science | 25 Okt 2024 | Online | Gratis | 75 |

## Binding IDs Referensi

| View ID | Tipe | Fungsi |
|---------|------|--------|
| `ivSuccess` | ImageView | Ikon centang berhasil (96dp, bg_circle_primary) |
| `tvSuccessTitle` | TextView | Judul "Pendaftaran Berhasil" |
| `tvSuccessSubtitle` | TextView | Subtitle terima kasih |
| `cardSummary` | MaterialCardView | Card ringkasan pendaftaran |
| `tvLabelSeminar` | TextView | Label "SEMINAR" |
| `tvSeminar` | TextView | Value nama seminar |
| `tvLabelTanggal` / `tvTanggal` | TextView | Label & value tanggal |
| `tvLabelWaktu` / `tvWaktu` | TextView | Label & value waktu |
| `tvLabelLokasi` / `tvLokasi` | TextView | Label & value lokasi |
| `ivLokasiIcon` | ImageView | Ikon videocam |
| `idDivider` | View | Pembatas garis horizontal |
| `tvLabelId` / `tvId` | TextView | Label "ID REGISTRASI" & value (monospace) |
| `chipConfirmed` | TextView | Badge "CONFIRMED" |
| `btnPrimary` | MaterialButton | "Kembali ke Home" |

## Resource Terkait

| Resource | Path |
|----------|------|
| Layout XML | `res/layout/activity_result.xml` |
| Activity Kotlin | `result/ResultActivity.kt` |
| Constants | `ui/Constants.kt` (IntentKeys, SeminarList) |
| Badge Background | `res/drawable/bg_confirmed_badge.xml` |
| Circle Background | `res/drawable/bg_circle_primary.xml` |
| Check Icon | `res/drawable/ic_check_circle.xml` |
| Video Icon | `res/drawable/ic_videocam.xml` |
| Strings | `res/values/strings.xml` |