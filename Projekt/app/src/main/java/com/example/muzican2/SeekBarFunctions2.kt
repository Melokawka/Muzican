package com.example.muzican2

import android.widget.SeekBar

class SeekBarFunctions2(private val seekBar: SeekBar) {

    // set min and max
    fun prepareSeekBar() {
        val musicDuration = MusicContainer.currentMusic!!.duration

        seekBar.min = 0
        seekBar.max = musicDuration
    }

    // set progress on seekbar
    fun setSeekBarTime(seconds: Int) {
        seekBar.progress = seconds
    }
}