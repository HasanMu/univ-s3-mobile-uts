# Halaman Form Pendaftaran Seminar

## Gambaran Umum

Halaman form pendaftaran seminar menampilkan formulir komprehensif yang terdiri dari 6 field input: Nama, Email, Nomor HP, Jenis Kelamin (RadioGroup), Pilihan Seminar (Dropdown), dan Checkbox persetujuan. Halaman ini memiliki validasi real-time per-field dan confirmation dialog sebelum submit.

## Arsitektur

```
SeminarFormActivity (Activity)
├── activity_seminar_form.xml (Layout — CoordinatorLayout + AppBar + ScrollView)
├── SeminarFormActivity.kt (Logika)
├── ValidationUtils (Validasi — shared utility)
├── Constants.SeminarList (Data Seminar — hardcoded 5 item)
└── ResultActivity (Target navigasi setelah submit)
```

## Konsep Teknis

### CoordinatorLayout + AppBarLayout + NestedScrollView

Layout menggunakan `CoordinatorLayout` sebagai root dengan `AppBarLayout` + `Toolbar` di atas dan `NestedScrollView` sebagai konten scrollable. Ketiganya bekerja bersama:

```xml
<CoordinatorLayout>
    <AppBarLayout ...>
        <Toolbar ... />
    </AppBarLayout>
    <NestedScrollView
        app:layout_behavior="@string/appbar_scrolling_view_behavior">
        <!-- konten form -->
    </NestedScrollView>
</CoordinatorLayout>
```

**Cara kerjanya:**

1. **`CoordinatorLayout`**: Layout yang mengoordinasikan perilaku child-nya. Ia bisa menghubungkan scroll di satu child dengan animasi di child lain.
2. **`AppBarLayout`**: Container vertikal yang merespons scroll event. Isinya (Toolbar) bisa di-animate saat user scroll.
3. **`NestedScrollView`**: Mensupplied scroll event ke CoordinatorLayout melalui `app:layout_behavior="@string/appbar_scrolling_view_behavior"`. Tanpa behavior ini, AppBarLayout tidak tahu bahwa konten sedang di-scroll.
4. **`app:layout_behavior`**: Memberitahu CoordinatorLayout bahwa scroll view ini harus diperlakukan sebagai scroll content — posisinya akan diatur relatif terhadap AppBarLayout (di bawahnya).

---

### `liftOnScroll="false"` — AppBar Tetap Rata

```xml
<com.google.android.material.appbar.AppBarLayout
    app:liftOnScroll="false"
    app:elevation="0dp">
```

Secara default, Material Design membuat AppBar "naik" (elevasi bertambah) saat konten di-scroll — ini disebut **lift on scroll**. Kita menonaktifkan ini karena:

- **`app:liftOnScroll="false"`**: AppBar tidak berubah saat scroll — tetap rata tanpa elevasi yang berubah-ubah.
- **`app:elevation="0dp"`**: Tidak ada shadow/bayangan di bawah AppBar.

Hasilnya: AppBar terlihat menyatu dengan konten, background-nya sama (`@color/background`), menciptakan tampilan flat yang konsisten.

---

### AutoCompleteTextView sebagai Dropdown

```xml
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/tilSeminar"
    style="@style/Widget.Utsmobile1.TextInputLayout.Form.ExposedDropdownMenu">
    <AutoCompleteTextView
        android:id="@+id/actvSeminar"
        android:inputType="none" />
</TextInputLayout>
```

**AutoCompleteTextView** adalah EditText yang menampilkan dropdown suggestion saat user mengetik. Kita gunakan untuk memilih seminar:

1. **`style="ExposedDropdownMenu"`**: Style dari Material yang membuat TextInputLayout tampil sebagai dropdown box (bukan text field biasa). Muncul ikon panah bawah di sisi kanan.
2. **`android:inputType="none"`**: User TIDAK bisa mengetik manual — hanya bisa memilih dari dropdown. Ini mencegah user mengisi seminar yang tidak ada di daftar.
3. **Data diisi dari `ArrayAdapter`**:

```kotlin
val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, seminarTitles)
binding.actvSeminar.setAdapter(adapter)
```

