package com.example.muzican2

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File

class MediaPlayerFunctions(private val context: Context, private val seekBar: SeekBar, private val playerbar: PlayerbarContents) {
    companion object {

    }

    fun playAudio(musicToBePlayed: Music) {
        // if player doesn't exist function creates it and plays music
        if (MusicAdapter.mediaPlayer == null)
            createPlayer(musicToBePlayed)

        // if player exists function lets you pause music or change it
        else controlPlayer(musicToBePlayed)

        // keep updating playerbar seekbar from main activity
        SeekBarFunctions(seekBar).handleSeekBar()
        updatePlayerbar()
        MainActivity.playerbarPlayButtonState.value = 1
        DetailedPlayerActivity.detailedPlayerPlayButtonState.value = 1
    }

    private fun createPlayer(musicToBePlayed: Music) {
        MusicContainer.currentMusic = musicToBePlayed
        Log.i("path", MusicContainer.currentMusic!!.path)

        if (MusicContainer.isShuffled == false)
            MusicContainer.currentMusicPosition = MusicContainer.fullMusicList!!.indexOf(musicToBePlayed)

        else MusicContainer.currentMusicPosition = MusicContainer.musicList!!.indexOf(musicToBePlayed)

        MusicAdapter.mediaPlayer = MediaPlayer.create(context, Uri.fromFile(File((musicToBePlayed.path))))
        MusicAdapter.mediaPlayer!!.start()

        // if music ends play next
        MusicAdapter.mediaPlayer!!.setOnCompletionListener {
            SongChanger(context, seekBar, playerbar).nextAudio()
            DetailedPlayerActivity.mediaPlayer = MusicAdapter.mediaPlayer
        }
    }

    private fun controlPlayer(musicToBePlayed: Music) {
        // same song picked
        if (MusicContainer.currentMusic!!.title == musicToBePlayed.title)
            pauseAudio()

        // change song
        else {
            finishAudio()
            playAudio(musicToBePlayed)
        }
    }

    private fun finishAudio() {
        MusicAdapter.mediaPlayer!!.release()
        MusicAdapter.mediaPlayer = null
    }

    fun pauseAudio() {
        if (MusicAdapter.mediaPlayer!!.isPlaying) {
            MusicAdapter.mediaPlayer!!.pause()

            MainActivity.playerbarPlayButtonState.value = 1
            DetailedPlayerActivity.detailedPlayerPlayButtonState.value = 1

            return
        }
        MusicAdapter.mediaPlayer!!.start()

        MainActivity.playerbarPlayButtonState.value = 1
        DetailedPlayerActivity.detailedPlayerPlayButtonState.value = 1
    }

    fun updatePlayerbar() {
        playerbar.binding.playerbarTitle.text = MusicContainer.currentMusic!!.title

        // load song images
        val retriever = MediaMetadataRetriever()

        // set source of music file cover image
        retriever.setDataSource(MusicContainer.currentMusic!!.path)

        Glide.with(context).load(retriever.embeddedPicture)
            .apply(RequestOptions().placeholder(R.drawable.placeholder).centerCrop())
            .into(playerbar.binding.playerbarPic)

        // on song change check if its on fav list and change fav icon accordingly
        if (MusicContainer.favMusicList!!.indexOf(MusicContainer.currentMusic!!) > -1)
            playerbar.binding.playerbarFav.setImageResource(R.drawable.ic_baseline_favorite_24)
        else playerbar.binding.playerbarFav.setImageResource(R.drawable.ic_baseline_favorite_border_24)
    }
}