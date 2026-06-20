package ca.gainzassist.activities.base

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import ca.gainzassist.core.util.UI
import ca.gainzassist.ui.components.GainzEdgeToEdgeBox

abstract class GainzBaseActivity : AppCompatActivity() {

    /**
     * The main Compose UI content of the activity.
     */
    @Composable
    abstract fun InnerContent()

    /**
     * Override this method to customize the root Compose wrapper.
     */
    @Composable
    open fun AppContent() = GainzEdgeToEdgeBox {
        InnerContent()
    }

    /**
     * Override this method to initialize any variables (like Intent extras)
     * before the first Compose UI pass is drawn.
     */
    open fun onBeforeSetContent() = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureEdgeToEdge()
        UI.setInitTheme(this)
        onBeforeSetContent()
        setContent {
            AppContent()
        }
    }

    /**
     * Override this method to customize edge-to-edge configuration.
     */
    open fun configureEdgeToEdge() = enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.light(
            scrim = Color.TRANSPARENT,
            darkScrim = Color.TRANSPARENT
        ),
        navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
    )
}
