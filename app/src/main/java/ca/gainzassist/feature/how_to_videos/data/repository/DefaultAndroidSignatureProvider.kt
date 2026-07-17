package ca.gainzassist.feature.how_to_videos.data.repository

import android.content.Context
import android.content.pm.Signature
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import ca.gainzassist.feature.how_to_videos.domain.repository.AndroidSignatureProvider
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException

class DefaultAndroidSignatureProvider(private val context: Context) : AndroidSignatureProvider {

    override val packageName: String
        get() = context.packageName

    override val certificateSha1: String
        get() = runCatching {
            val signatures: List<Signature> = PackageInfoCompat.getSignatures(
                /* packageManager = */ context.packageManager,
                /* packageName = */ context.packageName
            )
            val signature = signatures.firstOrNull()
            if (signature != null) {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(signature.toByteArray())
                val digest = md.digest()
                val hexString = StringBuilder()
                for (b in digest) {
                    hexString.append(String.format("%02X", b))
                }
                hexString.toString()
            } else {
                ""
            }
        }.getOrElse { e ->
            if (e is CancellationException) throw e
            Log.e("SignatureProvider", "Failed to get SHA1", e)
            ""
        }
}
