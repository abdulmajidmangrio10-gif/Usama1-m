package com.example.model

enum class CanvasRatio(val label: String, val ratioWidth: Float, val ratioHeight: Float, val subtitle: String) {
    RATIO_9_16("9:16", 9f, 16f, "Reels / TikTok"),
    RATIO_1_1("1:1", 1f, 1f, "Instagram"),
    RATIO_16_9("16:9", 16f, 9f, "YouTube"),
    RATIO_4_5("4:5", 4f, 5f, "Feed Post"),
    RATIO_3_4("3:4", 3f, 4f, "Classic"),
    RATIO_ORIGINAL("Original", 16f, 9f, "Fit Source");

    val aspectRatio: Float
        get() = ratioWidth / ratioHeight
}

enum class FilterType(val displayName: String, val description: String) {
    ORIGINAL("Original", "Natural colors"),
    CINEMATIC("Cinema", "Moody teal & orange"),
    WARM("Golden", "Warm sunset glow"),
    VINTAGE("Vintage", "90s warm analog film"),
    BLACK_WHITE("Noir B&W", "High contrast monochrome"),
    COOL("Ice Cool", "Arctic blue chill"),
    VIVID("Vivid Pop", "Boosted vibrant saturation"),
    CYBERPUNK("Cyberpunk", "Vivid neon & violet"),
    GLITCH("Glitch Art", "RGB split & edge grain"),
    SEPIA("Sepia", "Antique nostalgia")
}

data class VideoClip(
    val id: String,
    val title: String,
    val durationMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs,
    val uriString: String? = null,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val rotationAngle: Float = 0f,
    val isFlippedH: Boolean = false,
    val isFlippedV: Boolean = false,
    val filter: FilterType = FilterType.ORIGINAL,
    val brightness: Float = 0f,       // -1.0 to +1.0
    val contrast: Float = 1.0f,       // 0.5 to 2.0
    val saturation: Float = 1.0f,     // 0.0 to 2.0
    val vignette: Float = 0.0f,       // 0.0 to 1.0
    val exposure: Float = 0.0f,
    val temperature: Float = 0.0f,
    val themeGradientStart: Long = 0xFF0D5C3A,
    val themeGradientEnd: Long = 0xFF042617,
    val sceneIcon: String = "🎬"
) {
    val trimmedDurationMs: Long
        get() = ((trimEndMs - trimStartMs).coerceAtLeast(100L) / speed).toLong()
}

data class AudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val startOffsetMs: Long = 0L,
    val volume: Float = 1.0f,
    val isVoiceover: Boolean = false,
    val colorHex: Long = 0xFF00E5FF
)

data class TextOverlay(
    val id: String,
    val text: String,
    val startOffsetMs: Long,
    val durationMs: Long,
    val posX: Float = 0.5f, // Normalized 0..1
    val posY: Float = 0.5f, // Normalized 0..1
    val fontSizeSp: Float = 24f,
    val textColorHex: Long = 0xFFFFFFFF,
    val bgColorHex: Long = 0xAA000000,
    val fontStyle: String = "Bold",
    val fontFamilyId: String = "amiri",
    val hasBackgroundBox: Boolean = true,
    val isAutoCaption: Boolean = false,
    val isQuranAyah: Boolean = false,
    val isBasmala: Boolean = false,
    val outlineEnabled: Boolean = false,
    val outlineColorHex: Long = 0xFF000000,
    val shadowEnabled: Boolean = false,
    val shadowColorHex: Long = 0xAA000000,
    val animationName: String = "Fade In"
)

data class ImageOverlay(
    val id: String,
    val uriString: String? = null,
    val drawableResName: String = "ic_quran_editor_logo",
    val title: String = "Image Overlay",
    val startOffsetMs: Long = 0L,
    val durationMs: Long = 4000L,
    val posX: Float = 0.5f,
    val posY: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f
)

enum class CaptionStyle(
    val displayName: String,
    val textColorHex: Long,
    val bgColorHex: Long,
    val hasBackgroundBox: Boolean,
    val fontSizeSp: Float,
    val sampleText: String
) {
    ARABIC_GOLD("Arabic Gold 🌟", 0xFFFFD700, 0xCC002B1E, true, 26f, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"),
    ARABIC_EMERALD("Emerald Quran 🌿", 0xFFE0F2F1, 0xDD004D40, true, 25f, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"),
    CLEAN_WHITE("Clean White ⚪", 0xFFFFFFFF, 0x00000000, false, 24f, "Subtitles & Captions"),
    GOLD_OUTLINE("Gold Outline ✨", 0xFFFFD700, 0x00000000, false, 26f, "QURAN REELS"),
    BLACK_BOX("Black Caption ⬛", 0xFFFFFFFF, 0xCC000000, true, 22f, "High Contrast Subtitle"),
    YELLOW_POP("Yellow Highlight 🟡", 0xFFFFEA00, 0xCC111111, true, 24f, "Keyword Auto Caption"),
    MINIMAL_SHADOW("Minimal Shadow 🎬", 0xFFF5F5F5, 0x00000000, false, 24f, "Cinematic Subtitles")
}

enum class CanvasBgType {
    BLUR,
    COLOR,
    GRADIENT
}

data class StickerOverlay(
    val id: String,
    val emojiOrIcon: String,
    val startOffsetMs: Long,
    val durationMs: Long,
    val posX: Float = 0.5f,
    val posY: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f
)

data class Project(
    val id: Long = 0,
    val title: String,
    val lastModified: Long,
    val canvasRatio: CanvasRatio = CanvasRatio.RATIO_9_16,
    val bgType: CanvasBgType = CanvasBgType.BLUR,
    val bgColorHex: Long = 0xFF051C15,
    val clips: List<VideoClip> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val stickerOverlays: List<StickerOverlay> = emptyList(),
    val imageOverlays: List<ImageOverlay> = emptyList()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.trimmedDurationMs }.coerceAtLeast(1000L)
}

enum class EditorTool(val label: String, val iconName: String) {
    VIDEO("Video", "movie"),
    CANVAS("Canvas", "aspect_ratio"),
    QURAN("Quran", "menu_book"),
    BASMALA("Basmala", "auto_awesome"),
    TEXT("Text", "title"),
    AUTO_TEXT("Captions", "closed_caption"),
    AUDIO("Audio", "music_note"),
    MUSIC("Music", "music_note"),
    VOICE("Voice", "mic"),
    IMAGE("Image", "image"),
    FILTER("Filter", "auto_fix_high"),
    ADJUST("Adjust", "tune"),
    SPEED("Speed", "speed"),
    VOLUME("Volume", "volume_up"),
    ROTATE("Rotate", "rotate_right"),
    FLIP("Flip", "flip"),
    DUPLICATE("Duplicate", "content_copy"),
    STICKER("Sticker", "emoji_emotions"),
    TRIM("Trim", "content_cut"),
    SPLIT("Split", "call_split"),
    DELETE("Delete", "delete")
}
