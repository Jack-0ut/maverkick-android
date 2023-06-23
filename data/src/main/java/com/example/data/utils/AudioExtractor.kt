package com.example.data.utils

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import java.io.File
import java.nio.ByteBuffer

/**
 * Extract the audio file from the video file
 * @param context - the context, where it's working
 **/
class AudioExtractor(private val context: Context) {

    fun extractAudioFromVideo(videoUri: Uri, output: File) {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, videoUri, null)

        val numTracks = extractor.trackCount
        var audioTrackIndex = -1
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio/")) {
                audioTrackIndex = i
                break
            }
        }
        if (audioTrackIndex < 0) {
            throw IllegalArgumentException("No audio track found in $videoUri")
        }

        extractor.selectTrack(audioTrackIndex)

        val format = extractor.getTrackFormat(audioTrackIndex)
        val mediaMuxer = MediaMuxer(output.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val trackIndex = mediaMuxer.addTrack(format)
        mediaMuxer.start()

        val buffer = ByteBuffer.allocate(1024 * 1024)  // 1MB buffer
        val info = MediaCodec.BufferInfo()

        while (true) {
            info.offset = 0
            info.size = extractor.readSampleData(buffer, 0)

            if (info.size < 0) {
                break
            }

            info.presentationTimeUs = extractor.sampleTime

            // Convert extractor flags to codec flags.
            info.flags = when (extractor.sampleFlags) {
                MediaExtractor.SAMPLE_FLAG_SYNC -> MediaCodec.BUFFER_FLAG_KEY_FRAME
                MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME -> MediaCodec.BUFFER_FLAG_PARTIAL_FRAME
                else -> 0
            }

            mediaMuxer.writeSampleData(trackIndex, buffer, info)
            extractor.advance()
        }
    }
}
