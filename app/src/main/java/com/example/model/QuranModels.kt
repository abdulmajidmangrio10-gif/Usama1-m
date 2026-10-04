package com.example.model

data class SurahInfo(
    val number: Int,
    val arabicName: String,
    val englishName: String,
    val transliteration: String,
    val ayahCount: Int,
    val revelationType: String // "Makki" or "Madani"
)

data class AyahItem(
    val numberInSurah: Int,
    val arabicText: String, // Complete Tashkeel (زبر، زیر، پیش، جزم، تشدید)
    val urduTranslation: String = "",
    val englishTranslation: String = ""
)