- **`ArrayAdapter`**: Adapter yang mengubah `List<String>` menjadi item dropdown. `android.R.layout.simple_dropdown_item_1line` adalah layout bawaan Android untuk item dropdown 1 baris.
- **`seminarTitles`**: Didapat dari `Constants.SeminarList.seminars.map { it.title }` — list 5 judul seminar.

---

### RadioGroup — Pilihan Jenis Kelamin

```xml
<RadioGroup
    android:id="@+id/rgGender"
    android:orientation="horizontal">
    <MaterialRadioButton android:id="@+id/rbLaki" android:text="Laki-laki" />
    <MaterialRadioButton android:id="@+id/rbPerempuan" android:text="Perempuan" />
</RadioGroup>
```

**`RadioGroup`** mengelola sekelompok `RadioButton` — hanya satu yang bisa dipilih pada satu waktu. Saat user memilih opsi baru, opsi sebelumnya otomatis di-deselect.

**Validasi Gender — Berbeda dari field lain:**

```kotlin
// Cek apakah ada RadioButton yang dipilih
val gender = binding.rgGender.checkedRadioButtonId
if (gender == -1) {
    binding.tvGenderLabel.setTextColor(getColor(R.color.error))  // label jadi merah
    isValid = false
} else {
    binding.tvGenderLabel.setTextColor(getColor(R.color.on_surface_variant))  // label normal
}
```

- **`checkedRadioButtonId`**: Mengembalikan ID RadioButton yang dipilih, atau `-1` kalau belum ada yang dipilih.
- Kenapa tidak pakai `TextInputLayout.error`? Karena **RadioGroup bukan TextInputLayout** — ia tidak punya property `.error`. Jadi kita validasi dengan mengubah warna label menjadi merah (`R.color.error`) untuk menandakan field ini belum diisi.

Reset warna saat user memilih:
```kotlin
binding.rgGender.setOnCheckedChangeListener { _, _ ->
    binding.tvGenderLabel.setTextColor(getColor(R.color.on_surface_variant))
}
```

`OnCheckedChangeListener` dipanggil setiap kali user memilih opsi — kita reset warna label ke normal.

---

### MaterialCheckBox dan Error Display

```kotlin
val agreementResult = ValidationUtils.validateAgreement(agreement)
when (agreementResult) {
    is ValidationResult.Error -> {
        binding.cbAgreement.error = agreementResult.message  // ← error di checkbox
        isValid = false
    }
    else -> binding.cbAgreement.error = null
}
```

`MaterialCheckBox` punya property `.error` yang menampilkan pesan error di bawah checkbox — mirip dengan `TextInputLayout.error`. Ini menampilkan teks merah seperti "Anda harus menyetujui syarat dan ketentuan".

Reset saat checkbox di-centang:
```kotlin
binding.cbAgreement.setOnCheckedChangeListener { _, isChecked ->
    if (isChecked) {
        binding.cbAgreement.error = null
    }
}
```

---

### Pre-Selection Seminar dari Intent

```kotlin
selectedSeminarId = intent.getIntExtra("seminar_id", -1)

if (selectedSeminarId > 0) {
    val seminar = Constants.SeminarList.seminars.findById(selectedSeminarId)
    seminar?.let {
        binding.actvSeminar.setText(it.title, false)
    }
}
```

Ketika user men-tap seminar card di `SeminarFragment`, ID seminar dikirim via Intent:

```kotlin
// Di SeminarFragment
val intent = Intent(requireContext(), SeminarFormActivity::class.java)
intent.putExtra("seminar_id", seminar.id)
startActivity(intent)
```

Di `SeminarFormActivity`, kita baca ID tersebut dan pre-fill dropdown:
- **`intent.getIntExtra("seminar_id", -1)`**: Mengambil int dari Intent. Default `-1` (tidak ada ID) jika key tidak ditemukan.
- **`findById(selectedSeminarId)`**: Extension function yang mencari `SeminarItem` berdasarkan ID di list.
- **`setText(it.title, false)`**: Mengisi text AutoCompleteTextView. Parameter `false` berarti jangan filter dropdown — langsung set teksnya.

---

### `setText(it.title, false)` — Parameter Filter

```kotlin
binding.actvSeminar.setText(it.title, false)
```

