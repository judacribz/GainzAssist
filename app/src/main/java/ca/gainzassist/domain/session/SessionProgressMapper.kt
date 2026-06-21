package ca.gainzassist.domain.session

object SessionProgressMapper {
    private const val KEY_EXERCISE_PROGRESS = "exercise progress"
    private const val KEY_SET_PROGRESS = "set progress"

    fun toLegacyMap(snapshot: SessionProgressSnapshot): Map<String, Any> {
        val exerciseProgressMap = snapshot.exerciseProgress.mapKeys { it.key.toString() }
        val setProgressMap = snapshot.setProgress.mapKeys { it.key.toString() }

        return mapOf(
            KEY_EXERCISE_PROGRESS to exerciseProgressMap,
            KEY_SET_PROGRESS to setProgressMap
        )
    }

    fun fromLegacyMap(legacyMap: Map<String, Any?>?): SessionProgressSnapshot {
        if (legacyMap == null) {
            return SessionProgressSnapshot(emptyMap(), emptyMap())
        }

        val exerciseProgressRaw = legacyMap[KEY_EXERCISE_PROGRESS] as? Map<*, *>
        val setProgressRaw = legacyMap[KEY_SET_PROGRESS] as? Map<*, *>

        val exerciseProgress = parseProgressMap(exerciseProgressRaw)
        val setProgress = parseProgressMap(setProgressRaw)

        return SessionProgressSnapshot(
            exerciseProgress = exerciseProgress,
            setProgress = setProgress
        )
    }

    private fun parseProgressMap(rawMap: Map<*, *>?): Map<Int, Int?> {
        if (rawMap == null) return emptyMap()

        val parsedMap = mutableMapOf<Int, Int?>()

        for ((key, value) in rawMap) {
            val stringKey = key?.toString() ?: continue
            val intKey = stringKey.toIntOrNull() ?: continue

            val intValue = when (value) {
                null -> null
                is Number -> value.toInt()
                is String -> value.toIntOrNull() ?: continue
                else -> continue
            }

            parsedMap[intKey] = intValue
        }

        return parsedMap
    }
}
