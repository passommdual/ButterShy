package com.example.butterflydetector.ui.speciescatalog

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.butterflydetector.databinding.FragmentSpeciescatalogBinding
import com.example.butterflydetector.data.ButterflyEntity

class SpeciesCatalogFragment : Fragment() {

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
        setupSpeciesFilter()
        observeViewModel()

        Log.d("ButterflyAdapter", "SpeciesCatalogFragment created")
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = ButterflyAdapter(
            onInfoClick = { butterfly -> showButterflyInfo(butterfly) },
            onFavoriteClick = { butterfly -> viewModel.toggleFavorite(butterfly) }
        )

        binding.butterflyRecyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.butterflyRecyclerView.adapter = adapter
    }

    private fun setupSpeciesFilter() {
        val selectedItems = mutableSetOf<String>()
        binding.speciesFilterDropdown.setText("Select species...", false)

        binding.speciesFilterDropdown.setOnClickListener {
            viewModel.speciesList.value?.let { speciesList ->
                val listWithFavorites = speciesList.toMutableList().apply { add("Favorites") }
                val checkedItems = listWithFavorites.map { it in selectedItems }.toBooleanArray()

                AlertDialog.Builder(requireContext())
                    .setTitle("Select species")
                    .setMultiChoiceItems(listWithFavorites.toTypedArray(), checkedItems) { _, which, isChecked ->
                        val selected = listWithFavorites[which]
                        if (isChecked) selectedItems.add(selected) else selectedItems.remove(selected)
                    }
                    .setPositiveButton("Apply") { _, _ ->
                        val onlyFavorites = "Favorites" in selectedItems
                        val selectedSpecies = selectedItems.filter { it != "Favorites" }
                        viewModel.filterButterflies(selectedSpecies, onlyFavorites)

                        binding.speciesFilterDropdown.setText(
                            if (selectedItems.isEmpty()) "Select species..." else selectedItems.joinToString(", "),
                            false
                        )
                    }
                    .setNegativeButton("Cancel", null)
                    .setNeutralButton("Reset") { _, _ ->
                        selectedItems.clear()
                        viewModel.filterButterflies(emptyList(), false)
                        binding.speciesFilterDropdown.setText("Select species...", false)
                    }
                    .show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.filteredButterflies.observe(viewLifecycleOwner) { butterflies ->
            adapter.submitList(butterflies)
        }
    }

    private fun showButterflyInfo(butterfly: ButterflyEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle(butterfly.name)
            .setMessage("""
                Species: ${butterfly.species}
                
                Description: ${butterfly.description}
                
                Habitat: ${butterfly.habitat}
                
                Wingspan: ${butterfly.wingspan}
                
                Flight Period: ${butterfly.flightPeriod}
            """.trimIndent())
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
