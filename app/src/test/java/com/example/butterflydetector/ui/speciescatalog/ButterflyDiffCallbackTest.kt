package com.example.butterflydetector.ui.speciescatalog

import com.example.butterflydetector.data.ButterflyEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ButterflyDiffCallbackTest {

    private lateinit var diffCallback: ButterflyAdapter.ButterflyDiffCallback

    @Before
    fun setup() {
        diffCallback = ButterflyAdapter.ButterflyDiffCallback()
    }

    @Test
    fun `areItemsTheSame returns true for same id`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
            id = 1,
            name = "Different Name",
            species = "Different Species",
            imageFile = "different",
            description = "Different description",
            habitat = "Different habitat",
            wingspan = "Different wingspan",
            flightPeriod = "Different period",
            isFavorite = true
        )

        // When
        val result = diffCallback.areItemsTheSame(butterfly1, butterfly2)

        // Then
        assertTrue(result)
    }

    @Test
    fun `areItemsTheSame returns false for different id`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
            id = 2,
            name = "Monarch",
            species = "Danaus plexippus",
            imageFile = "monarch",
            description = "Beautiful orange butterfly",
            habitat = "Fields",
            wingspan = "3.5-4 inches",
            flightPeriod = "Spring-Fall",
            isFavorite = false
        )

        // When
        val result = diffCallback.areItemsTheSame(butterfly1, butterfly2)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areContentsTheSame returns true for identical butterflies`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
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

        // When
        val result = diffCallback.areContentsTheSame(butterfly1, butterfly2)

        // Then
        assertTrue(result)
    }

    @Test
    fun `areContentsTheSame returns false when name differs`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
            id = 1,
            name = "Different Name",
            species = "Danaus plexippus",
            imageFile = "monarch",
            description = "Beautiful orange butterfly",
            habitat = "Fields",
            wingspan = "3.5-4 inches",
            flightPeriod = "Spring-Fall",
            isFavorite = false
        )

        // When
        val result = diffCallback.areContentsTheSame(butterfly1, butterfly2)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areContentsTheSame returns false when isFavorite differs`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
            id = 1,
            name = "Monarch",
            species = "Danaus plexippus",
            imageFile = "monarch",
            description = "Beautiful orange butterfly",
            habitat = "Fields",
            wingspan = "3.5-4 inches",
            flightPeriod = "Spring-Fall",
            isFavorite = true
        )

        // When
        val result = diffCallback.areContentsTheSame(butterfly1, butterfly2)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areContentsTheSame returns false when species differs`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
            id = 1,
            name = "Monarch",
            species = "Different Species",
            imageFile = "monarch",
            description = "Beautiful orange butterfly",
            habitat = "Fields",
            wingspan = "3.5-4 inches",
            flightPeriod = "Spring-Fall",
            isFavorite = false
        )

        // When
        val result = diffCallback.areContentsTheSame(butterfly1, butterfly2)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areContentsTheSame returns false when imageFile differs`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
            id = 1,
            name = "Monarch",
            species = "Danaus plexippus",
            imageFile = "different_image",
            description = "Beautiful orange butterfly",
            habitat = "Fields",
            wingspan = "3.5-4 inches",
            flightPeriod = "Spring-Fall",
            isFavorite = false
        )

        // When
        val result = diffCallback.areContentsTheSame(butterfly1, butterfly2)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areContentsTheSame returns false when description differs`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
            id = 1,
            name = "Monarch",
            species = "Danaus plexippus",
            imageFile = "monarch",
            description = "Different description",
            habitat = "Fields",
            wingspan = "3.5-4 inches",
            flightPeriod = "Spring-Fall",
            isFavorite = false
        )

        // When
        val result = diffCallback.areContentsTheSame(butterfly1, butterfly2)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areContentsTheSame returns false when habitat differs`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
            id = 1,
            name = "Monarch",
            species = "Danaus plexippus",
            imageFile = "monarch",
            description = "Beautiful orange butterfly",
            habitat = "Different habitat",
            wingspan = "3.5-4 inches",
            flightPeriod = "Spring-Fall",
            isFavorite = false
        )

        // When
        val result = diffCallback.areContentsTheSame(butterfly1, butterfly2)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areContentsTheSame returns false when wingspan differs`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
            id = 1,
            name = "Monarch",
            species = "Danaus plexippus",
            imageFile = "monarch",
            description = "Beautiful orange butterfly",
            habitat = "Fields",
            wingspan = "Different wingspan",
            flightPeriod = "Spring-Fall",
            isFavorite = false
        )

        // When
        val result = diffCallback.areContentsTheSame(butterfly1, butterfly2)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areContentsTheSame returns false when flightPeriod differs`() {
        // Given
        val butterfly1 = ButterflyEntity(
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
        val butterfly2 = ButterflyEntity(
            id = 1,
            name = "Monarch",
            species = "Danaus plexippus",
            imageFile = "monarch",
            description = "Beautiful orange butterfly",
            habitat = "Fields",
            wingspan = "3.5-4 inches",
            flightPeriod = "Different period",
            isFavorite = false
        )

        // When
        val result = diffCallback.areContentsTheSame(butterfly1, butterfly2)

        // Then
        assertFalse(result)
    }
}
