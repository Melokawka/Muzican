package com.example.muzican2

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.muzican2.databinding.ActivityDetailedPlayerBinding
import com.example.muzican2.databinding.TimerPopupBinding
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds

class DetailedPlayerActivity : AppCompatActivity() {
    companion object {
        var mediaPlayer: MediaPlayer? = null
        lateinit var musicChangeListen : MutableLiveData<Int>
        var detailedPlayerPlayButtonState = DetailedPlayerPlayButtonState.detailedPlayerPlayButtonState
    }

    // image retriever
    private val retriever = MediaMetadataRetriever()

    private lateinit var mainSeekBar : SeekBar
    private lateinit var seekBarFunctions : SeekBarFunctions2

    private lateinit var detailedplayerbinding: ActivityDetailedPlayerBinding  // defining the binding class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setting layout
        detailedplayerbinding = ActivityDetailedPlayerBinding.inflate(layoutInflater)

        setContentView(detailedplayerbinding.root)

        if (MusicContainer.currentMusic == null) {
            MusicContainer.currentMusic = MusicContainer.musicList!![0]
            MusicContainer.currentMusicPosition = 0
        }

        mainSeekBar = detailedplayerbinding.mainSeekBar
        seekBarFunctions = SeekBarFunctions2(mainSeekBar)

        // reading initial song and updating playbar
        updateLayout(MusicContainer.currentMusic!!.path)
        listeners()
        listenersOfSeekBar()

        // first time the detailed player is opened or the song is changed
        MusicChange()
        musicChangeListen = MusicChange.musicChangeListen
        musicChangeListen.observe(this
        ) {
            mediaPlayer = MusicAdapter.mediaPlayer

            updateLayout(MusicContainer.currentMusic!!.path)

            // on song change check if its on fav list and change fav icon accordingly
            if (MusicContainer.favMusicList!!.indexOf(MusicContainer.currentMusic!!) > -1) {
                detailedplayerbinding.favBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
            }

            else {
                detailedplayerbinding.favBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }

            seekBarFunctions.prepareSeekBar()

            handleSeekbar()
        }

        popupMenuListener()

        MusicAdapter.playedSearchedListener = PlayedSearchedListener.playedSearchedListen
        MusicAdapter.playedSearchedListener.observe(this) {
            updateLayout(MusicContainer.currentMusic!!.path)
        }

        detailedPlayerPlayButtonState.observe(this) {
            if (MusicAdapter.mediaPlayer!!.isPlaying) {
                detailedplayerbinding.pauseButton.setImageResource(R.drawable.ic_baseline_pause_24)
            }
            else detailedplayerbinding.pauseButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }

        detailedplayerbinding.toolbar2.toolbarPlaylists.setOnClickListener {
            // open search window in new activity
            ContextCompat.startActivity(this, Intent(this, PlaylistActivity::class.java), null)
        }

