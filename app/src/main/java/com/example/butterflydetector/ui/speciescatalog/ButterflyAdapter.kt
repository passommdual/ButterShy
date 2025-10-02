package com.example.butterflydetector.ui.speciescatalog

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.butterflydetector.R
import com.example.butterflydetector.data.ButterflyEntity

class ButterflyAdapter(
    private val onInfoClick: (ButterflyEntity) -> Unit,
    private val onFavoriteClick: (ButterflyEntity) -> Unit
) : ListAdapter<ButterflyEntity, ButterflyAdapter.ButterflyViewHolder>(ButterflyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButterflyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_butterfly, parent, false)
        return ButterflyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ButterflyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ButterflyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.butterfly_image)
        private val infoButton: ImageButton = itemView.findViewById(R.id.info_button)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.favorite_button)

        fun bind(butterfly: ButterflyEntity) {
            val context = itemView.context
            val assetManager = context.assets
            val filename = butterfly.imageFile

            try {
                assetManager.open(filename).use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                    imageView.alpha = 1f
                }
            } catch (e: Exception) {
                // Fallback image if asset not found
                imageView.setImageResource(android.R.drawable.ic_menu_gallery)
                imageView.alpha = 0.5f
                Log.w(
                    "ButterflyAdapter",
                    "Could not load asset image: $filename. Available assets: ${assetManager.list("")?.joinToString(", ")}"
                )
            }

            // Set favorite icon
            favoriteButton.setImageResource(
                if (butterfly.isFavorite) android.R.drawable.star_big_on
                else android.R.drawable.star_big_off
            )

            // Click listeners
            infoButton.setOnClickListener { onInfoClick(butterfly) }
            favoriteButton.setOnClickListener { onFavoriteClick(butterfly) }
        }
    }

    class ButterflyDiffCallback : DiffUtil.ItemCallback<ButterflyEntity>() {
        override fun areItemsTheSame(oldItem: ButterflyEntity, newItem: ButterflyEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ButterflyEntity, newItem: ButterflyEntity): Boolean =
            oldItem == newItem
    }
}
