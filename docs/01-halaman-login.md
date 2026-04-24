# Halaman Login

## Gambaran Umum

Halaman login merupakan entry point utama aplikasi sebelum pengguna mengakses fitur pendaftaran seminar. Halaman ini mengimplementasikan autentikasi sederhana dengan validasi real-time dan navigasi ke halaman registrasi.

## Arsitektur

```
LoginActivity (Activity)
├── activity_login.xml (Layout)
├── ValidationUtils (Validasi)
└── LoginActivity.kt (Logika)
```

## Konsep Teknis

### ViewBinding — `binding`

ViewBinding adalah fitur Android yang secara otomatis **menghasilkan class** untuk mengakses view di layout XML tanpa perlu memanggil `findViewById()`.

```kotlin
private lateinit var binding: ActivityLoginBinding
```

- **`lateinit var`**: Keyword yang memberitahu compiler bahwa variable akan diinisialisasi **nanti** (bukan di constructor). Kita tidak bisa menggunakan `val` biasa karena Activity lifecycle mengharuskan inisialisasi di `onCreate()`, bukan saat class dibuat. Kalau kita akses `binding` sebelum di-inflate, app akan crash dengan `UninitializedPropertyAccessException`.
- **`ActivityLoginBinding`**: Class yang di-generate otomatis oleh Android berdasarkan nama file layout `activity_login.xml`. Android mengkonversi nama file _snake_case_ → class _PascalCase_ + suffix "Binding". Jadi `activity_login.xml` → `ActivityLoginBinding`. Setiap view yang punya `android:id` di XML akan menjadi property di class ini.
- **`binding = ActivityLoginBinding.inflate(layoutInflater)`**: Method `inflate()` membaca file XML layout, membuat semua view di dalamnya menjadi object di memory, dan mengembalikan object binding yang berisi referensi ke semua view. Ini menggantikan cara lama `setContentView(R.layout.activity_login)`.
- **`setContentView(binding.root)`**: `binding.root` adalah view paling atas (root) dari layout XML — dalam kasus ini `ConstraintLayout`. Kita meneruskannya ke `setContentView()` agar Android menampilkan layout tersebut di layar.

Setelah inisialisasi, kita bisa mengakses view langsung via `binding`:

```kotlin
binding.etEmail          // EditText dengan id "etEmail" — tanpa findViewById!
binding.btnLogin         // MaterialButton dengan id "btnLogin"
binding.tilEmail.error   // mengatur error di TextInputLayout
```

**Perbandingan tanpa ViewBinding (cara lama — HINDARI):**

```kotlin
// Tanpa ViewBinding — verbose dan rentan error
val etEmail = findViewById<EditText>(R.id.etEmail)           // bisa null, bisa salah cast
val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)    // tidak type-safe
```

Keuntungan ViewBinding dibanding `findViewById`:
1. **Type-safe** — Tidak bisa salah cast. `binding.etEmail` sudah bertipe `TextInputEditText`, bukan `View`.
2. **Null-safe** — Binding generated class menjamin semua view ada. Tidak perlu null check.
3. **Compile-time check** — Kalau ID salah/hilang, error muncul saat compile, bukan runtime crash.

---

### `enableEdgeToEdge()`

```kotlin
enableEdgeToEdge()
```

Method dari AndroidX Activity library yang membuat aplikasi menggambar konten **di belakang** system bars (status bar di atas, navigation bar di bawah). Tanpa method ini, aplikasi punya area hitam atau berwarna solid di atas dan bawah. Dengan method ini, gradient background aplikasi meluas ke seluruh layar, menciptakan tampilan modern yang "full screen".

Visualisasi efek:

