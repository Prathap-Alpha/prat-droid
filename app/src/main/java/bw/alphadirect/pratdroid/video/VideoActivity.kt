package bw.alphadirect.pratdroid.video

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity

/**
 * Parked-video player. NOTE: this plays on the PHONE only.
 * Android Auto (phone projection, e.g. Ford SYNC) does not support video apps — parked video
 * is an Android Automotive OS feature. android:appCategory="video" + parked-pause behaviour
 * only take effect on a real Automotive-OS head unit.
 */
class VideoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val video = VideoView(this)
        setContentView(video)
        video.setMediaController(MediaController(this).also { it.setAnchorView(video) })
        // Open test sample. Replace with your own parked-video source.
        video.setVideoURI(
            Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
        )
        video.setOnPreparedListener { it.start() }
    }
}
