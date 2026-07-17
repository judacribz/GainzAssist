package ca.gainzassist.feature.how_to_videos.domain.repository

interface AndroidSignatureProvider {
    val packageName: String
    val certificateSha1: String
}
