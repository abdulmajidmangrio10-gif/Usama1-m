package com.example.data.local

import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class ProjectRepository(private val dao: ProjectDao) {

    val allProjects: Flow<List<Project>> = dao.getAllProjects().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun getProjectById(id: Long): Project? {
        return dao.getProjectById(id)?.toDomainModel()
    }

    suspend fun saveProject(project: Project): Long {
        val entity = project.toEntity()
        return dao.insertProject(entity)
    }

    suspend fun deleteProject(id: Long) {
        dao.deleteProjectById(id)
    }

    suspend fun checkAndSeedInitialProjects() {
        if (dao.getProjectCount() == 0) {
            seedSampleProjects()
        }
    }

    private suspend fun seedSampleProjects() {
        val project1 = Project(
            id = 0,
            title = "Tokyo Neon Reel",
            lastModified = System.currentTimeMillis() - 3600000L * 3,
            canvasRatio = CanvasRatio.RATIO_9_16,
            bgType = CanvasBgType.BLUR,
            bgColorHex = 0xFF121217,
            clips = listOf(
                VideoClip(
                    id = "clip_1",
                    title = "City Sunset",
                    durationMs = 4500L,
                    trimStartMs = 500L,
                    trimEndMs = 4200L,
                    filter = FilterType.WARM,
                    speed = 1.0f,
                    themeGradientStart = 0xFFFF5252,
                    themeGradientEnd = 0xFFFF7A00,
                    sceneIcon = "🌇"
                ),
                VideoClip(
                    id = "clip_2",
                    title = "Neon Crossing",
                    durationMs = 5000L,
                    trimStartMs = 0L,
                    trimEndMs = 4800L,
                    filter = FilterType.CYBERPUNK,
                    speed = 1.2f,
                    themeGradientStart = 0xFF7C4DFF,
                    themeGradientEnd = 0xFF00E5FF,
                    sceneIcon = "🌃"
                ),
                VideoClip(
                    id = "clip_3",
                    title = "Cyber Beat Drop",
                    durationMs = 3800L,
                    trimStartMs = 200L,
                    trimEndMs = 3600L,
                    filter = FilterType.GLITCH,
                    speed = 1.0f,
                    themeGradientStart = 0xFFFF007F,
                    themeGradientEnd = 0xFF7928CA,
                    sceneIcon = "⚡"
                )
            ),
            audioTracks = listOf(
                AudioTrack(
                    id = "audio_1",
                    title = "Midnight Drive",
                    artist = "InShot Music",
                    durationMs = 12000L,
                    startOffsetMs = 0L,
                    volume = 0.9f
                )
            ),
            textOverlays = listOf(
                TextOverlay(
                    id = "text_1",
                    text = "NEON NIGHTS 🌙",
                    startOffsetMs = 800L,
                    durationMs = 3500L,
                    posX = 0.5f,
                    posY = 0.25f,
                    fontSizeSp = 26f,
                    textColorHex = 0xFFFFFFFF,
                    bgColorHex = 0x88FF2A55
                )
            ),
            stickerOverlays = listOf(
                StickerOverlay(
                    id = "sticker_1",
                    emojiOrIcon = "🔥",
                    startOffsetMs = 1200L,
                    durationMs = 3000L,
                    posX = 0.5f,
                    posY = 0.72f,
                    scale = 1.2f
                )
            )
        )

        val project2 = Project(
            id = 0,
            title = "Aesthetic Cafe Vlog",
            lastModified = System.currentTimeMillis() - 86400000L * 1,
            canvasRatio = CanvasRatio.RATIO_1_1,
            bgType = CanvasBgType.GRADIENT,
            bgColorHex = 0xFF1E202A,
            clips = listOf(
                VideoClip(
                    id = "clip_cafe_1",
                    title = "Pour Over Coffee",
                    durationMs = 4000L,
                    trimStartMs = 0L,
                    trimEndMs = 3800L,
                    filter = FilterType.VINTAGE,
                    speed = 0.8f,
                    themeGradientStart = 0xFF8D6E63,
                    themeGradientEnd = 0xFFD7CCC8,
                    sceneIcon = "☕"
                ),
                VideoClip(
                    id = "clip_cafe_2",
                    title = "Croissant & Morning",
                    durationMs = 3500L,
                    trimStartMs = 300L,
                    trimEndMs = 3300L,
                    filter = FilterType.WARM,
                    speed = 1.0f,
                    themeGradientStart = 0xFFFFB74D,
                    themeGradientEnd = 0xFFFFE0B2,
                    sceneIcon = "🥐"
                )
            ),
            audioTracks = listOf(
                AudioTrack(
                    id = "audio_cafe",
                    title = "Lo-Fi Morning Chords",
                    artist = "Coffee Studio",
                    durationMs = 8000L,
                    startOffsetMs = 0L,
                    volume = 0.7f
                )
            ),
            textOverlays = listOf(
                TextOverlay(
                    id = "text_cafe",
                    text = "Sunday Bliss ✨",
                    startOffsetMs = 500L,
                    durationMs = 3200L,
                    posX = 0.5f,
                    posY = 0.82f,
                    fontSizeSp = 22f,
                    textColorHex = 0xFFFFFFFF,
                    bgColorHex = 0x994E342E
                )
            ),
            stickerOverlays = listOf(
                StickerOverlay(
                    id = "sticker_cafe",
                    emojiOrIcon = "✨",
                    startOffsetMs = 400L,
                    durationMs = 3500L,
                    posX = 0.8f,
                    posY = 0.2f,
                    scale = 1.1f
                )
            )
        )

        saveProject(project1)
        saveProject(project2)
    }

    private fun Project.toEntity(): ProjectEntity {
        val root = JSONObject()
        val clipsArr = JSONArray()
        clips.forEach { clip ->
            val cObj = JSONObject()
            cObj.put("id", clip.id)
            cObj.put("title", clip.title)
            cObj.put("durationMs", clip.durationMs)
            cObj.put("trimStartMs", clip.trimStartMs)
            cObj.put("trimEndMs", clip.trimEndMs)
            cObj.put("speed", clip.speed.toDouble())
            cObj.put("volume", clip.volume.toDouble())
            cObj.put("rotationAngle", clip.rotationAngle.toDouble())
            cObj.put("isFlippedH", clip.isFlippedH)
            cObj.put("isFlippedV", clip.isFlippedV)
            cObj.put("filter", clip.filter.name)
            cObj.put("brightness", clip.brightness.toDouble())
            cObj.put("contrast", clip.contrast.toDouble())
            cObj.put("saturation", clip.saturation.toDouble())
            cObj.put("vignette", clip.vignette.toDouble())
            cObj.put("themeGradientStart", clip.themeGradientStart)
            cObj.put("themeGradientEnd", clip.themeGradientEnd)
            cObj.put("sceneIcon", clip.sceneIcon)
            clipsArr.put(cObj)
        }
        root.put("clips", clipsArr)

        val audioArr = JSONArray()
        audioTracks.forEach { audio ->
            val aObj = JSONObject()
            aObj.put("id", audio.id)
            aObj.put("title", audio.title)
            aObj.put("artist", audio.artist)
            aObj.put("durationMs", audio.durationMs)
            aObj.put("startOffsetMs", audio.startOffsetMs)
            aObj.put("volume", audio.volume.toDouble())
            aObj.put("isVoiceover", audio.isVoiceover)
            aObj.put("colorHex", audio.colorHex)
            audioArr.put(aObj)
        }
        root.put("audio", audioArr)

        val textArr = JSONArray()
        textOverlays.forEach { text ->
            val tObj = JSONObject()
            tObj.put("id", text.id)
            tObj.put("text", text.text)
            tObj.put("startOffsetMs", text.startOffsetMs)
            tObj.put("durationMs", text.durationMs)
            tObj.put("posX", text.posX.toDouble())
            tObj.put("posY", text.posY.toDouble())
            tObj.put("fontSizeSp", text.fontSizeSp.toDouble())
            tObj.put("textColorHex", text.textColorHex)
            tObj.put("bgColorHex", text.bgColorHex)
            tObj.put("fontStyle", text.fontStyle)
            tObj.put("hasBackgroundBox", text.hasBackgroundBox)
            tObj.put("isAutoCaption", text.isAutoCaption)
            textArr.put(tObj)
        }
        root.put("texts", textArr)

        val stickerArr = JSONArray()
        stickerOverlays.forEach { sticker ->
            val sObj = JSONObject()
            sObj.put("id", sticker.id)
            sObj.put("emojiOrIcon", sticker.emojiOrIcon)
            sObj.put("startOffsetMs", sticker.startOffsetMs)
            sObj.put("durationMs", sticker.durationMs)
            sObj.put("posX", sticker.posX.toDouble())
            sObj.put("posY", sticker.posY.toDouble())
            sObj.put("scale", sticker.scale.toDouble())
            sObj.put("rotation", sticker.rotation.toDouble())
            stickerArr.put(sObj)
        }
        root.put("stickers", stickerArr)

        return ProjectEntity(
            id = id,
            title = title,
            lastModified = lastModified,
            canvasRatioName = canvasRatio.name,
            bgTypeName = bgType.name,
            bgColorHex = bgColorHex,
            durationMs = totalDurationMs,
            clipsCount = clips.size,
            projectJson = root.toString()
        )
    }

    private fun ProjectEntity.toDomainModel(): Project {
        val clipsList = mutableListOf<VideoClip>()
        val audioList = mutableListOf<AudioTrack>()
        val textList = mutableListOf<TextOverlay>()
        val stickerList = mutableListOf<StickerOverlay>()

        try {
            val root = JSONObject(projectJson)

            if (root.has("clips")) {
                val arr = root.getJSONArray("clips")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    clipsList.add(
                        VideoClip(
                            id = obj.optString("id", "clip_$i"),
                            title = obj.optString("title", "Clip $i"),
                            durationMs = obj.optLong("durationMs", 3000L),
                            trimStartMs = obj.optLong("trimStartMs", 0L),
                            trimEndMs = obj.optLong("trimEndMs", obj.optLong("durationMs", 3000L)),
                            speed = obj.optDouble("speed", 1.0).toFloat(),
                            volume = obj.optDouble("volume", 1.0).toFloat(),
                            rotationAngle = obj.optDouble("rotationAngle", 0.0).toFloat(),
                            isFlippedH = obj.optBoolean("isFlippedH", false),
                            isFlippedV = obj.optBoolean("isFlippedV", false),
                            filter = try {
                                FilterType.valueOf(obj.optString("filter", FilterType.ORIGINAL.name))
                            } catch (_: Exception) {
                                FilterType.ORIGINAL
                            },
                            brightness = obj.optDouble("brightness", 0.0).toFloat(),
                            contrast = obj.optDouble("contrast", 1.0).toFloat(),
                            saturation = obj.optDouble("saturation", 1.0).toFloat(),
                            vignette = obj.optDouble("vignette", 0.0).toFloat(),
                            themeGradientStart = obj.optLong("themeGradientStart", 0xFFFF5252),
                            themeGradientEnd = obj.optLong("themeGradientEnd", 0xFFFF7A00),
                            sceneIcon = obj.optString("sceneIcon", "🎬")
                        )
                    )
                }
            }

            if (root.has("audio")) {
                val arr = root.getJSONArray("audio")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    audioList.add(
                        AudioTrack(
                            id = obj.optString("id", "audio_$i"),
                            title = obj.optString("title", "Track $i"),
                            artist = obj.optString("artist", "InShot"),
                            durationMs = obj.optLong("durationMs", 5000L),
                            startOffsetMs = obj.optLong("startOffsetMs", 0L),
                            volume = obj.optDouble("volume", 1.0).toFloat(),
                            isVoiceover = obj.optBoolean("isVoiceover", false),
                            colorHex = obj.optLong("colorHex", 0xFF00E5FF)
                        )
                    )
                }
            }

            if (root.has("texts")) {
                val arr = root.getJSONArray("texts")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    textList.add(
                        TextOverlay(
                            id = obj.optString("id", "text_$i"),
                            text = obj.optString("text", "Text"),
                            startOffsetMs = obj.optLong("startOffsetMs", 0L),
                            durationMs = obj.optLong("durationMs", 2000L),
                            posX = obj.optDouble("posX", 0.5).toFloat(),
                            posY = obj.optDouble("posY", 0.5).toFloat(),
                            fontSizeSp = obj.optDouble("fontSizeSp", 24.0).toFloat(),
                            textColorHex = obj.optLong("textColorHex", 0xFFFFFFFF),
                            bgColorHex = obj.optLong("bgColorHex", 0xAA000000),
                            fontStyle = obj.optString("fontStyle", "Bold"),
                            hasBackgroundBox = obj.optBoolean("hasBackgroundBox", true),
                            isAutoCaption = obj.optBoolean("isAutoCaption", false)
                        )
                    )
                }
            }

            if (root.has("stickers")) {
                val arr = root.getJSONArray("stickers")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    stickerList.add(
                        StickerOverlay(
                            id = obj.optString("id", "sticker_$i"),
                            emojiOrIcon = obj.optString("emojiOrIcon", "✨"),
                            startOffsetMs = obj.optLong("startOffsetMs", 0L),
                            durationMs = obj.optLong("durationMs", 2000L),
                            posX = obj.optDouble("posX", 0.5).toFloat(),
                            posY = obj.optDouble("posY", 0.5).toFloat(),
                            scale = obj.optDouble("scale", 1.0).toFloat(),
                            rotation = obj.optDouble("rotation", 0.0).toFloat()
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // fallback gracefully
        }

        return Project(
            id = id,
            title = title,
            lastModified = lastModified,
            canvasRatio = try {
                CanvasRatio.valueOf(canvasRatioName)
            } catch (_: Exception) {
                CanvasRatio.RATIO_9_16
            },
            bgType = try {
                CanvasBgType.valueOf(bgTypeName)
            } catch (_: Exception) {
                CanvasBgType.BLUR
            },
            bgColorHex = bgColorHex,
            clips = clipsList,
            audioTracks = audioList,
            textOverlays = textList,
            stickerOverlays = stickerList
        )
    }
}
