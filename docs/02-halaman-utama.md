# Halaman Utama (Home)

## Gambaran Umum

Halaman Home merupakan landing page setelah pengguna login. Halaman ini menampilkan welcome card, daftar seminar terdekat dalam format Bento Box grid, dan newsletter subscription card. Semua konten bersifat **text-only** — tidak ada gambar atau ikon dekoratif pada seminar card.

## Arsitektur

```
HomeFragment (Fragment)
├── fragment_home.xml (Layout)
├── HomeFragment.kt (Logika)
└── MainActivity (Host Activity yang punya BottomNavigationView)
```

## Konsep Teknis

### Fragment vs Activity Lifecycle

HomeFragment adalah **Fragment**, bukan Activity. Fragment hidup **di dalam** Activity — ia tidak bisa berdiri sendiri. Perbedaan lifecycle ini penting:

| Lifecycle | Activity | Fragment |
|-----------|----------|----------|
| Dibuat | `onCreate()` | `onCreateView()` → mengembalikan View |
| View siap | `onCreate()` | `onViewCreated()` → dipanggil SETELAH onCreateView |
| View dihancurkan | — | `onDestroyView()` → WAJIB null-kan binding |
| Dihancurkan | `onDestroy()` | `onDestroy()` |

Kenapa Fragment punya `onCreateView()` dan `onDestroyView()`? Karena **view Fragment bisa dihancurkan dan dibuat ulang** tanpa Fragment itu sendiri dihancurkan. Misalnya saat user pindah tab dan kembali — Fragment mungkin masih ada di memory tapi view-nya sudah dihancurkan.

---

### Fragment ViewBinding Pattern — `_binding` Nullable + `binding` Non-Null

```kotlin
private var _binding: FragmentHomeBinding? = null
private val binding get() = _binding!!
```

Pola ini berbeda dari Activity. Kenapa?

1. **`_binding` nullable (`?`)**: View Fragment bisa dihancurkan (saat user pindah tab), jadi binding bisa menjadi `null`. Kalau kita pakai `lateinit var`, app akan crash saat mengakses binding setelah view dihancurkan.
2. **`binding` via getter (`get() = _binding!!`)**: Memberikan akses yang nyaman (tanpa `?.` setiap saat) antara `onCreateView` dan `onDestroyView`. Di luar rentang itu, `_binding` null dan `binding` akan throw `KotlinNullPointerException` — ini intentional crash yang menandakan kita akses view di waktu yang salah.
3. **`_binding = null` di `onDestroyView()`**: Ini **KRITIS** untuk mencegah **memory leak**. Kalau kita tidak null-kan binding, Fragment akan terus memegang referensi ke view yang sudah dihancurkan. View tersebut tidak bisa di-garbage collect, dan seluruh view hierarchy (ratusan view object) akan tetap di memory sampai Fragment dihancurkan.

```kotlin
override fun onCreateView(...): View {
    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    return binding.root
}

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null    // ← WAJIB! Mencegah memory leak
}
```

**Perbandingan dengan Activity:**
```kotlin
// Activity — lateinit var (karena view hidup sepanjang Activity lifetime)
private lateinit var binding: ActivityLoginBinding

// Fragment — nullable + getter (karena view BISA dihancurkan saat Fragment masih hidup)
private var _binding: FragmentHomeBinding? = null
private val binding get() = _binding!!
```

---

### `inflate(inflater, container, false)` — 3 Parameter

```kotlin
_binding = FragmentHomeBinding.inflate(inflater, container, false)
```

| Parameter | Penjelasan |
|-----------|------------|
| `inflater` | `LayoutInflater` dari system — membaca XML dan membuat object View. Diberikan oleh system sebagai parameter `onCreateView`. |
| `container` | `ViewGroup` parent di mana Fragment akan ditempelkan — ini adalah `fragmentContainer` di `activity_main.xml`. Digunakan oleh inflater untuk menghitung `layout_width` dan `layout_height` yang bernilai `match_parent`. |
| `false` | **AttachToRoot** — `false` berarti jangan langsung tempelkan ke parent. Kita mengembalikan `binding.root` manual dan sistem Fragment yang menempelkannya. Kalau `true`, view akan ditempelkan dua kali dan crash. |

