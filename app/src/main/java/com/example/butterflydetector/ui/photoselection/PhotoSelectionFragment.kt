package com.example.butterflydetector.ui.photoselection

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.butterflydetector.R
import com.example.butterflydetector.data.PhotoDatabase
import com.example.butterflydetector.databinding.FragmentPhotoselectionBinding
import com.example.butterflydetector.ui.home.HomeViewModel
import kotlinx.coroutines.launch

class PhotoSelectionFragment : Fragment() {

    private var _binding: FragmentPhotoselectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var photoSelectionViewModel: PhotoSelectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoselectionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        photoSelectionViewModel = ViewModelProvider(this)[PhotoSelectionViewModel::class.java]

        setupRecyclerView()
        setupClickListeners()
        observeData()

        return root
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoAdapter(
            photos = emptyList(),
            onPhotoClick = { position, bitmap -> showPhotoZoom(bitmap) },
            onPhotoSelectionChanged = { position, isSelected ->
                photoSelectionViewModel.togglePhotoSelection(position)
            }
        )
        binding.photosRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = photoAdapter
        }
    }

    private fun setupClickListeners() {
        binding.clearSelectionBtn.setOnClickListener {
            val selectedIndices = photoSelectionViewModel.selectedPhotos.value ?: emptySet()
            homeViewModel.removePhotosAt(selectedIndices)    // nur ausgewählte löschen
            photoSelectionViewModel.clearSelection()         // Auswahl zurücksetzen
            photoAdapter.updatePhotos(homeViewModel.capturedPhotos) // Adapter aktualisieren
        }



        binding.sendToAiBtn.setOnClickListener {
            val photos = homeViewModel.capturedPhotos
            photoSelectionViewModel.sendSelectedPhotosToDatabase(photos)
        }
    }

    private fun observeData() {
        // Observe photos from HomeViewModel
        homeViewModel.photoCount.observe(viewLifecycleOwner) { count ->
            Log.d("PhotoSelectionFragment", "[v0] PhotoSelection observing photo count: $count")
            val photos = homeViewModel.capturedPhotos
            Log.d("PhotoSelectionFragment", "[v0] Actual photos list size: ${photos.size}")

            photoAdapter.updatePhotos(photos)

            binding.textGallery.text = if (count > 0) {
                "Recently captured photos ($count)"
            } else {
                "No photos captured yet. Press the camera button to start taking photos."
            }
        }

        // Observe photo selection
        photoSelectionViewModel.selectedPhotos.observe(viewLifecycleOwner) { selectedIndices ->
            val count = selectedIndices.size
            binding.selectionCount.text = "$count photos selected"
            binding.sendToAiBtn.isEnabled = count > 0
            photoAdapter.updateSelection(selectedIndices)
        }

        // Observe processing status
        photoSelectionViewModel.isProcessing.observe(viewLifecycleOwner) { isProcessing ->
            binding.sendToAiBtn.isEnabled = !isProcessing && (photoSelectionViewModel.selectedPhotos.value?.isNotEmpty() == true)
            binding.sendToAiBtn.text = if (isProcessing) "Processing..." else "Send to AI Identification"
        }

        photoSelectionViewModel.processingMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPhotoZoom(bitmap: Bitmap) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_photo_zoom, null)

        val zoomedPhoto = dialogView.findViewById<ImageView>(R.id.zoomed_photo)
        val closeBtn = dialogView.findViewById<View>(R.id.close_zoom_btn)

        zoomedPhoto.setImageBitmap(bitmap)
        closeBtn.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(dialogView)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class PhotoAdapter(
    private var photos: List<Bitmap>,
    private val onPhotoClick: (Int, Bitmap) -> Unit,
    private val onPhotoSelectionChanged: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    private var selectedPhotos: Set<Int> = emptySet()

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.photo_image)
        val checkbox: CheckBox = view.findViewById(R.id.photo_checkbox)
        val selectionOverlay: View = view.findViewById(R.id.selection_overlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        Log.d("PhotoAdapter", "[v0] Binding photo at position $position")
        val bitmap = photos[position]
        val isSelected = selectedPhotos.contains(position)

        holder.imageView.setImageBitmap(bitmap)

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = isSelected
        holder.selectionOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE

        // Click on image to zoom
        holder.imageView.setOnClickListener {
            onPhotoClick(position, bitmap)
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            Log.d("PhotoAdapter", "[v0] Checkbox clicked at position $position, isChecked: $isChecked")
            onPhotoSelectionChanged(position, isChecked)
        }

        // Click on entire item to toggle selection
        holder.itemView.setOnClickListener {
            Log.d("PhotoAdapter", "[v0] Item clicked at position $position")
            holder.checkbox.isChecked = !holder.checkbox.isChecked
        }
    }

    override fun getItemCount() = photos.size

    fun updatePhotos(newPhotos: List<Bitmap>) {
        Log.d("PhotoAdapter", "[v0] PhotoAdapter updating with ${newPhotos.size} photos")
        photos = newPhotos
        notifyDataSetChanged()
    }

    fun updateSelection(newSelection: Set<Int>) {
        selectedPhotos = newSelection
        notifyDataSetChanged()
    }
}
