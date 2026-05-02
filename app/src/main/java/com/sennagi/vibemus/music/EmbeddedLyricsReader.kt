package com.sennagi.vibemus.music

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import kotlin.math.max

data class TimedLyricLine(
    val timeMs: Long,
    val text: String,
    val translation: String? = null
)

data class LyricsContent(
    val rawText: String,
    val plainLines: List<String>,
    val timedLines: List<TimedLyricLine>
) {
    val hasTiming: Boolean
        get() = timedLines.isNotEmpty()
}

object EmbeddedLyricsReader {
    suspend fun load(context: Context, song: SongItem): LyricsContent? = withContext(Dispatchers.IO) {
        val rawLyrics = loadLocalLrcText(context, song)
            ?: context.contentResolver.openInputStream(song.contentUri)?.use(::readEmbeddedLyricsText)
            ?: return@withContext null
        parseLyricsContent(rawLyrics)
    }

    private fun readEmbeddedLyricsText(input: InputStream): String? {
        val bytes = input.readBytes()
        if (bytes.size < 4) return null
        return parseId3LyricsFromBytes(bytes) ?: parseFlacLyricsFromBytes(bytes)
    }

    private fun parseId3LyricsFromBytes(bytes: ByteArray): String? {
        if (bytes.size < 10 || bytes.decodeToString(0, 3) != "ID3") return null
        val header = bytes.copyOfRange(0, 10)
        val majorVersion = header[3].toInt() and 0xFF
        val flags = header[5].toInt() and 0xFF
        val tagSize = syncSafeToInt(header, 6)
        if (tagSize <= 0) return null
        val tagEnd = 10 + tagSize
        if (tagEnd > bytes.size) return null

        val tagData = bytes.copyOfRange(10, tagEnd)

        val normalizedTagData = if ((flags and 0x80) != 0) {
            deUnsynchronize(tagData)
        } else {
            tagData
        }

        return when (majorVersion) {
            2 -> parseId3v22Lyrics(normalizedTagData)
            3 -> parseId3v23Or24Lyrics(normalizedTagData, syncSafeFrameSize = false)
            4 -> parseId3v23Or24Lyrics(normalizedTagData, syncSafeFrameSize = true)
            else -> null
        }
    }

    private fun parseFlacLyricsFromBytes(bytes: ByteArray): String? {
        val flacMarker = byteArrayOf('f'.code.toByte(), 'L'.code.toByte(), 'a'.code.toByte(), 'C'.code.toByte())
        val startIndex = indexOfSequence(bytes, flacMarker)
        if (startIndex < 0 || startIndex + 4 > bytes.size) return null

        var offset = startIndex + 4
        var isLastBlock = false
        while (!isLastBlock && offset + 4 <= bytes.size) {
            val blockHeader = bytes[offset].toInt() and 0xFF
            isLastBlock = (blockHeader and 0x80) != 0
            val blockType = blockHeader and 0x7F
            val blockLength =
                ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
                    ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
                    (bytes[offset + 3].toInt() and 0xFF)
            offset += 4
            if (blockLength < 0 || offset + blockLength > bytes.size) break

            if (blockType == 4) {
                val blockData = bytes.copyOfRange(offset, offset + blockLength)
                parseFlacVorbisLyrics(blockData)?.let { return it }
            }
            offset += blockLength
        }
        return null
    }

    private fun parseFlacVorbisLyrics(blockData: ByteArray): String? {
        if (blockData.size < 8) return null
        var offset = 0
        val vendorLength = readLittleEndianInt(blockData, offset) ?: return null
        offset += 4
        if (vendorLength < 0 || offset + vendorLength > blockData.size) return null
        offset += vendorLength

        val commentsCount = readLittleEndianInt(blockData, offset) ?: return null
        offset += 4
        if (commentsCount < 0) return null

        val preferredKeys = listOf("LYRICS", "UNSYNCEDLYRICS", "SYNCEDLYRICS", "USLT", "LRC")
        val found = linkedMapOf<String, String>()
        var count = 0
        while (count < commentsCount && offset + 4 <= blockData.size) {
            val size = readLittleEndianInt(blockData, offset) ?: break
            offset += 4
            if (size <= 0 || offset + size > blockData.size) break
            val comment = decodeText(blockData.copyOfRange(offset, offset + size), 3).orEmpty()
            offset += size

            val separatorIndex = comment.indexOf('=')
            if (separatorIndex > 0) {
                val key = comment.substring(0, separatorIndex).trim().uppercase()
                val value = comment.substring(separatorIndex + 1).trim()
                if (value.isNotBlank() && key in preferredKeys && key !in found) {
                    found[key] = value
                }
            }
            count++
        }

        return preferredKeys.firstNotNullOfOrNull { key -> found[key] }
    }