```
TANPA enableEdgeToEdge():        DENGAN enableEdgeToEdge():
┌─────────────────────┐          ┌─────────────────────┐
│  Status Bar (putih)  │          │ Status Bar (gradient)│ ← konten di belakang
│─────────────────────│          │    [Logo]           │
│    [Logo]           │          │  [Form Card]        │
│  [Form Card]        │          │    ...              │
│    ...              │          │─────────────────────│
│─────────────────────│          │ Nav Bar (gradient)  │ ← konten di belakang
│  Nav Bar (putih)    │          └─────────────────────┘
└─────────────────────┘
```

---

### `setupWindowInsets()`

```kotlin
private fun setupWindowInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(
            resources.getDimensionPixelSize(R.dimen.screen_padding_horizontal),  // kiri
            systemBars.top,                       // atas (tinggi status bar)
            resources.getDimensionPixelSize(R.dimen.screen_padding_horizontal),  // kanan
            systemBars.bottom                     // bawah (tinggi navigation bar)
        )
        insets
    }
}
```

Karena kita menggunakan `enableEdgeToEdge()`, konten aplikasi menggambar di belakang system bars — artinya konten bisa **tertutup** oleh status bar dan navigation bar. `setupWindowInsets()` mengatasi masalah ini:

1. **`WindowInsetsCompat.Type.systemBars()`**: Mengambil ukuran (padding/inset) system bars — status bar di atas dan navigation bar di bawah. Setiap device punya ukuran berbeda (status bar bisa 24dp, 48dp, dll tergantung device).
2. **`setPadding()`**: Memberi padding pada root view agar konten tidak tertutup system bars.
   - Kiri & kanan: `@dimen/screen_padding_horizontal` — padding horizontal konsisten dari design system
   - Atas: `systemBars.top` — padding dinamis sesuai tinggi status bar device
   - Bawah: `systemBars.bottom` — padding dinamis sesuai tinggi navigation bar device
3. **`return insets`**: Inset dikembalikan agar listener lain di chain bisa memproses insets juga.

**Intinya: `enableEdgeToEdge()` menggambar di belakang system bars, `setupWindowInsets()` memastikan konten tidak tertutup system bars. Keduanya bekerja bersama.**

---

### TextWatcher — Clear-on-Type

```kotlin
private val textWatcher = object : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable?) {
        clearError()
    }
}
```

`TextWatcher` adalah interface Android yang mendengarkan perubahan teks di `EditText`. Memiliki 3 callback:

| Callback | Kapan dipanggil | Kegunaan umum |
|----------|----------------|---------------|
| `beforeTextChanged` | Sebelum teks berubah | Menyimpan state sebelum perubahan |
| `onTextChanged` | Saat teks sedang berubah | Live character counter |
| `afterTextChanged` | Setelah teks sudah berubah | Validasi, format otomatis |

Di `LoginActivity`, kita menggunakan pola **clear-on-type**: saat user mulai mengetik setelah melihat error, semua error langsung hilang. Ini UX yang baik karena error sudah "dibaca" dan user sedang berusaha memperbaiki inputnya.

Polanya berbeda dengan Register/SeminarForm yang menggunakan **per-field real-time validation** — di halaman tersebut, setiap field divalidasi secara independen saat mengetik. Login menggunakan pola yang lebih sederhana karena hanya punya 2 field.

---

### TextInputLayout dan Error Display

```xml
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/tilEmail"
    style="@style/Widget.Utsmobile1.TextInputLayout.Form"
    app:hintEnabled="false"
    app:boxBackgroundMode="filled"
    app:startIconDrawable="@drawable/ic_email">
    
    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/etEmail"
        .../>
</com.google.android.material.textfield.TextInputLayout>
```

`TextInputLayout` adalah container wrapper dari Material Design yang membungkus `TextInputEditText`. Ia menyediakan:
- **Error display**: Pesan error merah yang muncul di bawah field — ini yang kita pakai untuk validasi
- **Start/end icon**: Ikon dekoratif di kiri/kanan field (`startIconDrawable`, `endIconMode`)
- **Box mode**: Tampilan kotak input — kita pakai `filled` (background terisi)
- **Floating hint**: Label yang naik ke atas saat user mulai mengetik — kita NONAKTIFKAN ini (`app:hintEnabled="false"`) karena kita menggunakan `TextView` terpisah di atas setiap input sebagai label

