package com.example.muzican2

import android.content.Context
import android.util.Log
import android.widget.SeekBar

class SongChanger(context: Context, seekBar: SeekBar, playerbar: PlayerbarContents) {
    private val mediaPlayerFunctions = MediaPlayerFunctions(context, seekBar, playerbar)

    fun nextAudio() {
        val musicListSize = MusicContainer.musicList!!.size

        // end of list - start from index = 0
        if (MusicContainer.currentMusicPosition >= musicListSize-1) {
            val nextMusic = MusicContainer.musicList!![0]

            mediaPlayerFunctions.playAudio(nextMusic)

            MusicChange.musicChangeListen.value = 1

            return
        }

        // normal case
        val nextMusic = MusicContainer.musicList!![MusicContainer.currentMusicPosition+1]
        Log.i("position", MusicContainer.currentMusicPosition.toString())
        Log.i("title", MusicContainer.currentMusic!!.title)

        mediaPlayerFunctions.playAudio(nextMusic)

        MusicChange.musicChangeListen.value = 1
    }
}