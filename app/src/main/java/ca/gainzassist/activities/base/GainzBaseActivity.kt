package ca.gainzassist.activities.base

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import ca.gainzassist.core.util.UI
import ca.gainzassist.ui.components.GainzEdgeToEdgeBox

abstract class GainzBaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UI.setInitTheme(this)

        onBeforeSetContent()

        setContent {
            GainzEdgeToEdgeBox {
                InnerContent()
            }
        }
    }

    /**
     * Override this method to initialize any variables (like Intent extras) 
     * before the first Compose UI pass is drawn.
     */
    open fun onBeforeSetContent() {}

    /**
     * The main Compose UI content of the activity.
     */
    @Composable
    abstract fun InnerContent()
}
