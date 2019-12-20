package com.example.arfaceexample.record

import android.content.Context
import android.content.res.Configuration
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.Surface
import com.google.ar.sceneform.SceneView
import java.io.File
import java.io.IOException

@Suppress("DEPRECATION")
class VideoRecorder(private val context: Context) {

    companion object {
        private val TAG = VideoRecorder::class.java.simpleName
        private const val DEFAULT_BITRATE = 10000000
        private const val DEFAULT_FRAMERATE = 30

        private val FALLBACK_QUALITY_LEVELS = intArrayOf(
            CamcorderProfile.QUALITY_HIGH,
            CamcorderProfile.QUALITY_2160P,
            CamcorderProfile.QUALITY_1080P,
            CamcorderProfile.QUALITY_720P,
            CamcorderProfile.QUALITY_480P
        )
    }


    // recordingVideoFlag is true when the media recorder is capturing video.
    private var recordingVideoFlag = false

    private var mediaRecorder: MediaRecorder? = null

    private var videoSize: Size? = null

    private var sceneView: SceneView? = null
    private var videoCodec = 0
    private var videoDirectory: File? = null
    private var videoBaseName: String? = null
    private var videoPath: File? = null
    private var bitRate = DEFAULT_BITRATE
    private var frameRate = DEFAULT_FRAMERATE
    private var encoderSurface: Surface? = null

    init {
        recordingVideoFlag = false
    }

    fun getVideoPath(): File {
        return videoPath ?: throw NullPointerException()
    }

    fun setBitRate(bitRate: Int) {
        this.bitRate = bitRate
    }

    fun setFrameRate(frameRate: Int) {
        this.frameRate = frameRate
    }

    fun setSceneView(sceneView: SceneView) {
        this.sceneView = sceneView
    }

    /*return true if recording is now active*/
    fun onToggleRecord(): Boolean {
        if (recordingVideoFlag) {
            stopRecordingVideo()
        } else {
            startRecordingVideo()
        }

        return recordingVideoFlag
    }

    private fun startRecordingVideo() {
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder()
        }

        try {
            buildFilename()
            setUpMediaRecorder()
        } catch (e: IOException) {
            Log.e(TAG, "Exception setting up recorder", e);
            return
        }

        encoderSurface = mediaRecorder?.surface

        sceneView!!.startMirroringToSurface(
            encoderSurface, 0, 0, videoSize!!.width, videoSize!!.height
        )

        recordingVideoFlag = true
    }

    @Throws(IOException::class)
    fun setUpMediaRecorder() {
        if (mediaRecorder == null)
            return

        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)

        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)

        mediaRecorder?.setOutputFile(videoPath?.absolutePath)
        mediaRecorder?.setVideoEncodingBitRate(bitRate)
        mediaRecorder?.setVideoFrameRate(frameRate)
        mediaRecorder?.setVideoSize(videoSize!!.width, videoSize!!.height)

        mediaRecorder?.setVideoEncoder(videoCodec)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)



        mediaRecorder?.prepare()

        try {
            mediaRecorder?.start()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Exception starting capture: ${e.message}", e);
        }
    }

    fun setVideoSize(widht: Int, height: Int) {
        videoSize = Size(widht, height)
    }

    fun setVideoQuality(quality: Int, orientation: Int) {
        var profile: CamcorderProfile? = null

        if (CamcorderProfile.hasProfile(quality)) {
            profile = CamcorderProfile.get(quality)
        }

        if (profile == null) {
            // Select a quality  that is available on this device.
            for (level in FALLBACK_QUALITY_LEVELS) {
                if (CamcorderProfile.hasProfile(level)) {
                    profile = CamcorderProfile.get(level)
                    break
                }
            }
        }

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setVideoSize(profile!!.videoFrameWidth, profile!!.videoFrameHeight)
        } else {
            setVideoSize(profile!!.videoFrameHeight, profile!!.videoFrameWidth)
        }
        setVideoCodec(profile.videoCodec)
        setBitRate(profile.videoBitRate)
        setFrameRate(profile.videoFrameRate)
    }

    fun setVideoCodec(videoCodec: Int) {
        this.videoCodec = videoCodec
    }

    fun isRecording(): Boolean {
        return recordingVideoFlag
    }

    private fun buildFilename() {
        if (videoDirectory == null) {
            videoDirectory = File(
//                "${context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/Sceneform"
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}/Sceneform"
            )
        }

        if (videoBaseName == null) {
            videoBaseName = "Sample"
        }

        videoPath = File(
            videoDirectory, "$videoBaseName${System.currentTimeMillis()}.mp4"
        )

        val dir = videoPath?.parentFile

        if (!dir!!.exists()) {
            dir.mkdirs()
        }
    }

    private fun stopRecordingVideo() {
        recordingVideoFlag = false

        if (encoderSurface != null) {
            sceneView?.stopMirroringToSurface(encoderSurface)
            encoderSurface = null
        }

        //Stop Recording
        mediaRecorder!!.stop()
        mediaRecorder!!.reset()
    }


}