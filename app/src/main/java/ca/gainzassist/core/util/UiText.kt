package ca.gainzassist.core.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * A wrapper class for UI text that can be either a hardcoded string or a string resource.
 * This is useful for passing localized text from ViewModels to the UI.
 */
sealed class UiText {

    data class DynamicString(val value: String) : UiText()
    class StringResource(@param:StringRes val resId: Int, vararg val args: Any) : UiText()

    @Composable
    fun asString(): String = when (this) {

        is DynamicString -> value
        is StringResource -> {
            val resolvedArgs = args.map { arg ->
                (arg as? UiText)?.asString() ?: arg
            }.toTypedArray()
            stringResource(resId, *resolvedArgs)
        }
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> {
            val resolvedArgs = args.map { arg ->
                (arg as? UiText)?.asString(context) ?: arg
            }.toTypedArray()
            context.getString(resId, *resolvedArgs)
        }
    }
}
