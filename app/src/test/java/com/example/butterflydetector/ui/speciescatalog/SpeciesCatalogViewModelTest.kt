package com.example.butterflydetector.ui.speciescatalog

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.butterflydetector.data.ButterflyEntity
import com.example.butterflydetector.data.ButterflyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SpeciesCatalogViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: SpeciesCatalogViewModel
    private lateinit var mockApplication: Application
    private lateinit var mockRepository: ButterflyRepository

    private val testButterflies = listOf(
        ButterflyEntity(
            id = 1,
            name = "Monarch Butterfly",
            species = "Danaus plexippus",
            imageFile = "monarch",
            description = "Beautiful orange butterfly",
            habitat = "Fields and meadows",
            wingspan = "3.5-4 inches",
            flightPeriod = "Spring-Fall",
            isFavorite = false
        ),
        ButterflyEntity(
            id = 2,
            name = "Eastern Tiger Swallowtail",
            species = "Papilio glaucus",
            imageFile = "swallowtail",
            description = "Large yellow butterfly with black stripes",
            habitat = "Gardens and woodlands",
            wingspan = "4-5 inches",
            flightPeriod = "Summer",
            isFavorite = true
        ),
        ButterflyEntity(
            id = 3,
            name = "Cabbage White",
            species = "Pieris rapae",
            imageFile = "cabbage_white",
            description = "Small white butterfly",
            habitat = "Gardens",
            wingspan = "1.5-2 inches",
            flightPeriod = "Spring-Fall",
            isFavorite = false
        ),
        ButterflyEntity(
            id = 4,
            name = "Painted Lady",
            species = "Vanessa cardui",
            imageFile = "painted_lady",
            description = "Orange and black butterfly",
            habitat = "Open areas",
            wingspan = "2-2.5 inches",
            flightPeriod = "Year-round",
            isFavorite = true
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockApplication = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)

        // Mock ButterflyRepository constructor
        mockkConstructor(ButterflyRepository::class)
        coEvery { anyConstructed<ButterflyRepository>().getAll() } returns testButterflies
        coEvery { anyConstructed<ButterflyRepository>().getAllSpecies() } returns listOf(
            "Danaus plexippus", "Papilio glaucus", "Pieris rapae", "Vanessa cardui"
        )
        coEvery { anyConstructed<ButterflyRepository>().updateFavorite(any(), any()) } returns Unit

        viewModel = SpeciesCatalogViewModel(mockApplication)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel initializes with all butterflies`() = runTest {
        // Given - setup already done in @Before

        // When
        advanceUntilIdle()

        // Then
        val result = viewModel.filteredButterflies.value
        assertEquals(4, result?.size)
        assertEquals(testButterflies, result)
    }

    @Test
    fun `viewModel loads species list on initialization`() = runTest {
        // Given - setup already done in @Before

        // When
        advanceUntilIdle()

        // Then
        val result = viewModel.speciesList.value
        assertEquals(4, result?.size)
        assertTrue(result?.contains("Danaus plexippus") == true)
        assertTrue(result?.contains("Papilio glaucus") == true)
    }

    @Test
    fun `searchAndFilter with empty query returns all butterflies`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.searchAndFilter("", false)

        // Then
        val result = viewModel.filteredButterflies.value
        assertEquals(4, result?.size)
    }

    @Test
    fun `searchAndFilter filters by name case insensitive`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.searchAndFilter("monarch", false)

        // Then
        val result = viewModel.filteredButterflies.value
        assertEquals(1, result?.size)
        assertEquals("Monarch Butterfly", result?.get(0)?.name)
    }

    @Test
    fun `searchAndFilter filters by species case insensitive`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.searchAndFilter("papilio", false)

        // Then
        val result = viewModel.filteredButterflies.value
        assertEquals(1, result?.size)
        assertEquals("Papilio glaucus", result?.get(0)?.species)
    }

    @Test
    fun `searchAndFilter with onlyFavorites true returns only favorites`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.searchAndFilter("", true)

        // Then
        val result = viewModel.filteredButterflies.value
        assertEquals(2, result?.size)
        assertTrue(result?.all { it.isFavorite } == true)
    }

    @Test
    fun `searchAndFilter combines search query and favorites filter`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.searchAndFilter("painted", true)

        // Then
        val result = viewModel.filteredButterflies.value
        assertEquals(1, result?.size)
        assertEquals("Painted Lady", result?.get(0)?.name)
        assertTrue(result?.get(0)?.isFavorite == true)
    }

    @Test
    fun `searchAndFilter with non-matching query returns empty list`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.searchAndFilter("nonexistent butterfly", false)

        // Then
        val result = viewModel.filteredButterflies.value
        assertEquals(0, result?.size)
    }

    @Test
    fun `searchAndFilter with favorites filter and non-favorite query returns empty`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.searchAndFilter("monarch", true)

        // Then
        val result = viewModel.filteredButterflies.value
        assertEquals(0, result?.size)
    }

    @Test
    fun `toggleFavorite updates butterfly favorite status`() = runTest {
        // Given
        advanceUntilIdle()
        val butterfly = testButterflies[0] // Monarch, not favorite

        // When
        viewModel.toggleFavorite(butterfly)
        advanceUntilIdle()

        // Then
        coVerify { anyConstructed<ButterflyRepository>().updateFavorite(1, true) }
    }

    @Test
    fun `toggleFavorite can unfavorite a butterfly`() = runTest {
        // Given
        advanceUntilIdle()
        val butterfly = testButterflies[1] // Swallowtail, is favorite

        // When
        viewModel.toggleFavorite(butterfly)
        advanceUntilIdle()

        // Then
        coVerify { anyConstructed<ButterflyRepository>().updateFavorite(2, false) }
    }

    @Test
    fun `searchAndFilter with partial name match returns results`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.searchAndFilter("butter", false)

        // Then
        val result = viewModel.filteredButterflies.value
        assertEquals(1, result?.size)
        assertEquals("Monarch Butterfly", result?.get(0)?.name)
    }

    @Test
    fun `searchAndFilter with uppercase query works case insensitive`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.searchAndFilter("CABBAGE", false)

        // Then
        val result = viewModel.filteredButterflies.value
        assertEquals(1, result?.size)
        assertEquals("Cabbage White", result?.get(0)?.name)
    }

    @Test
    fun `filteredButterflies LiveData emits updates`() = runTest {
        // Given
        advanceUntilIdle()
        val observer = mockk<Observer<List<ButterflyEntity>>>(relaxed = true)
        viewModel.filteredButterflies.observeForever(observer)
        val capturedValues = mutableListOf<List<ButterflyEntity>>()
        val slot = slot<List<ButterflyEntity>>()
        every { observer.onChanged(capture(slot)) } answers {
            capturedValues.add(slot.captured)
        }

        // When
        viewModel.searchAndFilter("monarch", false)

        // Then
        assertTrue(capturedValues.size >= 1)
        assertEquals(1, capturedValues.last().size)
        assertEquals("Monarch Butterfly", capturedValues.last()[0].name)

        viewModel.filteredButterflies.removeObserver(observer)
    }
}
