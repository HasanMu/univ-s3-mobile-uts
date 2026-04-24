# Dialog Konfirmasi

## Gambaran Umum

Aplikasi menggunakan `MaterialAlertDialogBuilder` dari Material Design 3 untuk menampilkan dialog konfirmasi sebelum data pendaftaran seminar dikirim. Dialog ini muncul sebagai langkah verifikasi terakhir — setelah semua field lolos validasi.

## Alur Dialog

```
Form Pendaftaran
  └── User klik "Konfirmasi Pendaftaran"
      └── validateFields()
          ├── Ada error → tampilkan error per field (STOP, dialog TIDAK muncul)
          └── Semua valid → showConfirmationDialog()
              └── MaterialAlertDialogBuilder
                  ├── Title: "Konfirmasi"
                  ├── Message: "Apakah data yang Anda isi sudah benar?"
                  ├── Positive Button "Ya" → navigateToResult()
                  └── Negative Button "Tidak" → dismiss dialog (null listener)
```

**Poin penting**: Dialog **hanya muncul** jika semua field lolos validasi. Kalau ada error, dialog tidak ditampilkan dan user harus memperbaiki error terlebih dahulu.

## Konsep Teknis

### MaterialAlertDialogBuilder — Material 3 Dialog

```kotlin
private fun showConfirmationDialog() {
    val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
        .setTitle(R.string.konfirmasi_title)
        .setMessage(R.string.konfirmasi_message)
        .setPositiveButton(R.string.ya) { _, _ ->
            navigateToResult()
        }
        .setNegativeButton(R.string.tidak, null)
        .create()
    dialog.show()
}
```

**`MaterialAlertDialogBuilder`** adalah class dari library Material Design 3 (bukan `AlertDialog.Builder` bawaan Android). Perbedaannya:

| Fitur | `AlertDialog.Builder` (lama) | `MaterialAlertDialogBuilder` (Material 3) |
|-------|------------------------------|--------------------------------------------|
| Tampilan | Default Android style | Mengikuti Material 3 theme (warna, corner radius, typography) |
| Button styling | Warna default | Mengikuti `colorPrimary` dan `colorOnSurfaceVariant` dari tema |
| Rounded corners | Tergantung Android version | Konsisten rounded corners di semua API level |
| Animation | Default | Material motion animation |

Karena aplikasi menggunakan tema Material 3 (`Theme.Utsmobile1`), `MaterialAlertDialogBuilder` secara otomatis mengikuti warna dan style tema tanpa konfigurasi tambahan.

---

### Lambda `{ _, _ -> }` — Mengabaikan Parameter

```kotlin
.setPositiveButton(R.string.ya) { _, _ ->
    navigateToResult()
}
```

`setPositiveButton` menerima 2 parameter di lambda-nya:
1. **`DialogInterface`**: Dialog yang memicu event
2. **`Int`**: Index tombol yang ditekan (selalu `BUTTON_POSITIVE` untuk positive button)

Kita menulis `{ _, _ -> }` karena **kita tidak butuh kedua parameter tersebut**. Underscore `_` adalah cara Kotlin mengatakan "saya tahu parameter ini ada, tapi saya tidak pakai."

Tanpa underscore, kodenya akan terlihat seperti ini:
```kotlin
.setPositiveButton(R.string.ya) { dialog, which ->
    navigateToResult()
    // dialog dan which tidak dipakai — Kotlin akan warning "unused parameter"
}
```

---

### Negative Button dengan `null` Listener

```kotlin
.setNegativeButton(R.string.tidak, null)
```

Parameter kedua `null` berarti kita tidak memberikan click listener khusus. Saat user menekan "Tidak", dialog akan **otomatis ditutup** oleh Android — ini perilaku default. Kita tidak perlu menulis manual `dialog.dismiss()`.

Kalau kita ingin melakukan something sebelum dismiss:
```kotlin
// Contoh: Kalau kita ingin log analytics saat user klik "Tidak"
.setNegativeButton(R.string.tidak) { _, _ ->
    Analytics.log("registration_cancelled")
    // dialog otomatis dismiss setelah ini
}
```

---

### `finish()` — Menutup Activity

```kotlin
private fun navigateToResult() {
    val intent = Intent(this, ResultActivity::class.java).apply {
        putExtra(Constants.IntentKeys.NAMA, binding.etNama.text?.toString())
        putExtra(Constants.IntentKeys.EMAIL, binding.etEmail.text?.toString())
        putExtra(Constants.IntentKeys.HP, binding.etHp.text?.toString())
        // ...
    }
    startActivity(intent)
    finish()  // ← menutup SeminarFormActivity dari back stack
}
```

