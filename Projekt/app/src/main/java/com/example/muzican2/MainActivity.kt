package com.example.muzican2

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.muzican2.databinding.ActivityMainBinding
import com.example.muzican2.databinding.TimerPopupBinding
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var playerbarPlayButtonState : MutableLiveData<Int>
    }
    private lateinit var mainbinding: ActivityMainBinding  // defining the binding class
    private var doubleBackToExitPressedOnce = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestRuntimePermission()

        initializeLayout()

        Glide.with(this).load(R.drawable.placeholder)
            .into(mainbinding.mainPlayerbar.playerbarPic)

        playerbarPlayButtonState = PlayerbarPlayButtonState.playerbarPlayButtonState
        playerbarPlayButtonState.observe(this) {
            if (MusicAdapter.mediaPlayer!!.isPlaying) {
                mainbinding.mainPlayerbar.playerbarPause.setImageResource(R.drawable.ic_baseline_pause_24)
            }
            else mainbinding.mainPlayerbar.playerbarPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }

        mainbinding.mainPlayerbar.playerbarShuffle.setOnClickListener {
            if (MusicContainer.isShuffled == false) {
                MusicContainer.isShuffled = true

                MusicContainer.musicList!!.remove(MusicContainer.currentMusic)
                MusicContainer.musicList!!.shuffle()
                MusicContainer.musicList!!.add(MusicContainer.currentMusicPosition, MusicContainer.currentMusic!!)
                //MusicContainer.currentMusicPosition = 0

                mainbinding.mainPlayerbar.playerbarShuffle.setImageResource(R.drawable.ic_baseline_shuffle_on_24)
            }
            else {
                MusicContainer.isShuffled = false

                for (it in MusicContainer.musicList!!) {
                    Log.i("a", it.title)
                }

                // remember about creating new arraylist so that the shuffle doesnt affect both the lists somehow.........
                MusicContainer.musicList = ArrayList(MusicContainer.fullMusicList)
                MusicContainer.currentMusicPosition = MusicContainer.fullMusicList!!.indexOf(MusicContainer.currentMusic!!)

                mainbinding.mainPlayerbar.playerbarShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24)
            }
        }
    }

    private fun initializeLayout() {
        mainbinding = ActivityMainBinding.inflate(layoutInflater)  // initializing the binding class
        setContentView(mainbinding.root)  // we now set the contentview as the mainbinding.root

        // get all music from device
        MusicContainer.fullMusicList = getAllAudio()
        MusicContainer.musicList = ArrayList(MusicContainer.fullMusicList)

        MusicContainer.currentMusic = MusicContainer.musicList!![0]

        // populate recyclerview
        mainbinding.musicrecyclerview.setHasFixedSize(true)
        mainbinding.musicrecyclerview.setItemViewCacheSize(100)
        mainbinding.musicrecyclerview.layoutManager = LinearLayoutManager(this@MainActivity)

        // create adapter and pass binding to playerbar
        val playerBarContents = PlayerbarContents(mainbinding.mainPlayerbar)

        val musicAdapter = MusicAdapter(this@MainActivity, MusicContainer.musicList!!, playerBarContents, this)
        mainbinding.musicrecyclerview.adapter = musicAdapter

        val seekBar = mainbinding.mainPlayerbar.playerbarSeekBar

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val inputSeconds = seekBar.progress * 1000
                if (seekBar.max == inputSeconds)
                    MusicAdapter.mediaPlayer!!.seekTo(MusicAdapter.mediaPlayer!!.duration-1)
                else
                    MusicAdapter.mediaPlayer!!.seekTo(inputSeconds)

                Log.i("new music position: ", seekBar.progress.toString())
            }

        })

        //val toolbarSearchTxtField = mainbinding.toolbar.searchTextField

        mainbinding.toolbar.toolbarSearch.setOnClickListener {
            // open search window in new activity
            ContextCompat.startActivity(this, Intent(this, SearchActivity::class.java), null)
        }

        mainbinding.toolbar.toolbarPlaylists.setOnClickListener {
            // open search window in new activity
            ContextCompat.startActivity(this, Intent(this, PlaylistActivity::class.java), null)
        }

        mainbinding.toolbar.toolbarOptions.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            val timerDialog = Dialog(this)
            timerDialog.window!!.setLayout(300, 200);

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

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()

            MusicAdapter.mediaPlayer?.pause();
            MusicAdapter.mediaPlayer?.release();
            MusicAdapter.mediaPlayer = null;

            MusicContainer.closeSingleton();

            finishAffinity()

            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    @SuppressLint("Range")
    private fun getAllAudio(): ArrayList<Music> {
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA
        )
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC", null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val music = Music(
                        id = idC, title = titleC, album = albumC, artist = artistC,
                        path = pathC, duration = durationC.toInt() / 1000
                    )
                    val file = File(music.path)
                    if (file.exists()) {
                        Log.i("List", music.title)
                        tempList.add(music)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return ((tempList.sortedBy { it.title }).distinctBy { it.title }).toCollection(ArrayList())
    }

    private fun requestRuntimePermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            else
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
        }
    }
}