        detailedplayerbinding.shuffleBtn.setOnClickListener {
            if (MusicContainer.isShuffled == false) {
                MusicContainer.isShuffled = true

                MusicContainer.musicList!!.remove(MusicContainer.currentMusic)
                MusicContainer.musicList!!.shuffle()
                MusicContainer.musicList!!.add(MusicContainer.currentMusicPosition, MusicContainer.currentMusic!!)

                detailedplayerbinding.shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_on_24)
            }
            else {
                MusicContainer.isShuffled = false

                for (it in MusicContainer.musicList!!) {
                    Log.i("a", it.title)
                }

                // remember about creating new arraylist so that the shuffle doesnt affect both the lists somehow.........
                MusicContainer.musicList = ArrayList(MusicContainer.fullMusicList)
                MusicContainer.currentMusicPosition = MusicContainer.fullMusicList!!.indexOf(MusicContainer.currentMusic!!)

                detailedplayerbinding.shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_24)
            }
        }
    }

    private fun popupMenuListener() {
        detailedplayerbinding.toolbar2.toolbarSearch.setOnClickListener {
            // open search window in new activity
            ContextCompat.startActivity(this, Intent(this, SearchActivity::class.java), null)
        }

        detailedplayerbinding.toolbar2.toolbarOptions.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            val timerDialog = Dialog(this)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_timer -> {
                        val timerPopupBinding: TimerPopupBinding =
                            TimerPopupBinding.inflate(LayoutInflater.from(this))

                        timerDialog.setContentView(timerPopupBinding.root)
                        timerDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                        val np1 = timerDialog.findViewById<NumberPicker>(R.id.picker_minutes)
                        val nums = arrayOfNulls<String>(61)

                        for (i in nums.indices) nums[i] = i.toString()

                        np1.minValue = 0
                        np1.maxValue = 60
                        np1.wrapSelectorWheel = true
                        np1.displayedValues = nums
                        np1.value = 0

                        val np2 = timerDialog.findViewById<NumberPicker>(R.id.picker_seconds)
                        val nums2 = arrayOfNulls<String>(61)

                        for (i in nums.indices) nums2[i] = i.toString()

                        np2.minValue = 0
                        np2.maxValue = 60
                        np2.wrapSelectorWheel = true
                        np2.displayedValues = nums2
                        np2.value = 0

                        val timer_btn = timerPopupBinding.timerBtn

                        timer_btn.setOnClickListener {
                            var minutes = np1.value
                            var seconds = np2.value

                            seconds += minutes*60
                            Log.i("haha", seconds.toString())

                            Handler().postDelayed( {
                                if (MusicAdapter.mediaPlayer != null && MusicAdapter.mediaPlayer!!.isPlaying) {
                                    MusicAdapter.mediaPlayer!!.pause()
                                    detailedPlayerPlayButtonState.value = 1
                                }
                            }, seconds.toLong()*1000)
                            timerDialog.dismiss()
                        }
                        timerDialog.show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.menu_toolbar)
            popupMenu.show()
        }
    }

    // set listeners for pause and next buttons
    private fun listeners() {
        // detailedplayer buttons
        detailedplayerbinding.pauseButton.setOnClickListener {
            if  (MusicAdapter.mediaPlayer!!.isPlaying)
                MusicAdapter.mediaPlayer!!.pause()

            else MusicAdapter.mediaPlayer!!.start()

            detailedPlayerPlayButtonState.value = 1
        }

        detailedplayerbinding.nextButton.setOnClickListener {
            //mediaPlayer!!.seekTo(mediaPlayer!!.duration)
            ChangeMusicFromDetailedPlayer.changeSongFromDetailedPlayer.value = 1
        }

        detailedplayerbinding.favBtn.setOnClickListener {
            if (MusicContainer.favMusicList!!.indexOf(MusicContainer.currentMusic!!) < 0) {
                MusicContainer.favMusicList!!.add(MusicContainer.currentMusic!!)
                detailedplayerbinding.favBtn.setImageResource(R.drawable.ic_baseline_favorite_24)
                UpdateFavBtn.updateFavBtn.value = 1
            }

            else {
                MusicContainer.favMusicList!!.remove(MusicContainer.currentMusic!!)
                detailedplayerbinding.favBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                UpdateFavBtn.updateFavBtn.value = 1
            }
        }
    }

    private fun handleSeekbar() {
        // refreshing seekbar time
        val job = Job()

        refreshSeekBar(job)

        job.complete()
    }

    private fun listenersOfSeekBar() {
        // has to be in this class because of mediaplayer
        mainSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val inputSeconds = seekBar.progress * 1000
                if (inputSeconds == seekBar.max)
                    mediaPlayer!!.seekTo(mediaPlayer!!.duration)
                else
                    mediaPlayer!!.seekTo(inputSeconds)
            }
        })
    }

    private fun refreshSeekBar(job: Job) {
        val scope = CoroutineScope(job + Dispatchers.Main)

        val mediaPlayerTime = MusicAdapter.mediaPlayer!!.currentPosition

        val seconds = mediaPlayerTime.seconds.inWholeSeconds.toInt() / 1000

        // setting time
        seekBarFunctions.setSeekBarTime(seconds)

        // every .2s call this function again to set seekbar
            scope.launch {
                delay(200)
                if (mediaPlayer != null)
                    refreshSeekBar(job)
            }
    }

    private fun loadImage(path: String, imageview: ImageView) {
        retriever.setDataSource(path)

        Glide.with(this).load(retriever.embeddedPicture)
            .apply(RequestOptions().placeholder(R.drawable.placeholder).centerCrop())
            .into(imageview)
    }

    private fun updateLayout(path: String) {
        loadImage(path, detailedplayerbinding.mainImage)

        detailedplayerbinding.mainTitle.text = MusicContainer.currentMusic!!.title
    }
}