---

### Safe Cast — `(activity as? MainActivity)?.navigateToSeminar()`

```kotlin
binding.btnDaftarSeminar.setOnClickListener {
    (activity as? MainActivity)?.navigateToSeminar()
}
```

Baris ini melakukan 3 hal sekaligus:

1. **`activity`**: Property bawaan Fragment yang mengembalikan Activity yang menjadi host Fragment ini.
2. **`as? MainActivity`**: **Safe cast** — mencoba meng-cast `activity` ke tipe `MainActivity`. Kalau gagal (misalnya Fragment dipakai di Activity lain), hasilnya `null` bukan crash. Ini berbeda dengan `as MainActivity` (unsafe cast) yang akan throw `ClassCastException` kalau gagal.
3. **`?.` (safe call)**: Kalau hasil cast `null`, pemanggilan method `navigateToSeminar()` di-skip tanpa error. Kalau berhasil, method dipanggil.

Kenapa perlu safe cast? Karena Fragment seharusnya **tidak tahu** Activity mana yang menjadi host-nya. Fragment dirancang untuk reusable — bisa dipakai di Activity manapun. Safe cast memastikan kalau Fragment dipakai di Activity yang bukan `MainActivity`, app tidak crash.

---

### ConstraintLayout Guideline — Pembagi 2 Kolom

```xml
<androidx.constraintlayout.widget.Guideline
    android:id="@+id/guideline_vertical"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:layout_constraintGuide_percent="0.5"
    android:orientation="vertical" />
```

`Guideline` adalah garis bantu invisible di `ConstraintLayout` yang berfungsi sebagai titik acuan untuk constraint. Ia tidak terlihat di layar, tapi view lain bisa me-reference-nya.

- **`orientation="vertical"`**: Garis vertikal (kiri-kanan)
- **`layout_constraintGuide_percent="0.5"`**: Posisi di 50% lebar parent — ini yang membagi layar jadi 2 kolom

Cara kerjanya:
```
┌───────────────┬───────────────┐
│               │               │
│  Card 1       │  Card 2      │
│  (start→guideline)  (guideline→end)  │
│               │               │
└───────────────┴───────────────┘
      50%    ← guidelines →    50%
```

Card kiri di-constrain: `start→parent`, `end→guideline`
Card kanan di-constrain: `start→guideline`, `end→parent`

Ini menghasilkan layout 2 kolom yang responsif — lebar masing-masing kolom selalu 50% berapapun ukuran layar.

---

### Toast — Feedback Newsletter

```kotlin
Toast.makeText(context, "Berhasil berlangganan!", Toast.LENGTH_SHORT).show()
```

`Toast` adalah pesan singkat yang muncul di bawah layar selama beberapa detik lalu menghilang sendiri. Ini digunakan untuk konfirmasiSukses newsletter karena feedback-nya ringan dan tidak mengharuskan user melakukan tindakan apapun.

| Parameter | Penjelasan |
|-----------|------------|
| `context` | Activity/Fragment context — `context` di Fragment mengembalikan Activity context |
| `"Berhasil berlangganan!"` | Pesan yang ditampilkan |
| `Toast.LENGTH_SHORT` | Durasi tampil ~2 detik. `LENGTH_LONG` untuk ~3.5 detik |

Toast cocok untuk konfirmasi sederhana. Untuk pesan penting yang membutuhkan aksi user, sebaiknya gunakan `SnackBar` atau `AlertDialog`.

---

### Validasi Newsletter Email

