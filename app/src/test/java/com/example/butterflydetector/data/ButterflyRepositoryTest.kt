package com.example.butterflydetector.data

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ButterflyRepositoryTest {

    private lateinit var repository: ButterflyRepository
    private lateinit var mockDao: ButterflyDao
    private lateinit var mockContext: Context
    private lateinit var mockDatabase: ButterflyDatabase

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        every { mockContext.applicationContext } returns mockContext

        mockDao = mockk(relaxed = true)
        mockDatabase = mockk(relaxed = true)

        mockkObject(ButterflyDatabase.Companion)
        every { ButterflyDatabase.getDatabase(any()) } returns mockDatabase
        every { mockDatabase.butterflyDao() } returns mockDao

        repository = ButterflyRepository(mockContext)
    }

    @After
    fun tearDown() {
        unmockkObject(ButterflyDatabase.Companion)
    }

    @Test
    fun `getAll returns all butterflies from dao`() = runTest {
        // Given
        val expectedButterflies = listOf(
            ButterflyEntity(
                id = 1,
                name = "Monarch",
                species = "Danaus plexippus",
                imageFile = "monarch",
                description = "Beautiful orange butterfly",
                habitat = "Fields",
                wingspan = "3.5-4 inches",
                flightPeriod = "Spring-Fall",
                isFavorite = false
            ),
            ButterflyEntity(
                id = 2,
                name = "Swallowtail",
                species = "Papilio",
                imageFile = "swallowtail",
                description = "Large butterfly with tail",
                habitat = "Gardens",
                wingspan = "4-5 inches",
                flightPeriod = "Summer",
                isFavorite = true
            )
        )
        coEvery { mockDao.getAll() } returns expectedButterflies

        // When
        val result = repository.getAll()

        // Then
        assertEquals(expectedButterflies, result)
        coVerify(exactly = 1) { mockDao.getAll() }
    }

    @Test
    fun `getBySpecies returns filtered butterflies`() = runTest {
        // Given
        val species = "Danaus plexippus"
        val expectedButterflies = listOf(
            ButterflyEntity(
                id = 1,
                name = "Monarch",
                species = species,
                imageFile = "monarch",
                description = "Beautiful orange butterfly",
                habitat = "Fields",
                wingspan = "3.5-4 inches",
                flightPeriod = "Spring-Fall",
                isFavorite = false
            )
        )
        coEvery { mockDao.getBySpecies(species) } returns expectedButterflies

        // When
        val result = repository.getBySpecies(species)

        // Then
        assertEquals(expectedButterflies, result)
        coVerify(exactly = 1) { mockDao.getBySpecies(species) }
    }

    @Test
    fun `searchByName returns matching butterflies`() = runTest {
        // Given
        val query = "Monarch"
        val expectedButterflies = listOf(
            ButterflyEntity(
                id = 1,
                name = "Monarch",
                species = "Danaus plexippus",
                imageFile = "monarch",
                description = "Beautiful orange butterfly",
                habitat = "Fields",
                wingspan = "3.5-4 inches",
                flightPeriod = "Spring-Fall",
                isFavorite = false
            )
        )
        coEvery { mockDao.searchByName(query) } returns expectedButterflies

        // When
        val result = repository.searchByName(query)

        // Then
        assertEquals(expectedButterflies, result)
        coVerify(exactly = 1) { mockDao.searchByName(query) }
    }

    @Test
    fun `getFavorites returns only favorite butterflies`() = runTest {
        // Given
        val expectedButterflies = listOf(
            ButterflyEntity(
                id = 2,
                name = "Swallowtail",
                species = "Papilio",
                imageFile = "swallowtail",
                description = "Large butterfly with tail",
                habitat = "Gardens",
                wingspan = "4-5 inches",
                flightPeriod = "Summer",
                isFavorite = true
            )
        )
        coEvery { mockDao.getFavorites() } returns expectedButterflies

        // When
        val result = repository.getFavorites()

        // Then
        assertEquals(expectedButterflies, result)
        assertEquals(1, result.size)
        assertEquals(true, result[0].isFavorite)
        coVerify(exactly = 1) { mockDao.getFavorites() }
    }

    @Test
    fun `getAllSpecies returns distinct species list`() = runTest {
        // Given
        val expectedSpecies = listOf("Danaus plexippus", "Papilio", "Pieris rapae")
        coEvery { mockDao.getAllSpecies() } returns expectedSpecies

        // When
        val result = repository.getAllSpecies()

        // Then
        assertEquals(expectedSpecies, result)
        assertEquals(3, result.size)
        coVerify(exactly = 1) { mockDao.getAllSpecies() }
    }

    @Test
    fun `insertAll inserts butterflies into database`() = runTest {
        // Given
        val butterflies = listOf(
            ButterflyEntity(
                id = 1,
                name = "Monarch",
                species = "Danaus plexippus",
                imageFile = "monarch",
                description = "Beautiful orange butterfly",
                habitat = "Fields",
                wingspan = "3.5-4 inches",
                flightPeriod = "Spring-Fall",
                isFavorite = false
            )
        )
        coEvery { mockDao.insertAll(butterflies) } returns Unit

        // When
        repository.insertAll(butterflies)

        // Then
        coVerify(exactly = 1) { mockDao.insertAll(butterflies) }
    }

    @Test
    fun `updateFavorite updates butterfly favorite status`() = runTest {
        // Given
        val butterflyId = 1
        val isFavorite = true
        coEvery { mockDao.updateFavorite(butterflyId, isFavorite) } returns Unit

        // When
        repository.updateFavorite(butterflyId, isFavorite)

        // Then
        coVerify(exactly = 1) { mockDao.updateFavorite(butterflyId, isFavorite) }
    }

    @Test
    fun `updateFavorite can toggle favorite from true to false`() = runTest {
        // Given
        val butterflyId = 2
        val isFavorite = false
        coEvery { mockDao.updateFavorite(butterflyId, isFavorite) } returns Unit

        // When
        repository.updateFavorite(butterflyId, isFavorite)

        // Then
        coVerify(exactly = 1) { mockDao.updateFavorite(butterflyId, isFavorite) }
    }
}
