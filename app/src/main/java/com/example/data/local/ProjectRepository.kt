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
            title = "Original Video Project",
            lastModified = System.currentTimeMillis() - 3600000L * 2,
            canvasRatio = CanvasRatio.RATIO_ORIGINAL,
            bgType = CanvasBgType.COLOR,
            bgColorHex = 0xFF000000,
            clips = listOf(
                VideoClip(
                    id = "clip_clean_1",
                    title = "Original Video",
                    durationMs = 8000L,
                    trimStartMs = 0L,
                    trimEndMs = 8000L,
                    filter = FilterType.ORIGINAL,
                    speed = 1.0f,
                    themeGradientStart = 0xFF141920,
                    themeGradientEnd = 0xFF0D1217,
                    sceneIcon = "🎬"
                )
            ),
            audioTracks = emptyList(),
            textOverlays = emptyList(),
            stickerOverlays = emptyList(),
            imageOverlays = emptyList()
        )

        dao.insertProject(project1.toEntity())
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
            if (clip.uriString != null) cObj.put("uriString", clip.uriString)
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
            tObj.put("isQuranAyah", text.isQuranAyah)
            tObj.put("isBasmala", text.isBasmala)
            tObj.put("animationName", text.animationName)
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

        val imageArr = JSONArray()
        imageOverlays.forEach { img ->
            val iObj = JSONObject()
            iObj.put("id", img.id)
            if (img.uriString != null) iObj.put("uriString", img.uriString)
            iObj.put("title", img.title)
            iObj.put("startOffsetMs", img.startOffsetMs)
            iObj.put("durationMs", img.durationMs)
            iObj.put("posX", img.posX.toDouble())
            iObj.put("posY", img.posY.toDouble())
            iObj.put("scale", img.scale.toDouble())
            iObj.put("rotation", img.rotation.toDouble())
            imageArr.put(iObj)
        }
        root.put("images", imageArr)

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
        val imageList = mutableListOf<ImageOverlay>()

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
                            uriString = obj.optString("uriString").takeIf { it.isNotBlank() },
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
                            themeGradientStart = obj.optLong("themeGradientStart", 0xFF0D5C3A),
                            themeGradientEnd = obj.optLong("themeGradientEnd", 0xFF042617),
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
                            artist = obj.optString("artist", "Quran Editor"),
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
                            text = obj.optString("text", ""),
                            startOffsetMs = obj.optLong("startOffsetMs", 0L),
                            durationMs = obj.optLong("durationMs", 3000L),
                            posX = obj.optDouble("posX", 0.5).toFloat(),
                            posY = obj.optDouble("posY", 0.5).toFloat(),
                            fontSizeSp = obj.optDouble("fontSizeSp", 24.0).toFloat(),
                            textColorHex = obj.optLong("textColorHex", 0xFFFFFFFF),
                            bgColorHex = obj.optLong("bgColorHex", 0xAA000000),
                            fontStyle = obj.optString("fontStyle", "Bold"),
                            hasBackgroundBox = obj.optBoolean("hasBackgroundBox", true),
                            isAutoCaption = obj.optBoolean("isAutoCaption", false),
                            isQuranAyah = obj.optBoolean("isQuranAyah", false),
                            isBasmala = obj.optBoolean("isBasmala", false),
                            animationName = obj.optString("animationName", "Fade In")
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
                            emojiOrIcon = obj.optString("emojiOrIcon", "⭐"),
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

            if (root.has("images")) {
                val arr = root.getJSONArray("images")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    imageList.add(
                        ImageOverlay(
                            id = obj.optString("id", "img_$i"),
                            uriString = obj.optString("uriString").takeIf { it.isNotBlank() },
                            title = obj.optString("title", "Image $i"),
                            startOffsetMs = obj.optLong("startOffsetMs", 0L),
                            durationMs = obj.optLong("durationMs", 4000L),
                            posX = obj.optDouble("posX", 0.5).toFloat(),
                            posY = obj.optDouble("posY", 0.5).toFloat(),
                            scale = obj.optDouble("scale", 1.0).toFloat(),
                            rotation = obj.optDouble("rotation", 0.0).toFloat()
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Fallback default
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
            clips = if (clipsList.isNotEmpty()) clipsList else listOf(
                VideoClip(
                    id = "def_clip",
                    title = title,
                    durationMs = durationMs.coerceAtLeast(3000L),
                    themeGradientStart = 0xFF0D5C3A,
                    themeGradientEnd = 0xFF042617
                )
            ),
            audioTracks = audioList,
            textOverlays = textList,
            stickerOverlays = stickerList,
            imageOverlays = imageList
        )
    }
}
