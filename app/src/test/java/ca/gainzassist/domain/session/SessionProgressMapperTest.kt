package ca.gainzassist.domain.session

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionProgressMapperTest {

    @Test
    fun `toLegacyMap converts snapshot to map with string keys`() {
        val snapshot = SessionProgressSnapshot(
            exerciseProgress = mapOf(1 to 100, 2 to null),
            setProgress = mapOf(10 to 50, 20 to 100)
        )

        val legacyMap = SessionProgressMapper.toLegacyMap(snapshot)

        val expectedExerciseProgress = mapOf("1" to 100, "2" to null)
        val expectedSetProgress = mapOf("10" to 50, "20" to 100)

        assertEquals(expectedExerciseProgress, legacyMap["exercise progress"])
        assertEquals(expectedSetProgress, legacyMap["set progress"])
    }

    @Test
    fun `fromLegacyMap converts map with string keys to snapshot`() {
        val legacyMap = mapOf(
            "exercise progress" to mapOf("1" to 100, "2" to null),
            "set progress" to mapOf("10" to 50, "20" to 100)
        )

        val snapshot = SessionProgressMapper.fromLegacyMap(legacyMap)

        val expectedExerciseProgress = mapOf(1 to 100, 2 to null)
        val expectedSetProgress = mapOf(10 to 50, 20 to 100)

        assertEquals(expectedExerciseProgress, snapshot.exerciseProgress)
        assertEquals(expectedSetProgress, snapshot.setProgress)
    }

    @Test
    fun `fromLegacyMap returns empty maps when input maps are missing`() {
        val legacyMap = mapOf<String, Any>() // Missing both keys

        val snapshot = SessionProgressMapper.fromLegacyMap(legacyMap)

        assertEquals(emptyMap<Int, Int?>(), snapshot.exerciseProgress)
        assertEquals(emptyMap<Int, Int?>(), snapshot.setProgress)
    }

    @Test
    fun `fromLegacyMap ignores invalid keys safely`() {
        val legacyMap = mapOf(
            "exercise progress" to mapOf("1" to 100, "invalid" to 50, "2" to 200),
            "set progress" to mapOf("10" to 50)
        )

        val snapshot = SessionProgressMapper.fromLegacyMap(legacyMap)

        val expectedExerciseProgress = mapOf(1 to 100, 2 to 200)
        val expectedSetProgress = mapOf(10 to 50)

        assertEquals(expectedExerciseProgress, snapshot.exerciseProgress)
        assertEquals(expectedSetProgress, snapshot.setProgress)
    }

    @Test
    fun `fromLegacyMap parses long values safely`() {
        // Firebase often returns numbers as Long
        val legacyMap = mapOf(
            "exercise progress" to mapOf("1" to 100L),
            "set progress" to mapOf("10" to "50") // Handles strings too, if necessary based on defensive parsing
        )

        val snapshot = SessionProgressMapper.fromLegacyMap(legacyMap)

        val expectedExerciseProgress = mapOf(1 to 100)
        val expectedSetProgress = mapOf(10 to 50)

        assertEquals(expectedExerciseProgress, snapshot.exerciseProgress)
        assertEquals(expectedSetProgress, snapshot.setProgress)
    }

    @Test
    fun `fromLegacyMap ignores invalid values safely`() {
        val legacyMap = mapOf(
            "exercise progress" to mapOf("1" to "bad", "2" to null),
            "set progress" to mapOf("10" to 50)
        )

        val snapshot = SessionProgressMapper.fromLegacyMap(legacyMap)

        val expectedExerciseProgress = mapOf(2 to null)
        val expectedSetProgress = mapOf(10 to 50)

        assertEquals(expectedExerciseProgress, snapshot.exerciseProgress)
        assertEquals(expectedSetProgress, snapshot.setProgress)
    }
}
