package com.example.muzican2

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzican2.databinding.ActivityDetailedPlayerBinding
import com.example.muzican2.databinding.ActivitySearchBinding

private lateinit var searchbinding: ActivitySearchBinding  // defining the binding class

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setting layout
        searchbinding = ActivitySearchBinding.inflate(layoutInflater)

        setContentView(searchbinding.root)

        // populate recyclerview
        searchbinding.searchRecyclerview.setHasFixedSize(true)
        searchbinding.searchRecyclerview.setItemViewCacheSize(13)
        searchbinding.searchRecyclerview.layoutManager = LinearLayoutManager(this@SearchActivity)

        val searchAdapter = SearchAdapter(this@SearchActivity, MusicContainer.fullMusicList!!)
        searchbinding.searchRecyclerview.adapter = searchAdapter

        searchFieldListener()

    }

    private fun searchFieldListener() {


        searchbinding.searchTextField.editText!!.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val foundMusicList = searchMusic(s.toString())
                val searchAdapter = SearchAdapter(this@SearchActivity, foundMusicList)
                searchbinding.searchRecyclerview.adapter = searchAdapter
                }
            })
    }

    private fun searchMusic(text: String): ArrayList<Music> {
        var matchesList = ArrayList<Music>()
        for (music in MusicContainer.musicList!!) {
            if (music.title.contains(text, ignoreCase = true)) matchesList.add(music)
        }
        return matchesList
    }
}