```kotlin
binding.btnGabung.setOnClickListener {
    val email = binding.etNewsletter.text?.toString()
    val result = ValidationUtils.validateEmail(email ?: "")
    when (result) {
        is ValidationResult.Error -> binding.tilNewsletter.error = result.message
        else -> {
            binding.tilNewsletter.error = null
            Toast.makeText(context, "Berhasil berlangganan!", Toast.LENGTH_SHORT).show()
        }
    }
}
```

Validasi newsletter menggunakan `ValidationUtils.validateEmail()` yang sama dengan form login — memastikan email tidak kosong dan format valid. Perbedaannya: di sini kita pakai pola **per-field immediate validation** (langsung divalidasi saat klik, tanpa clear-on-type).

## Alur Navigasi

```
HomeFragment ──[btnDaftarSeminar]──▶ Tab Seminar (via MainActivity.navigateToSeminar())
HomeFragment ──[tvLihatSemua]──▶ Tab Seminar (via MainActivity.navigateToSeminar())
HomeFragment ──[cardSeminar1/2]──▶ (placeholder)
HomeFragment ──[btnGabung]──▶ Validasi email → Toast sukses / Error
```

## Struktur Layout

```
NestedScrollView (scrollView)
└── ConstraintLayout (padded, screen_padding_horizontal)
    ├── MaterialCardView (cardHero) — Bento Hero Card
    │   └── ConstraintLayout (padded, card_padding)
    │       ├── TextView (tvWelcomeBadge, "Welcome", bg_confirmed_badge)
    │       ├── TextView (tvWelcome, app_name, color=primary)
    │       ├── TextView (tvSubtitle, welcome_message)
    │       └── MaterialButton (btnDaftarSeminar, icon=ic_arrow_forward, iconGravity=end)
    ├── Guideline (50% vertical) — pembagi 2 kolom
    ├── LinearLayout (llSectionTitle) — "Seminar Terdekat" + "Lihat Semua"
    ├── MaterialCardView (cardSeminar1, kiri) — Bento Grid Left
    ├── MaterialCardView (cardSeminar2, kanan) — Bento Grid Right
    └── MaterialCardView (cardNewsletter) — Newsletter Section
        └── ConstraintLayout (padded)
            ├── TextView (tvNewsletterTitle)
            ├── TextView (tvNewsletterSubtitle)
            ├── TextInputLayout (tilNewsletter) + TextInputEditText (etNewsletter)
            └── MaterialButton (btnGabung, "Gabung", bgTint=black)
```

## Data Seminar Hardcoded

Card seminar 1 & 2 di Home menggunakan data statis (hardcoded di layout XML):
- Card 1: "Sustainability" / "12 Okt 2023" / "Workshop"
- Card 2: "Business Intelligence" / "15 Okt 2023" / "Seminar"

Data seminar lengkap untuk list ada di `Constants.SeminarList` (5 item, di file `ui/Constants.kt`).

## Binding IDs Referensi

| View ID | Tipe | Fungsi |
|---------|------|--------|
| `cardHero` | MaterialCardView | Welcome card utama |
| `btnDaftarSeminar` | MaterialButton | CTA navigasi ke tab Seminar |
| `tvLihatSemua` | TextView | Link navigasi ke tab Seminar |
| `cardSeminar1` / `cardSeminar2` | MaterialCardView | Card seminar (placeholder) |
| `cardNewsletter` | MaterialCardView | Newsletter subscription card |
| `etNewsletter` | TextInputEditText | Input email newsletter |
| `tilNewsletter` | TextInputLayout | Error container email newsletter |
| `btnGabung` | MaterialButton | Trigger validasi & submit newsletter |

## Resource Terkait

| Resource | Path |
|----------|------|
| Layout XML | `res/layout/fragment_home.xml` |
| Fragment Kotlin | `home/HomeFragment.kt` |
| Activity Host | `MainActivity.kt` |
| Constants | `ui/Constants.kt` (SeminarList) |
| Strings | `res/values/strings.xml` |
| Badge Background | `res/drawable/bg_confirmed_badge.xml` |