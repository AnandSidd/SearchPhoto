package com.example.searchphoto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_grid_item.view.*

class StaggeredRecyclerViewAdapter(var listFlickrPhoto: ArrayList<FlickrPhoto>) :
    RecyclerView.Adapter<StaggeredRecyclerViewAdapter.ViewHolder>() {

    fun updatePhoto(newPhoto: List<FlickrPhoto>, pageNo: Int, issearched: Boolean) {
        if (pageNo == 1)
            listFlickrPhoto.clear()
        val size = listFlickrPhoto.size
        if(issearched == false){
            listFlickrPhoto.addAll(newPhoto)
            notifyItemRangeInserted(size, newPhoto.size)
        }else{
            listFlickrPhoto.clear()
            listFlickrPhoto.addAll(newPhoto)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.layout_grid_item, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listFlickrPhoto[position])
    }

    override fun getItemCount() = listFlickrPhoto.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imageView = view.image

        private val progressDrawable = getProgressDrawable(view.context)

        fun bind(flickrPhoto: FlickrPhoto) {
            imageView.loadImage(flickrPhoto.url, progressDrawable)
        }
    }
}