package com.example.butterflydetector.ui.speciescatalog

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.butterflydetector.R
import com.example.butterflydetector.databinding.FragmentSpeciescatalogBinding
import com.example.butterflydetector.data.ButterflyEntity
import com.example.butterflydetector.ui.base.BaseFragment
import com.google.android.material.textfield.TextInputLayout

class SpeciesCatalogFragment : BaseFragment() {

    private var _binding: FragmentSpeciescatalogBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SpeciesCatalogViewModel
    private lateinit var adapter: ButterflyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeciescatalogBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[SpeciesCatalogViewModel::class.java]

        setupRecyclerView()
        setupFilters()
        observeViewModel()

        Log.d("ButterflyAdapter", "SpeciesCatalogFragment created")
        return binding.root
    }

    override fun applyColorMode(view: View) {
        super.applyColorMode(view)

        // Apply background color to main layout
        val mainLayout = view.findViewById<LinearLayout>(R.id.species_catalog_main_layout)
        mainLayout?.setBackgroundColor(getBookPages())

        val searchFilterLayout = view.findViewById<TextInputLayout>(R.id.search_filter_layout)
        searchFilterLayout?.setBackgroundColor(getLogoGreen())

        val speciesFilterLayout = view.findViewById<TextInputLayout>(R.id.species_filter_layout)
        speciesFilterLayout?.setBackgroundColor(getLogoGreen())

        val logoDarkGreen = getLogoDarkGreen()
        val boxStrokeColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_hovered),
                intArrayOf()
            ),
            intArrayOf(
                logoDarkGreen, // focused state
                logoDarkGreen, // hovered state
                logoDarkGreen  // default state
            )
        )
        searchFilterLayout?.setBoxStrokeColorStateList(boxStrokeColorStateList)
        speciesFilterLayout?.setBoxStrokeColorStateList(boxStrokeColorStateList)

        val logoGreen = getLogoGreen()

        // Chip color state list for checked/unchecked states
        val chipBackgroundColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                logoDarkGreen, // checked state
                logoGreen      // unchecked state
            )
        )

        val chipStrokeColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                logoDarkGreen, // checked state
                logoDarkGreen  // unchecked state
            )
        )

        binding.favoritesChip.chipBackgroundColor = chipBackgroundColorStateList
        binding.favoritesChip.chipStrokeColor = chipStrokeColorStateList
        binding.favoritesChip.chipStrokeWidth = 2f

        binding.clearFilterChip.chipBackgroundColor = ColorStateList.valueOf(logoGreen)
        binding.clearFilterChip.chipStrokeColor = ColorStateList.valueOf(logoDarkGreen)
        binding.clearFilterChip.chipStrokeWidth = 2f
    }

    private fun setupRecyclerView() {
        adapter = ButterflyAdapter(
            context = requireContext(),
            onInfoClick = { butterfly -> showButterflyInfo(butterfly) },
            onFavoriteClick = { butterfly -> viewModel.toggleFavorite(butterfly) }
        )

        binding.butterflyRecyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.butterflyRecyclerView.adapter = adapter
    }

    private fun setupFilters() {
        val searchInput = binding.searchFilterInput
        val dropdownInput = binding.speciesFilterDropdown

        // Setup dropdown with species list
        viewModel.speciesList.observe(viewLifecycleOwner) { speciesList ->
            val allOptions = mutableListOf("All Species").apply { addAll(speciesList) }
            val arrayAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                allOptions
            )
            dropdownInput.setAdapter(arrayAdapter)
        }

        // Real-time text search as user types
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                applyFilters()
            }
        })

        // Handle dropdown selection
        dropdownInput.setOnItemClickListener { _, _, position, _ ->
            applyFilters()
        }

        // Favorites filter chip
        binding.favoritesChip.setOnCheckedChangeListener { _, _ ->
            applyFilters()
        }

        // Clear all filters
        binding.clearFilterChip.setOnClickListener {
            searchInput.setText("")
            dropdownInput.setText("All Species", false)
            binding.favoritesChip.isChecked = false
            applyFilters()
        }
    }

    private fun applyFilters() {
        val searchQuery = binding.searchFilterInput.text?.toString()?.trim() ?: ""
        val selectedSpecies = binding.speciesFilterDropdown.text?.toString()?.trim() ?: "All Species"
        val favoritesOnly = binding.favoritesChip.isChecked

        // Combine filters: if dropdown is not "All Species", use it; otherwise use search query
        val finalQuery = if (selectedSpecies != "All Species") {
            selectedSpecies
        } else {
            searchQuery
        }

        viewModel.searchAndFilter(finalQuery, favoritesOnly)
    }

    private fun observeViewModel() {
        viewModel.filteredButterflies.observe(viewLifecycleOwner) { butterflies ->
            adapter.submitList(butterflies)
        }
    }

    private fun showButterflyInfo(butterfly: ButterflyEntity) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(butterfly.name)
            .setMessage("""
                Species: ${butterfly.species}
                
                Description: ${butterfly.description}
                
                Habitat: ${butterfly.habitat}
                
                Wingspan: ${butterfly.wingspan}
                
                Flight Period: ${butterfly.flightPeriod}
            """.trimIndent())
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()

        // Apply custom color to dialog button after showing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getLogoDarkGreen())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