**`finish()`** dipanggil setelah `startActivity(intent)` untuk menutup `SeminarFormActivity` dari back stack. Tanpa `finish()`, user bisa menekan tombol back dari ResultActivity dan kembali ke form — yang tidak diinginkan karena data sudah di-submit.

Back stack behavior:
```
TANPA finish():
  [HomeFragment → SeminarFragment → SeminarFormActivity → ResultActivity]
  User tekan back → kembali ke SeminarFormActivity (form masih terisi)

DENGAN finish():
  [HomeFragment → SeminarFragment → ResultActivity]
  User tekan back → kembali ke SeminarFragment (form sudah hilang)
```

---

### `Intent.apply {}` — Block Scope untuk Intent

```kotlin
val intent = Intent(this, ResultActivity::class.java).apply {
    putExtra(Constants.IntentKeys.NAMA, binding.etNama.text?.toString())
    putExtra(Constants.IntentKeys.EMAIL, binding.etEmail.text?.toString())
    // ...
}
```

**`apply`** adalah scope function Kotlin yang memungkinkan kita memanggil multiple method pada object yang sama tanpa mengulangi nama variabel. Tanpa `apply`:

```kotlin
val intent = Intent(this, ResultActivity::class.java)
intent.putExtra(Constants.IntentKeys.NAMA, binding.etNama.text?.toString())
intent.putExtra(Constants.IntentKeys.EMAIL, binding.etEmail.text?.toString())
intent.putExtra(Constants.IntentKeys.HP, binding.etHp.text?.toString())
// ... repetitif
```

Dengan `apply`, semua `putExtra()` dipanggil dalam konteks `intent` — lebih bersih dan readable.

---

### `Constants.IntentKeys` — Kenapa Pakai Constant?

```kotlin
object Constants {
    object IntentKeys {
        const val NAMA = "nama"
        const val EMAIL = "email"
        const val HP = "hp"
        const val GENDER = "gender"
        const val SEMINAR = "seminar"
        // ...
    }
}
```

Kenapa tidak langsung pakai string `"nama"`, `"email"`, dll?

**Tanpa constant:**
```kotlin
// Di SeminarFormActivity
intent.putExtra("nama", nama)
// Di ResultActivity
val nama = intent.getStringExtra("nama")  // TYPO! → null, tidak crash tapi data hilang
```

**Dengan constant:**
```kotlin
// Di SeminarFormActivity
intent.putExtra(Constants.IntentKeys.NAMA, nama)
// Di ResultActivity
val nama = intent.getStringExtra(Constants.IntentKeys.NAMA)  // type-safe, tidak bisa typo
```

Keuntungan:
1. **Type-safe**: Tidak bisa typo — compiler akan error kalau nama constant salah
2. **Refactoring-friendly**: Kalau perlu ganti key, cukup ubah di satu tempat
3. **Auto-complete**: IDE bisa auto-suggest nama constant
4. **Discoverability**: Semua key terkumpul di satu class — mudah dicari

---

### Gender Mapping — `when` Expression dengan `R.id`

```kotlin
val gender = when (genderId) {
    R.id.rbLaki -> getString(R.string.laki_laki)    // "Laki-laki"
    R.id.rbPerempuan -> getString(R.string.perempuan) // "Perempuan"
    else -> ""
}
```

`when` expression di Kotlin memetakan ID RadioButton ke string yang sesuai:
- `R.id.rbLaki` → "Laki-laki"
- `R.id.rbPerempuan` → "Perempuan"
- `else` → "" (string kosong, seharusnya tidak tercapai karena sudah divalidasi)

Ini diperlukan karena `RadioGroup.getCheckedRadioButtonId()` mengembalikan **ID resource** (bukan nilai string), jadi kita perlu map ID → string.

## Data yang Dikirim ke ResultActivity

| Key | Constant | Tipe | Contoh |
|-----|----------|------|--------|
| `"nama"` | `IntentKeys.NAMA` | String | "Ahmad Fadilah" |
| `"email"` | `IntentKeys.EMAIL` | String | "ahmad@email.com" |
| `"hp"` | `IntentKeys.HP` | String | "081234567890" |
| `"gender"` | `IntentKeys.GENDER` | String | "Laki-laki" |
| `"seminar"` | `IntentKeys.SEMINAR` | String | "Seminar AI" |

## String Resources

```xml
<string name="konfirmasi_title">Konfirmasi</string>
<string name="konfirmasi_message">Apakah data yang Anda isi sudah benar?</string>
<string name="ya">Ya</string>
<string name="tidak">Tidak</string>
```

## File Terkait

| File | Path |
|------|------|
| Dialog Implementation | `seminar/SeminarFormActivity.kt` → `showConfirmationDialog()` |
| Intent Keys | `ui/Constants.kt` → `IntentKeys` |
| String Resources | `res/values/strings.xml` |
| Target Activity | `result/ResultActivity.kt` |