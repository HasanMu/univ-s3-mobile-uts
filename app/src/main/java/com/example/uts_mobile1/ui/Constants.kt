package com.example.uts_mobile1.ui

/**
 * App-wide constants following Lentera Modernist design system
 */
object Constants {

    // ==================== Intent Keys ====================
    object IntentKeys {
        const val NAMA = "nama"
        const val EMAIL = "email"
        const val HP = "hp"
        const val GENDER = "gender"
        const val SEMINAR = "seminar"
        const val SEMINAR_ID = "seminar_id"
        const val TANGGAL = "tanggal"
        const val WAKTU = "waktu"
        const val LOKASI = "lokasi"
    }

    // ==================== Seminar List (Hardcoded - 5 items) ====================
    object SeminarList {
        val seminars = listOf(
            SeminarItem(
                id = 1,
                title = "Seminar Android",
                description = "Belajar pengembangan aplikasi Android dengan Kotlin dan Jetpack Compose",
                date = "12 Okt 2023",
                location = "Jakarta, ID",
                price = "Rp 250.000",
                quota = 45
            ),
            SeminarItem(
                id = 2,
                title = "Seminar UI/UX",
                description = "Fundamental desain produk digital dengan pendekatan human-centered",
                date = "15 Okt 2023",
                location = "Online",
                price = "Gratis",
                quota = 100
            ),
            SeminarItem(
                id = 3,
                title = "Seminar AI",
                description = "Membahas dampak transformatif kecerdasan buatan terhadap industri kreatif",
                date = "15 Okt 2024",
                location = "Jakarta & Online",
                price = "Rp 500.000",
                quota = 50
            ),
            SeminarItem(
                id = 4,
                title = "Seminar Cyber Security",
                description = "Seminar eksklusif mengenai ancaman siber terbaru dan cara mitigasinya",
                date = "20 Okt 2024",
                location = "Jakarta",
                price = "Rp 300.000",
                quota = 30
            ),
            SeminarItem(
                id = 5,
                title = "Seminar Data Science",
                description = "Teknik dasar pengolahan data untuk pengambilan keputusan bisnis yang tepat",
                date = "25 Okt 2024",
                location = "Online",
                price = "Gratis",
                quota = 75
            )
        )
    }

    // ==================== Shared Preferences Keys ====================
    object PrefsKeys {
        const val IS_LOGGED_IN = "is_logged_in"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
    }
}

/**
 * Data class for Seminar item
 */
data class SeminarItem(
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val price: String,
    val quota: Int
)

/**
 * Extension to get seminar by ID
 */
fun List<SeminarItem>.findById(id: Int): SeminarItem? = find { it.id == id }