AutoCompleteTextView punya method `setText()` dengan 2 parameter:
1. **`text`**: Teks yang akan ditampilkan
2. **`filter`**: Kalau `true`, dropdown akan di-filter berdasarkan teks. Kalau `false`, dropdown tidak di-filter.

Kita set `false` karena kita ingin menampilkan judul seminar yang sudah dipilih tanpa memicu filter ulang.

## Alur Navigasi

```
SeminarFragment ──[tap seminar card]──▶ SeminarFormActivity (dengan seminar_id extra)
SeminarFormActivity ──[btnBack]──▶ finish() (kembali ke halaman sebelumnya)
SeminarFormActivity ──[btnSubmit + valid]──▶ Dialog Konfirmasi
Dialog Konfirmasi ──[Ya]──▶ ResultActivity (dengan Intent extras)
Dialog Konfirmasi ──[Tidak]──▶ dismiss dialog
```

## Struktur Layout

```
CoordinatorLayout (root, bg=background)
├── AppBarLayout (elevation=0dp, liftOnScroll=false, bg=background)
│   └── Toolbar
│       ├── ImageButton (btnBack, ic_arrow_back, tint=primary)
│       └── TextView ("Form Pendaftaran", color=primary, bold)
└── NestedScrollView (scrollView, appbar_scrolling_view_behavior)
    └── ConstraintLayout (padded)
        ├── Title + Subtitle
        ├── [NAMA] Label + TextInputLayout/TextInputEditText
        ├── [EMAIL] Label + TextInputLayout/TextInputEditText
        ├── [NOMOR HP] Label + TextInputLayout/TextInputEditText
        ├── [JENIS KELAMIN] Label + RadioGroup (horizontal, 2 RadioButton block-style)
        ├── [PILIH SEMINAR] Label + TextInputLayout/AutoCompleteTextView (dropdown)
        ├── [AGREEMENT] MaterialCheckBox
        ├── MaterialButton (btnSubmit, "Konfirmasi Pendaftaran")
        └── TextView (privacy notice, center-aligned)
```

## Detail Field Validasi

| Field | Input Type | Validasi | Real-time? |
|-------|-----------|----------|------------|
| Nama | `textPersonName` | `validateNama` — tidak kosong | ✅ Per-field TextWatcher |
| Email | `textEmailAddress` | `validateEmail` — tidak kosong, format valid | ✅ Per-field TextWatcher |
| Nomor HP | `phone`, maxLength=13 | `validateHp` — tidak kosong, hanya digit, 10-13 digit, dimulai 08 | ✅ Per-field TextWatcher |
| Jenis Kelamin | RadioGroup | `validateGender` — harus dipilih | ✅ OnCheckedChangeListener |
| Pilih Seminar | AutoCompleteTextView | `validateSeminar` — tidak kosong | ✅ Clear on text change |
| Persetujuan | CheckBox | `validateAgreement` — harus dicentang | ✅ OnCheckedChangeListener |

## Binding IDs Referensi

| View ID | Tipe | Fungsi |
|---------|------|--------|
| `btnBack` | ImageButton | Tombol kembali |
| `tilNama` / `etNama` | TextInputLayout / EditText | Input nama lengkap |
| `tilEmail` / `etEmail` | TextInputLayout / EditText | Input email |
| `tilHp` / `etHp` | TextInputLayout / EditText | Input nomor HP |
| `tvGenderLabel` | TextView | Label jenis kelamin (berubah warna saat error) |
| `rgGender` | RadioGroup | Pilihan jenis kelamin |
| `rbLaki` / `rbPerempuan` | MaterialRadioButton | Opsi L/P |
| `tilSeminar` / `actvSeminar` | TextInputLayout / AutoCompleteTextView | Dropdown seminar |
| `cbAgreement` | MaterialCheckBox | Checkbox persetujuan |
| `btnSubmit` | MaterialButton | Trigger validasi & submit |

## Resource Terkait

| Resource | Path |
|----------|------|
| Layout XML | `res/layout/activity_seminar_form.xml` |
| Activity Kotlin | `seminar/SeminarFormActivity.kt` |
| Validation Logic | `util/ValidationUtils.kt` |
| Seminar Data | `ui/Constants.kt` |
| RadioButton Style | `res/values/themes.xml` (Widget.Utsmobile1.RadioButton.Block) |
| Result Activity | `result/ResultActivity.kt` |