package com.example.muzican2

import android.widget.SeekBar
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds

class SeekBarFunctions(private val seekBar: SeekBar) {

    // main function
    fun handleSeekBar() {
        val job = Job()

        refreshSeekBar(job)

        job.complete()
    }

    // set progress on seekbar
    private fun setSeekBarTime(seconds: Int) {
        // set min and max
        seekBar.min = 0
        val musicDuration = MusicContainer.currentMusic!!.duration
        seekBar.max = musicDuration

        // store max for detailedplayer
        MusicContainer.currentMusicDuration = musicDuration

        seekBar.progress = seconds
    }

    // return mediaplayer position
    private fun getSongProgress(): Int {
        if (MusicAdapter.mediaPlayer == null) return 0

        val mediaPlayerTime = MusicAdapter.mediaPlayer!!.currentPosition

        val seconds = mediaPlayerTime.seconds.inWholeSeconds.toInt() / 1000

        // store current song time for detailedplayer
        MusicContainer.currentMusicTime = seconds

        return seconds
    }

    // repeat everything every 200ms
    private fun refreshSeekBar(job: Job) {
        if (MusicAdapter.mediaPlayer == null) return

        val scope = CoroutineScope(job + Dispatchers.Main)

        // setting the seekbar
        setSeekBarTime(getSongProgress())

        // every .2s call this function again to set seekbar
        if (MusicAdapter.mediaPlayer!!.isPlaying)
            scope.launch {
                delay(200)
                refreshSeekBar(job)
            }
    }
}