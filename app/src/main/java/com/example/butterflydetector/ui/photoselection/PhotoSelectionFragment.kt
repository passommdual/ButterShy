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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.butterflydetector.R
import com.example.butterflydetector.databinding.FragmentPhotoselectionBinding
import com.example.butterflydetector.ui.home.HomeViewModel
import com.example.butterflydetector.ui.base.BaseFragment
import com.example.butterflydetector.utils.ColorModeManager

class PhotoSelectionFragment : BaseFragment() {

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

        // <CHANGE> Initialize ViewModels with proper error handling
        try {
            homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
            photoSelectionViewModel = ViewModelProvider(this)[PhotoSelectionViewModel::class.java]
        } catch (e: Exception) {
            Log.e("PhotoSelectionFragment", "Error initializing ViewModels", e)
            Toast.makeText(requireContext(), "Error initializing photo selection", Toast.LENGTH_SHORT).show()
        }

        setupRecyclerView()
        setupClickListeners()
        observeData()

        return root
    }

    override fun applyColorMode(view: View) {
        super.applyColorMode(view)

        view.setBackgroundColor(getBookPages())

        binding.clearSelectionBtn.setBackgroundColor(getLogoGreen())
        binding.sendToAiBtn.setBackgroundColor(getLogoDarkGreen())

        if (::photoAdapter.isInitialized) {
            photoAdapter.updateColorMode(ColorModeManager.isColorblindMode(requireContext()))
        }
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoAdapter(
            photos = emptyList(),
            onPhotoClick = { position, bitmap -> showPhotoZoom(bitmap) },
            onPhotoSelectionChanged = { position, isSelected ->
                photoSelectionViewModel.togglePhotoSelection(position)
            },
            isColorblindMode = ColorModeManager.isColorblindMode(requireContext())
        )

        binding.photosRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = photoAdapter
        }
    }

    private fun setupClickListeners() {
        binding.clearSelectionBtn.setOnClickListener {
            try {
                val selectedIndices = photoSelectionViewModel.selectedPhotos.value ?: emptySet()
                if (selectedIndices.isNotEmpty()) {

                    photoSelectionViewModel.clearSelection()
                    Toast.makeText(requireContext(), "Selection cleared", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No photos selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PhotoSelectionFragment", "Error clearing selection", e)
                Toast.makeText(requireContext(), "Error clearing selection", Toast.LENGTH_SHORT).show()
            }
        }


        binding.sendToAiBtn.setOnClickListener {
            try {
                val photos = homeViewModel.capturedPhotos
                if (photos.isNotEmpty()) {
                    photoSelectionViewModel.sendSelectedPhotosToDatabase(photos)
                } else {
                    Toast.makeText(requireContext(), "No photos to send", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PhotoSelectionFragment", "Error sending photos to AI", e)
                Toast.makeText(requireContext(), "Error sending photos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeData() {
        homeViewModel.photoCount.observe(viewLifecycleOwner) { count ->
            Log.d("PhotoSelectionFragment", "[v0] Photo count: $count")
            val photos = homeViewModel.capturedPhotos
            Log.d("PhotoSelectionFragment", "[v0] Actual photos list size: ${photos.size}")

            photoAdapter.updatePhotos(photos)

            binding.textGallery.text = if (count > 0) {
                "Recently captured photos ($count)"
            } else {
                "No photos captured yet. Press the camera button to start taking photos."
            }
        }

        photoSelectionViewModel.selectedPhotos.observe(viewLifecycleOwner) { selectedIndices ->
            val count = selectedIndices.size
            binding.selectionCount.text = "$count photos selected"
            binding.sendToAiBtn.isEnabled = count > 0
            photoAdapter.updateSelection(selectedIndices)
        }

        photoSelectionViewModel.isProcessing.observe(viewLifecycleOwner) { isProcessing ->
            val hasSelection = photoSelectionViewModel.selectedPhotos.value?.isNotEmpty() == true
            binding.sendToAiBtn.isEnabled = !isProcessing && hasSelection
            binding.sendToAiBtn.text = if (isProcessing) "Processing..." else "Send to AI Identification"
        }

        photoSelectionViewModel.processingMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPhotoZoom(bitmap: Bitmap) {
        try {
            val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_photo_zoom, null)

            val zoomedPhoto = dialogView.findViewById<ImageView>(R.id.zoomed_photo)
            val closeBtn = dialogView.findViewById<View>(R.id.close_zoom_btn)

            zoomedPhoto.setImageBitmap(bitmap)
            closeBtn.setOnClickListener { dialog.dismiss() }

            dialog.setContentView(dialogView)
            dialog.show()
        } catch (e: Exception) {
            Log.e("PhotoSelectionFragment", "Error showing photo zoom", e)
            Toast.makeText(requireContext(), "Error displaying photo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class PhotoAdapter(
    private var photos: List<Bitmap>,
    private val onPhotoClick: (Int, Bitmap) -> Unit,
    private val onPhotoSelectionChanged: (Int, Boolean) -> Unit,
    private var isColorblindMode: Boolean = false
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

        if (position >= photos.size) {
            Log.e("PhotoAdapter", "Invalid position $position for photos size ${photos.size}")
            return
        }

        val bitmap = photos[position]
        val isSelected = selectedPhotos.contains(position)

        holder.imageView.setImageBitmap(bitmap)

        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = isSelected

        if (isSelected) {
            holder.selectionOverlay.visibility = View.VISIBLE
            val overlayColor = if (isColorblindMode) {
                0x440000FF // Semi-transparent blue
            } else {
                0x4400FF00 // Semi-transparent green
            }
            holder.selectionOverlay.setBackgroundColor(overlayColor)
        } else {
            holder.selectionOverlay.visibility = View.GONE
        }

        holder.imageView.setOnClickListener {
            onPhotoClick(position, bitmap)
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            Log.d("PhotoAdapter", "[v0] Checkbox clicked at position $position, isChecked: $isChecked")
            onPhotoSelectionChanged(position, isChecked)
        }

        holder.itemView.setOnClickListener {
            Log.d("PhotoAdapter", "[v0] Item clicked at position $position")
            holder.checkbox.isChecked = !holder.checkbox.isChecked
        }
    }

    override fun getItemCount() = photos.size

    fun updatePhotos(newPhotos: List<Bitmap>) {
        Log.d("PhotoAdapter", "[v0] Updating with ${newPhotos.size} photos")
        photos = newPhotos
        notifyDataSetChanged()
    }

    fun updateSelection(newSelection: Set<Int>) {
        selectedPhotos = newSelection
        notifyDataSetChanged()
    }

    fun updateColorMode(colorblindMode: Boolean) {
        isColorblindMode = colorblindMode
        notifyDataSetChanged()
    }
}