    private fun loadLocalLrcText(context: Context, song: SongItem): String? {
        val resolver = context.contentResolver
        val trackMeta = queryTrackMeta(context, song.contentUri)
        val baseName = trackMeta.displayName.substringBeforeLast('.', trackMeta.displayName).ifBlank { song.title }
        val relativePath = normalizeRelativePath(trackMeta.relativePath ?: song.folder)
        val candidateNames = linkedSetOf(
            "$baseName.lrc",
            "${song.title}.lrc",
            "${song.title.trim()}.lrc"
        ).filter { it.isNotBlank() }

        relativePath?.let { path ->
            findLrcViaMediaStore(resolver, path, candidateNames)?.let { return it }
            findLrcByFilesystem(path, candidateNames)?.let { return it }
        }

        trackMeta.absolutePath?.let { songPath ->
            val parent = runCatching { File(songPath).parentFile }.getOrNull()
            if (parent != null && parent.exists()) {
                candidateNames.forEach { name ->
                    val candidate = File(parent, name)
                    readTextFileSafely(candidate)?.let { return it }
                }
            }
        }

        return null
    }

    private fun findLrcViaMediaStore(
        resolver: android.content.ContentResolver,
        relativePath: String,
        candidateNames: List<String>
    ): String? {
        val filesUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(MediaStore.Files.FileColumns._ID)

        candidateNames.forEach { fileName ->
            val selection =
                "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND ${MediaStore.MediaColumns.DISPLAY_NAME}=?"
            val args = arrayOf(relativePath, fileName)
            runCatching {
                resolver.query(filesUri, projection, selection, args, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                        val lrcUri = ContentUris.withAppendedId(filesUri, id)
                        resolver.openInputStream(lrcUri)?.use { stream ->
                            decodeTextGuessing(stream.readBytes())
                        }
                    } else {
                        null
                    }
                }
            }.getOrNull()?.let { if (it.isNotBlank()) return it }
        }
        return null
    }

    private fun findLrcByFilesystem(relativePath: String, candidateNames: List<String>): String? {
        val root = runCatching { Environment.getExternalStorageDirectory() }.getOrNull() ?: return null
        val folder = File(root, relativePath)
        if (!folder.exists()) return null
        candidateNames.forEach { fileName ->
            val candidate = File(folder, fileName)
            readTextFileSafely(candidate)?.let { return it }
        }
        return null
    }

    private fun readTextFileSafely(file: File): String? {
        if (!file.exists() || !file.isFile || !file.canRead()) return null
        return runCatching {
            decodeTextGuessing(file.readBytes())
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun decodeTextGuessing(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            return bytes.copyOfRange(3, bytes.size).toString(Charsets.UTF_8)
        }
        if (bytes.size >= 2 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xFE.toByte()
        ) {
            return bytes.copyOfRange(2, bytes.size).toString(Charset.forName("UTF-16LE"))
        }
        if (bytes.size >= 2 &&
            bytes[0] == 0xFE.toByte() &&
            bytes[1] == 0xFF.toByte()
        ) {
            return bytes.copyOfRange(2, bytes.size).toString(Charset.forName("UTF-16BE"))
        }
        return runCatching { bytes.toString(Charsets.UTF_8) }
            .getOrElse {
                runCatching { bytes.toString(Charset.forName("GBK")) }
                    .getOrDefault(bytes.toString(Charsets.ISO_8859_1))
            }
    }

    private data class TrackMeta(
        val displayName: String,
        val relativePath: String?,
        val absolutePath: String?
    )

    private fun queryTrackMeta(context: Context, songUri: Uri): TrackMeta {
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.DATA
        )
        return runCatching {
            context.contentResolver.query(songUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    val relativeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                    val absoluteIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex).orEmpty() else ""
                    val relative = if (relativeIndex >= 0) cursor.getString(relativeIndex) else null
                    val absolute = if (absoluteIndex >= 0) cursor.getString(absoluteIndex) else null
                    TrackMeta(
                        displayName = name.ifBlank { "unknown.mp3" },
                        relativePath = relative,
                        absolutePath = absolute
                    )
                } else {
                    TrackMeta(displayName = "unknown.mp3", relativePath = null, absolutePath = null)
                }
            } ?: TrackMeta(displayName = "unknown.mp3", relativePath = null, absolutePath = null)
        }.getOrDefault(TrackMeta(displayName = "unknown.mp3", relativePath = null, absolutePath = null))
    }

    private fun normalizeRelativePath(path: String?): String? {
        val normalized = path.orEmpty().trim().trimStart('/').trimEnd('/')
        if (normalized.isBlank()) return null
        return "$normalized/"
    }

    private fun parseId3v22Lyrics(tagData: ByteArray): String? {
        var offset = 0
        while (offset + 6 <= tagData.size) {
            val frameId = tagData.decodeToString(offset, offset + 3)
            if (frameId.all { it == '\u0000' }) break

            val frameSize = ((tagData[offset + 3].toInt() and 0xFF) shl 16) or
                ((tagData[offset + 4].toInt() and 0xFF) shl 8) or
                (tagData[offset + 5].toInt() and 0xFF)
            val frameStart = offset + 6
            val frameEnd = frameStart + frameSize
            if (frameSize <= 0 || frameEnd > tagData.size) break

            val payload = tagData.copyOfRange(frameStart, frameEnd)
            when (frameId) {
                "ULT" -> decodeUsltLikeFrame(payload)?.let { return it }
                "SLT" -> decodeSyltFrame(payload)?.let { return it }
            }

            offset = frameEnd
        }
        return null
    }

    private fun parseId3v23Or24Lyrics(tagData: ByteArray, syncSafeFrameSize: Boolean): String? {
        var offset = 0
        while (offset + 10 <= tagData.size) {
            val frameId = tagData.decodeToString(offset, offset + 4)
            if (frameId.all { it == '\u0000' }) break

            val frameSize = if (syncSafeFrameSize) {
                syncSafeToInt(tagData, offset + 4)
            } else {
                ((tagData[offset + 4].toInt() and 0xFF) shl 24) or
                    ((tagData[offset + 5].toInt() and 0xFF) shl 16) or
                    ((tagData[offset + 6].toInt() and 0xFF) shl 8) or
                    (tagData[offset + 7].toInt() and 0xFF)
            }
            val frameFlags2 = tagData[offset + 9].toInt() and 0xFF
            val frameStart = offset + 10
            val frameEnd = frameStart + frameSize
            if (frameSize <= 0 || frameEnd > tagData.size) break

            // Skip encrypted/compressed/unsynchronised frame payloads we don't explicitly support.
            if ((frameFlags2 and 0x0C) == 0) {
                val payload = tagData.copyOfRange(frameStart, frameEnd)
                when (frameId) {
                    "USLT" -> decodeUsltLikeFrame(payload)?.let { return it }
                    "SYLT" -> decodeSyltFrame(payload)?.let { return it }
                }
            }

            offset = frameEnd
        }
        return null
    }

    private fun decodeUsltLikeFrame(payload: ByteArray): String? {
        if (payload.size <= 4) return null
        val encoding = payload[0].toInt() and 0xFF
        val descriptorStart = 4
        val (_, lyricsStart) = readTerminatedText(payload, descriptorStart, encoding)
        if (lyricsStart >= payload.size) return null
        return decodeText(payload.copyOfRange(lyricsStart, payload.size), encoding)
            ?.replace("\u0000", "")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun decodeSyltFrame(payload: ByteArray): String? {
        if (payload.size <= 6) return null
        val encoding = payload[0].toInt() and 0xFF
        val (_, contentStart) = readTerminatedText(payload, 6, encoding)
        if (contentStart >= payload.size) return null

        val terminatorLength = textTerminatorLength(encoding)
        var offset = contentStart
        val builder = StringBuilder()
        var sawNewline = false

        while (offset < payload.size) {
            val (text, nextOffset) = readTerminatedText(payload, offset, encoding)
            offset = nextOffset
            if (offset + 4 > payload.size) break
            offset += 4 // timestamp
            if (text.isNotEmpty()) {
                if (builder.isNotEmpty() && !text.startsWith(" ") && !builder.last().isWhitespace()) {
                    builder.append(' ')
                }
                builder.append(text)
                if (text.contains('\n')) sawNewline = true
            } else if (terminatorLength == 2 && offset < payload.size && payload[offset - 1].toInt() == 0) {
                builder.append('\n')
                sawNewline = true
            }
        }

        return builder.toString()
            .replace("\u0000", "")
            .replace(Regex("[ \\t]*\\n[ \\t]*"), "\n")
            .trim()
            .takeIf { it.isNotBlank() && (sawNewline || it.length > 8) }
    }

    private fun parseLyricsContent(rawLyrics: String): LyricsContent? {
        val normalized = rawLyrics
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .replace("\uFEFF", "")
            .trim()
        if (normalized.isBlank()) return null

        val timedLines = parseTimedLyrics(normalized)
        val plainLines = normalized
            .lines()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }

        return LyricsContent(
            rawText = normalized,
            plainLines = plainLines,
            timedLines = timedLines
        )
    }

    private fun parseTimedLyrics(text: String): List<TimedLyricLine> {
        val timestampRegex = Regex("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?]")
        val timedLines = mutableListOf<TimedLyricLine>()

        text.lineSequence().forEach { line ->
            val matches = timestampRegex.findAll(line).toList()
            if (matches.isEmpty()) return@forEach

            val lyricText = line.replace(timestampRegex, "").trim()
            if (lyricText.isBlank()) return@forEach

            val (original, translation) = splitLyricAndTranslation(lyricText)

            matches.forEach { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: return@forEach
                val seconds = match.groupValues[2].toLongOrNull() ?: return@forEach
                val fractionText = match.groupValues[3]
                val millis = when (fractionText.length) {
                    0 -> 0L
                    1 -> fractionText.toLong() * 100L
                    2 -> fractionText.toLong() * 10L
                    else -> fractionText.take(3).toLong()
                }
                timedLines += TimedLyricLine(
                    timeMs = max(0L, minutes * 60_000L + seconds * 1_000L + millis),
                    text = original,
                    translation = translation
                )
            }
        }

        return timedLines
            .distinctBy { "${it.timeMs}-${it.text}" }
            .sortedBy { it.timeMs }
    }

    private fun splitLyricAndTranslation(text: String): Pair<String, String?> {
        val thinSpace = "\u2009"
        val index = text.indexOf(thinSpace)
        if (index > 0 && index < text.length - 1) {
            val original = text.substring(0, index).trimEnd()
            val translation = text.substring(index + 1).trimStart()
            if (original.isNotBlank() && translation.isNotBlank()) {
                return original to translation
            }
        }
        return text to null
    }

    private fun readTerminatedText(data: ByteArray, start: Int, encoding: Int): Pair<String, Int> {
        val terminatorLength = textTerminatorLength(encoding)
        var index = start
        while (index + terminatorLength <= data.size) {
            val atTerminator = if (terminatorLength == 1) {
                data[index].toInt() == 0
            } else {
                data[index].toInt() == 0 && data[index + 1].toInt() == 0
            }
            if (atTerminator) {
                val text = decodeText(data.copyOfRange(start, index), encoding).orEmpty()
                return text to (index + terminatorLength)
            }
            index += terminatorLength
        }
        return decodeText(data.copyOfRange(start, data.size), encoding).orEmpty() to data.size
    }

    private fun decodeText(bytes: ByteArray, encoding: Int): String? {
        if (bytes.isEmpty()) return ""
        return runCatching {
            when (encoding) {
                0 -> bytes.toString(Charsets.ISO_8859_1)
                1 -> bytes.toString(Charset.forName("UTF-16"))
                2 -> bytes.toString(Charset.forName("UTF-16BE"))
                3 -> bytes.toString(Charsets.UTF_8)
                else -> bytes.toString(Charsets.UTF_8)
            }
        }.getOrNull()
    }

    private fun textTerminatorLength(encoding: Int): Int {
        return if (encoding == 1 || encoding == 2) 2 else 1
    }

    private fun syncSafeToInt(bytes: ByteArray, offset: Int): Int {
        return ((bytes[offset].toInt() and 0x7F) shl 21) or
            ((bytes[offset + 1].toInt() and 0x7F) shl 14) or
            ((bytes[offset + 2].toInt() and 0x7F) shl 7) or
            (bytes[offset + 3].toInt() and 0x7F)
    }

    private fun deUnsynchronize(data: ByteArray): ByteArray {
        val output = ArrayList<Byte>(data.size)
        var index = 0
        while (index < data.size) {
            val current = data[index]
            if (
                index + 1 < data.size &&
                current.toInt() == 0xFF &&
                data[index + 1].toInt() == 0x00
            ) {
                output += current
                index += 2
            } else {
                output += current
                index++
            }
        }
        return ByteArray(output.size) { output[it] }
    }

    private fun readLittleEndianInt(data: ByteArray, offset: Int): Int? {
        if (offset < 0 || offset + 4 > data.size) return null
        return (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8) or
            ((data[offset + 2].toInt() and 0xFF) shl 16) or
            ((data[offset + 3].toInt() and 0xFF) shl 24)
    }

    private fun indexOfSequence(source: ByteArray, target: ByteArray): Int {
        if (target.isEmpty() || source.size < target.size) return -1
        val lastStart = source.size - target.size
        for (start in 0..lastStart) {
            var matched = true
            for (index in target.indices) {
                if (source[start + index] != target[index]) {
                    matched = false
                    break
                }
            }
            if (matched) return start
        }
        return -1
    }
}
