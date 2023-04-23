package com.example.muzican2

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.muzican2.databinding.MusicListItemBinding

public class SearchAdapter(private val context: Context, private val musicList: ArrayList<Music>
//, private val playerbar: PlayerbarContents
                           ): RecyclerView.Adapter<SearchAdapter.MusicViewHolder>() {

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
            //MediaPlayerFunctions(context, playerbar.binding.playerbarSeekBar, playerbar).playAudio(currentItem)
            MusicContainer.currentMusic = currentItem
            MusicContainer.currentMusicPosition = MusicContainer.fullMusicList!!.indexOf(currentItem)

            if (MusicAdapter.mediaPlayer != null) {
                MusicAdapter.mediaPlayer!!.release()
                MusicAdapter.mediaPlayer = null
            }

            Log.i("h","a")
            PlayedSearchedListener.playedSearchedListen.value = 1
            Log.i("h","b")
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }
}