package com.example.muzican2

class MusicContainer {
    companion object {
        var fullMusicList: ArrayList<Music>? = ArrayList()
        var favMusicList: ArrayList<Music>? = ArrayList()
        var musicList: ArrayList<Music>? = ArrayList()

        var isShuffled = false

        // currently played music
        var currentMusic: Music? = null
        var currentMusicPosition = 0
        var currentMusicDuration = 0
        var currentMusicTime = 0

        init {
            //musicList!!.add(Music("id","title","album","artist",0,""))
        }

        fun closeSingleton() {
            isShuffled = false

            // currently played music
            currentMusic = null
            currentMusicPosition = 0
            currentMusicDuration = 0
            currentMusicTime = 0
        }
    }
}