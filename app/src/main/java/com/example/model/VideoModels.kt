package com.example.model

enum class CanvasRatio(val label: String, val ratioWidth: Float, val ratioHeight: Float, val subtitle: String) {
    RATIO_9_16("9:16", 9f, 16f, "TikTok / Reels"),
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
    VINTAGE("Vintage", "90s warm analog film"),
    CYBERPUNK("Cyberpunk", "Vivid neon & violet"),
    BLACK_WHITE("Noir B&W", "High contrast monochrome"),
    WARM("Golden", "Warm sunset glow"),
    VIVID("Vivid Pop", "Boosted vibrant saturation"),
    GLITCH("Glitch Art", "RGB split & edge grain"),
    COOL("Ice Cool", "Arctic blue chill"),
    SEPIA("Sepia", "Antique nostalgia")
}

data class VideoClip(
    val id: String,
    val title: String,
    val durationMs: Long,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = durationMs,
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
    val themeGradientStart: Long = 0xFFFF5252,
    val themeGradientEnd: Long = 0xFFFF7A00,
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
    val hasBackgroundBox: Boolean = true,
    val isAutoCaption: Boolean = false
)

enum class CaptionStyle(
    val displayName: String,
    val textColorHex: Long,
    val bgColorHex: Long,
    val hasBackgroundBox: Boolean,
    val fontSizeSp: Float,
    val sampleText: String
) {
    VIRAL_YELLOW("Viral Yellow", 0xFFFFD600L, 0xEE000000L, true, 24f, "🔥 VIRAL HIGHLIGHT"),
    KARAOKE_POP("Karaoke Pop", 0xFFFFFFFFL, 0xEEFF2A55L, true, 25f, "⚡ POP BOUNCE"),
    NEON_CYBER("Neon Cyber", 0xFF00E5FFL, 0xEE121217L, true, 22f, "✨ CYBER GLOW"),
    MINIMAL_WHITE("Minimal Dark", 0xFFFFFFFFL, 0x99000000L, true, 20f, "Subtitles clean"),
    BOLD_CREATIVE("Bold Outline", 0xFFFF5252L, 0xFFFFFFFFL, true, 23f, "🎯 CREATIVE BOLD"),
    ARABIC_GOLD("Arabic Gold (ذهب)", 0xFFFFD700L, 0xEE121820L, true, 26f, "بِسْمِ اللَّهِ 🌸"),
    ARABIC_EMERALD("Emerald (زمرد)", 0xFFE0F2F1L, 0xEE004D40L, true, 26f, "الْحَمْدُ لِلَّهِ ✨")
}

data class StickerOverlay(
    val id: String,
    val emojiOrIcon: String,
    val startOffsetMs: Long,
    val durationMs: Long,
    val posX: Float = 0.5f,
    val posY: Float = 0.35f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f
)

enum class CanvasBgType {
    BLUR,
    GRADIENT,
    SOLID_COLOR
}

data class Project(
    val id: Long,
    val title: String,
    val lastModified: Long,
    val canvasRatio: CanvasRatio = CanvasRatio.RATIO_9_16,
    val bgType: CanvasBgType = CanvasBgType.BLUR,
    val bgColorHex: Long = 0xFF121217,
    val clips: List<VideoClip> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val stickerOverlays: List<StickerOverlay> = emptyList()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.trimmedDurationMs }.coerceAtLeast(1000L)
}

enum class EditorTool(val label: String, val iconName: String) {
    CANVAS("Canvas", "aspect_ratio"),
    MUSIC("Music", "music_note"),
    QURAN("Quran", "menu_book"),
    AUTO_TEXT("Auto Text", "closed_caption"),
    STICKER("Sticker", "emoji_emotions"),
    TEXT("Text", "title"),
    FILTER("Filter", "auto_fix_high"),
    PIP("PIP", "picture_in_picture"),
    PRECUT("Precut", "content_cut"),
    SPLIT("Split", "call_split"),
    DELETE("Delete", "delete"),
    SPEED("Speed", "speed"),
    CROP("Crop", "crop"),
    VOLUME("Volume", "volume_up"),
    ROTATE("Rotate", "rotate_right"),
    FLIP("Flip", "flip"),
    DUPLICATE("Duplicate", "content_copy"),
    REVERSE("Reverse", "fast_rewind")
}
