package com.example.ui.screens.editor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuranRepository
import com.example.model.AyahItem
import com.example.model.CaptionStyle
import com.example.model.SurahInfo
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun QuranToolPanel(
    onAddAyahsToTimeline: (List<AyahItem>, Boolean, CaptionStyle) -> Unit,
    onClose: () -> Unit
) {
    var selectedSurah by remember { mutableStateOf<SurahInfo?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("quran_tool_panel")
    ) {
        if (selectedSurah == null) {
            // Screen 1: Surahs Index
            SurahIndexScreen(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                filterType = filterType,
                onFilterChange = { filterType = it },
                onSurahSelected = { selectedSurah = it },
                onClose = onClose
            )
        } else {
            // Screen 2: Ayah Tick Mark Selection Screen
            AyahSelectorScreen(
                surah = selectedSurah!!,
                onBack = { selectedSurah = null },
                onAddAyahs = { ayahs, withUrdu, style ->
                    onAddAyahsToTimeline(ayahs, withUrdu, style)
                    onClose()
                }
            )
        }
    }
}

@Composable
private fun SurahIndexScreen(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterType: String,
    onFilterChange: (String) -> Unit,
    onSurahSelected: (SurahInfo) -> Unit,
    onClose: () -> Unit
) {
    PanelHeader(title = "قرآن پاک (The Holy Quran)", onClose = onClose)

    Spacer(modifier = Modifier.height(10.dp))

    // Search Box
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Search Surah (e.g. Yaseen, Rahman, 55, مریم)", fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = InShotYellow,
            unfocusedBorderColor = DarkBorder
        ),
        modifier = Modifier.fillMaxWidth().testTag("quran_search_field")
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Filter Chips
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        listOf("All", "Popular", "Makki", "Madani").forEach { filter ->
            val isSelected = filterType == filter
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) InShotYellow.copy(alpha = 0.25f) else DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) InShotYellow else DarkBorder),
                modifier = Modifier.clickable { onFilterChange(filter) }
            ) {
                Text(
                    text = filter,
                    color = if (isSelected) InShotYellow else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Surahs List
    val popularSurahNumbers = setOf(1, 18, 36, 55, 56, 67, 112, 113, 114, 108, 97, 94)
    val filteredSurahs = remember(searchQuery, filterType) {
        QuranRepository.allSurahs.filter { surah ->
            val matchesSearch = searchQuery.isBlank() ||
                    surah.englishName.contains(searchQuery, ignoreCase = true) ||
                    surah.transliteration.contains(searchQuery, ignoreCase = true) ||
                    surah.arabicName.contains(searchQuery) ||
                    surah.number.toString() == searchQuery.trim()

            val matchesFilter = when (filterType) {
                "Popular" -> surah.number in popularSurahNumbers
                "Makki" -> surah.revelationType == "Makki"
                "Madani" -> surah.revelationType == "Madani"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(filteredSurahs, key = { it.number }) { surah ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSurahSelected(surah) }
                    .testTag("surah_item_${surah.number}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Surah number pill
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkBackground)
                                .border(1.dp, InShotYellow.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${surah.number}",
                                color = InShotYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Column {
                            Text(
                                text = surah.transliteration,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${surah.ayahCount} Ayahs • ${surah.revelationType}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Arabic Calligraphy Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = surah.arabicName,
                            color = InShotYellow,
                            fontFamily = AmiriFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AyahSelectorScreen(
    surah: SurahInfo,
    onBack: () -> Unit,
    onAddAyahs: (List<AyahItem>, Boolean, CaptionStyle) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var ayahs by remember { mutableStateOf<List<AyahItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val selectedAyahNumbers = remember { mutableStateListOf<Int>() }
    var includeUrduTranslation by remember { mutableStateOf(true) }
    var selectedCaptionStyle by remember { mutableStateOf(CaptionStyle.ARABIC_GOLD) }

    LaunchedEffect(surah.number) {
        isLoading = true
        ayahs = QuranRepository.getAyahsForSurah(surah.number)
        // Default: pre-select first 2 ayahs
        if (ayahs.isNotEmpty()) {
            selectedAyahNumbers.clear()
            selectedAyahNumbers.add(ayahs.first().numberInSurah)
        }
        isLoading = false
    }

    // Top Header: Back + Surah Title
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Column {
                Text(
                    text = "${surah.transliteration} (${surah.arabicName})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Tick mark Ayahs to add to video",
                    color = InShotCyan,
                    fontSize = 10.sp
                )
            }
        }

        // Quick Select/Deselect All button
        TextButton(
            onClick = {
                if (selectedAyahNumbers.size == ayahs.size) {
                    selectedAyahNumbers.clear()
                } else {
                    selectedAyahNumbers.clear()
                    selectedAyahNumbers.addAll(ayahs.map { it.numberInSurah })
                }
            }
        ) {
            Text(
                text = if (selectedAyahNumbers.size == ayahs.size) "Deselect" else "Select All",
                color = InShotYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Urdu translation toggle & style selector
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { includeUrduTranslation = !includeUrduTranslation }
        ) {
            Checkbox(
                checked = includeUrduTranslation,
                onCheckedChange = { includeUrduTranslation = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = InShotCyan,
                    checkmarkColor = Color.Black
                ),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "مع اردو ترجمہ (With Urdu)", color = Color.White, fontSize = 11.sp)
        }

        // Gold vs Emerald style toggle
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selectedCaptionStyle == CaptionStyle.ARABIC_GOLD) Color(0x33FFD700) else Color(0x33004D40),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (selectedCaptionStyle == CaptionStyle.ARABIC_GOLD) InShotYellow else InShotCyan
            ),
            modifier = Modifier.clickable {
                selectedCaptionStyle = if (selectedCaptionStyle == CaptionStyle.ARABIC_GOLD) {
                    CaptionStyle.ARABIC_EMERALD
                } else {
                    CaptionStyle.ARABIC_GOLD
                }
            }
        ) {
            Text(
                text = if (selectedCaptionStyle == CaptionStyle.ARABIC_GOLD) "Style: Gold 🌟" else "Style: Emerald 🌿",
                color = if (selectedCaptionStyle == CaptionStyle.ARABIC_GOLD) InShotYellow else InShotCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = InShotYellow, modifier = Modifier.size(36.dp))
        }
    } else {
        // Ayahs List with Tick Marks
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ayahs, key = { it.numberInSurah }) { ayah ->
                val isTicked = selectedAyahNumbers.contains(ayah.numberInSurah)

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTicked) DarkSurfaceVariant else DarkBackground
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isTicked) 1.5.dp else 1.dp,
                        color = if (isTicked) InShotYellow else DarkBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isTicked) {
                                selectedAyahNumbers.remove(ayah.numberInSurah)
                            } else {
                                selectedAyahNumbers.add(ayah.numberInSurah)
                            }
                        }
                        .testTag("ayah_card_${ayah.numberInSurah}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        // Card Header: Ayah Number + Tick Mark Checkbox
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DarkBackground
                            ) {
                                Text(
                                    text = "آیت ${ayah.numberInSurah}",
                                    color = if (isTicked) InShotYellow else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Prominent Tick Mark Checkbox
                            Checkbox(
                                checked = isTicked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (!selectedAyahNumbers.contains(ayah.numberInSurah)) {
                                            selectedAyahNumbers.add(ayah.numberInSurah)
                                        }
                                    } else {
                                        selectedAyahNumbers.remove(ayah.numberInSurah)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = InShotYellow,
                                    checkmarkColor = Color.Black,
                                    uncheckedColor = TextMuted
                                ),
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("ayah_tick_${ayah.numberInSurah}")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Arabic Ayah with Full Tashkeel (زبر، زیر، پیش، جزم)
                        Text(
                            text = ayah.arabicText,
                            color = if (isTicked) Color.White else Color.White.copy(alpha = 0.85f),
                            fontFamily = AmiriFontFamily,
                            fontSize = 18.sp,
                            textAlign = TextAlign.End,
                            lineHeight = 28.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Urdu Translation
                        if (includeUrduTranslation && ayah.urduTranslation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ayah.urduTranslation,
                                color = InShotYellow.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Bottom Action: Add Selected Ayahs to Video Timeline
    Button(
        onClick = {
            val selectedList = ayahs.filter { selectedAyahNumbers.contains(it.numberInSurah) }
            if (selectedList.isNotEmpty()) {
                onAddAyahs(selectedList, includeUrduTranslation, selectedCaptionStyle)
            }
        },
        enabled = selectedAyahNumbers.isNotEmpty(),
        colors = ButtonDefaults.buttonColors(
            containerColor = InShotRed,
            disabledContainerColor = DarkSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_ayahs_to_timeline_btn")
    ) {
        Icon(Icons.Default.Add, null)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Add ${selectedAyahNumbers.size} Ayahs to Timeline (ٹائم لائن پر شامل کریں)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