Cara menampilkan dan menghapus error:
```kotlin
binding.tilEmail.error = "Email tidak boleh kosong"  // muncul pesan merah di bawah field
binding.tilEmail.error = null                          // hapus error, kembali normal
```

Kenapa pakai `TextInputLayout` bukan langsung `EditText`? Karena `TextInputLayout` memberikan animasi error yang smooth, animasi label, dan ikon dalam satu komponen yang sudah mengikuti Material Design guidelines.

---

### Intent Flags — `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`

```kotlin
intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
```

Kombinasi dua flag ini memastikan **back stack di-clear total**:

- **`FLAG_ACTIVITY_NEW_TASK`**: Membuat task baru untuk Activity yang dituju
- **`FLAG_ACTIVITY_CLEAR_TASK`**: Menghapus semua Activity yang sudah ada di task sebelumnya

Visualisasi back stack:

```
TANPA FLAGS:
  [LoginActivity] → tekan login → [LoginActivity, MainActivity]
  User tekan back → kembali ke LoginActivity (TIDAK DIINGINKAN)

DENGAN FLAGS:
  [LoginActivity] → tekan login → [MainActivity] ← LoginActivity dihapus dari stack
  User tekan back → keluar app (DIINGINKAN)
```

Ini penting karena setelah login berhasil, user seharusnya tidak bisa kembali ke halaman login tanpa logout terlebih dahulu. Tanpa flag ini, user bisa tekan back dan masuk ke MainActivity lagi tanpa login.

---

### NestedScrollView dengan fillViewport

```xml
<androidx.core.widget.NestedScrollView
    android:id="@+id/scrollView"
    android:fillViewport="true" ...>
```

- **`NestedScrollView`**: Versi compat dari ScrollView yang mendukung **nested scrolling** — scroll di dalam scroll. Ini penting karena layout login berada di dalam activity yang juga punya system bars yang bisa scroll.
- **`fillViewport="true"`**: Memastikan konten mengisi setidaknya seluruh viewport. Jika konten lebih pendek dari layar, ia akan stretch. Jika lebih panjang, ia bisa scroll. Tanpa ini, layout bisa terpotong dan tidak scroll.

---

### Bento Box Design Pattern

Layout login menggunakan pola **Bento Box** — konten utama (form) dibungkus dalam `MaterialCardView` dengan background putih (`@color/surface`) yang "mengapung" di atas gradient background. Ini menciptakan hirarki visual yang jelas:

```
┌─────────────────────────┐
│   Gradient Background   │  ← Layer 1: background penuh layar
│   ┌─────────────────┐   │
│   │  Card (surface) │   │  ← Layer 2: form card putih dengan elevasi
│   │  [Email input]  │   │
│   │  [Pass input]   │   │  ← Layer 3: input fields dan tombol di dalam card
│   │  [Login btn]    │   │
│   └─────────────────┘   │
│   Belum punya akun? Daftar │
└─────────────────────────┘
```

## Alur Navigasi

```
LoginActivity ──[btnLogin]──▶ MainActivity (FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK)
LoginActivity ──[tvRegisterLink / llRegister]──▶ RegisterActivity
```

## Struktur Layout

