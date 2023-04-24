package com.example.muzican2

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.muzican2.databinding.MusicListItemBinding

class MusicAdapter(private val context: Context, private val musicList: ArrayList<Music>, private val playerbar: PlayerbarContents, private val lifecycle: LifecycleOwner
                   ): RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    companion object {
        var mediaPlayer: MediaPlayer? = null
        lateinit var playedSearchedListener: MutableLiveData<Int>
        lateinit var changeSongFromDetailedPlayer: MutableLiveData<Int>
        lateinit var updateFavBtn: MutableLiveData<Int>

    }

    val seekBar = playerbar.binding.playerbarSeekBar

    class MusicViewHolder(binding: MusicListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val musicListItemBinding = binding

        val musicImage = binding.musicitemImage
        val musicTitle = binding.musicitemText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(MusicListItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        // current list item
        val currentItem = musicList[position]

        // set each song title in viewholders
        holder.musicTitle.text = currentItem.title

        // load song images
        // ----------------
        val retriever = MediaMetadataRetriever()

        // set source of music file cover image
        retriever.setDataSource(currentItem.path)

        Glide.with(context).load(retriever.embeddedPicture)
            .apply(RequestOptions().placeholder(R.drawable.placeholder).centerCrop())
            .into(holder.musicImage)
        // ----------------

        // after clicking any viewholder from recyclerview
        holder.musicListItemBinding.musicitem.setOnClickListener {
            // send clicked item (currentItem) to check if its different from the currently played song (MusicContainer.currentMusic)
            MediaPlayerFunctions(context, seekBar, playerbar).playAudio(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    init {
        // put listeners on playerbar icons
        playerbar.binding.playerbarPause.setOnClickListener {
            if (mediaPlayer == null) MediaPlayerFunctions(context, seekBar, playerbar).playAudio(MusicContainer.currentMusic!!)
            else MediaPlayerFunctions(context, seekBar, playerbar).pauseAudio()
        }

        playerbar.binding.playerbarNext.setOnClickListener {
            SongChanger(context, seekBar, playerbar).nextAudio()
        }

        playerbar.binding.playerbarFav.setOnClickListener {
            if (MusicContainer.favMusicList!!.indexOf(MusicContainer.currentMusic!!) < 0) {
                MusicContainer.favMusicList!!.add(MusicContainer.currentMusic!!)
                playerbar.binding.playerbarFav.setImageResource(R.drawable.ic_baseline_favorite_24)
            }

            else {
                MusicContainer.favMusicList!!.remove(MusicContainer.currentMusic!!)
                playerbar.binding.playerbarFav.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }
        }

        // open detailed player after clicking playerbar
        playerbar.binding.playerbar.setOnClickListener {

            // open detailed player in new activity
            if (mediaPlayer != null) {
                ContextCompat.startActivity(context, Intent(context, DetailedPlayerActivity::class.java), null)
                DetailedPlayerActivity.mediaPlayer = mediaPlayer
            }
        }

        // listen if user opened search window and played music from there -> update playerbar
        playedSearchedListener = PlayedSearchedListener.playedSearchedListen
        playedSearchedListener.observeForever {
            MediaPlayerFunctions(context, seekBar, playerbar).playAudio(MusicContainer.currentMusic!!)
        }

        // listen if user clicked next song button in detailed player, then play next song
        changeSongFromDetailedPlayer = ChangeMusicFromDetailedPlayer.changeSongFromDetailedPlayer
        changeSongFromDetailedPlayer.observeForever {
            SongChanger(context, seekBar, playerbar).nextAudio()
        }

        // update fav button if it was clicked in detailed player
        updateFavBtn = UpdateFavBtn.updateFavBtn
        updateFavBtn.observeForever {
            // on song change check if its on fav list and change fav icon accordingly
            if (MusicContainer.favMusicList!!.indexOf(MusicContainer.currentMusic!!) > -1)
                playerbar.binding.playerbarFav.setImageResource(R.drawable.ic_baseline_favorite_24)
            else playerbar.binding.playerbarFav.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }
    }
}