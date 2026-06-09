package ca.gainzassist.domain.session

data class SessionProgressSnapshot(
    val exerciseProgress: Map<Int, Int?>,
    val setProgress: Map<Int, Int?>
)