```
ConstraintLayout (root, bg_gradient)
└── NestedScrollView (scrollView, fillViewport=true)
    └── ConstraintLayout (padded content)
        ├── FrameLayout (logoContainer)
        │   └── ImageView (ivLogo, ic_school, tint=primary)
        ├── TextView (tvTitle, app_name)
        ├── TextView (tvSubtitle, login_subtitle)
        ├── MaterialCardView (cardForm, style=Elevated, bg=surface)
        │   └── ConstraintLayout (padded)
        │       ├── TextView (tvEmailLabel) → label email
        │       ├── TextInputLayout (tilEmail, style=Form, startIcon=ic_email)
        │       │   └── TextInputEditText (etEmail, inputType=textEmailAddress)
        │       ├── TextView (tvPasswordLabel) → label password
        │       ├── TextInputLayout (tilPassword, style=Form, startIcon=ic_lock, endIcon=password_toggle)
        │       │   └── TextInputEditText (etPassword, inputType=textPassword)
        │       └── MaterialButton (btnLogin, style=Primary)
        └── LinearLayout (llRegister, horizontal)
            ├── TextView ("Belum punya akun? ")
            └── TextView (tvRegisterLink, "Daftar", color=primary, bold)
```

## Logika Kotlin Detail

### Lifecycle Inisialisasi

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)           // 1. Wajib — menyimpan state Activity
    enableEdgeToEdge()                            // 2. Sebelum setContentView — siapkan window
    binding = ActivityLoginBinding.inflate(layoutInflater)  // 3. Inflate layout → buat view hierarchy
    setContentView(binding.root)                  // 4. Tampilkan root view di layar
    setupWindowInsets()                           // 5. Atur padding agar tidak tertutup system bars
    setupViews()                                  // 6. Tambahkan TextWatcher
    setupValidation()                             // 7. Placeholder (kosong)
    setupListeners()                              // 8. Tambahkan click listener
}
```

Urutan ini **sangat penting** — `enableEdgeToEdge()` harus sebelum `setContentView()`, dan `setupWindowInsets()` harus setelah `setContentView()` karena membutuhkan `binding.root` yang sudah ada di window.

### Flow Validasi Login

```
User klik Login
  └── validateFields()
      ├── Email: validateEmail(email)
      │   ├── blank → "Email tidak boleh kosong"
      │   ├── no @ → "Format email tidak valid"
      │   ├── bad format → "Format email tidak valid"
      │   └── valid → tilEmail.error = null
      ├── Password: validatePasswordRequired(password)
      │   ├── blank → "Password tidak boleh kosong"
      │   └── valid → tilPassword.error = null
      ├── Semua valid → navigateToMain()
      │   └── Intent(MainActivity) + FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
      └── Ada error → tampilkan error, jangan navigasi
```

Kenapa login hanya pakai `validatePasswordRequired` (tidak cek panjang minimal)?

Karena di halaman login, kita **tidak tahu** apakah password user itu panjangnya 8 karakter atau tidak — yang penting adalah user memasukkan sesuatu. Validasi panjang password (minimal 8 karakter) hanya ada di halaman Register, karena di situlah user **membuat** password baru.

## Binding IDs Referensi

| View ID | Tipe | Fungsi |
|---------|------|--------|
| `tilEmail` | TextInputLayout | Container error display untuk email |
| `tilPassword` | TextInputLayout | Container error display untuk password |
| `etEmail` | TextInputEditText | Input email |
| `etPassword` | TextInputEditText | Input password |
| `btnLogin` | MaterialButton | Trigger validasi & navigasi |
| `tvRegisterLink` | TextView | Link ke halaman registrasi |
| `llRegister` | LinearLayout | Container link registrasi (klik area) |
| `cardForm` | MaterialCardView | Card yang membungkus form |

## Resource Terkait

| Resource | Path |
|----------|------|
| Layout XML | `res/layout/activity_login.xml` |
| Activity Kotlin | `auth/LoginActivity.kt` |
| Validation Logic | `util/ValidationUtils.kt` |
| Background Gradient | `res/drawable/bg_gradient.xml` |
| Logo Circle | `res/drawable/bg_logo_circle.xml` |
| Icons | `res/drawable/ic_school.xml`, `ic_email.xml`, `ic_lock.xml` |
| Strings | `res/values/strings.xml` |
| Text Appearances | `res/values/themes.xml` (TextAppearance.Utsmobile1.*) |
| Dimens | `res/values/dimens.xml` (spacing_*, icon_size_*, screen_padding_*) |