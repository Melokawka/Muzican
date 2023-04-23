package com.example.muzican2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzican2.databinding.ActivityPlaylistBinding

class PlaylistActivity : AppCompatActivity() {
    private lateinit var playlistbinding: ActivityPlaylistBinding  // defining the binding class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setting layout
        playlistbinding = ActivityPlaylistBinding.inflate(layoutInflater)

        setContentView(playlistbinding.root)

        // populate recyclerview
        playlistbinding.playlistsRecyclerview.setHasFixedSize(true)
        playlistbinding.playlistsRecyclerview.setItemViewCacheSize(13)
        playlistbinding.playlistsRecyclerview.layoutManager = LinearLayoutManager(this@PlaylistActivity)

        val playlistAdapter = PlaylistAdapter(this@PlaylistActivity, MusicContainer.favMusicList!!)
        playlistbinding.playlistsRecyclerview.adapter = playlistAdapter
    }
}