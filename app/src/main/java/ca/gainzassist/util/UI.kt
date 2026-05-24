package ca.gainzassist.util

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import ca.gainzassist.R
import ca.gainzassist.util.Preferences.getThemePref
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.facebook.rebound.SpringUtil
import java.util.Locale

object UI {

    private var backPressedTwice = false

    @JvmStatic
    fun handleBackButton(context: Context) {
        if (backPressedTwice) {
            context.startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            backPressedTwice = true
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ backPressedTwice = false }, 2000)
        }
    }

    @JvmStatic
    fun setToolbar(act: AppCompatActivity, titleId: Int, setBackArrow: Boolean): String {
        return setToolbar(act, act.resources.getString(titleId), setBackArrow)
    }

    @JvmStatic
    fun setToolbar(act: AppCompatActivity, title: String, setBackArrow: Boolean): String {
        act.setSupportActionBar(act.findViewById<View>(R.id.toolbar) as Toolbar)
        val actionBar = act.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false)
            if (setBackArrow) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setDisplayShowHomeEnabled(true)
            }
        }
        val titleView = act.findViewById<View>(R.id.title) as TextView
        titleView.transformationMethod = null
        titleView.text = title
        return title
    }

    @JvmStatic
    fun setInitTheme(act: Activity) {
        val col = getThemePref(act)
        if (col != null) {
            if (col == "blue") {
                act.setTheme(R.style.BlueTheme)
            } else if (col == "green") {
                act.setTheme(R.style.GreenTheme)
            }
        }
    }

    @JvmStatic
    fun setSpinnerWithArray(act: Activity?, arrResId: Int, spr: Spinner) {
        val adapter = ArrayAdapter.createFromResource(
            act!!,
            arrResId,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spr.adapter = adapter
    }

    @JvmStatic
    fun getTextString(spr: Spinner): String {
        return spr.selectedItem.toString().lowercase(Locale.getDefault())
    }

    @JvmStatic
    fun getTextInt(tv: TextView): Int {
        return getTextString(tv).toInt()
    }

    @JvmStatic
    fun getTextFloat(et: EditText): Float {
        return getTextString(et).toFloat()
    }

    @JvmStatic
    fun getTextString(tv: TextView): String {
        return tv.text.toString().trim { it <= ' ' }
    }

    @JvmStatic
    fun validateForm(act: Activity, ets: Array<EditText>): Boolean {
        var isValid = true
        for (et in ets) {
            isValid = isValid and validateFormEntry(act, et)
        }
        return isValid
    }

    @JvmStatic
    fun validateFormEntry(act: Activity, et: EditText): Boolean {
        val text = et.text.toString().trim { it <= ' ' }
        if (text.isEmpty()) {
            et.error = act.getString(R.string.err_required)
            return false
        }
        return true
    }

    @JvmStatic
    fun clearForm(ets: Array<EditText>) {
        for (et in ets) {
            clearFormEntry(et)
        }
    }

    @JvmStatic
    fun clearFormEntry(et: EditText) {
        et.setText("")
    }

    @JvmStatic
    fun calculateNoOfColumns(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return (dpWidth / 180).toInt()
    }

    @JvmStatic
    fun setSpring(view: View): Spring {
        val springSystem = SpringSystem.create()
        val spring = springSystem.createSpring()
        spring.addListener(object : SimpleSpringListener() {
            override fun onSpringUpdate(spring: Spring) {
                val value = SpringUtil.mapValueFromRangeToRange(spring.currentValue, 0.0, 1.0, 1.0, 0.0).toFloat()
                view.scaleX = value
                view.scaleY = value
                if (spring.isAtRest) {
                    spring.endValue = 0.0
                }
            }
        })
        return spring
    }

    @JvmStatic
    fun handleFocusLeft(et: EditText, min: Number, res: Number) {
        val etStr = getTextString(et)
        if (etStr.isEmpty()) {
            et.setText(res.toString())
        } else if (etStr.toFloat() < min.toFloat()) {
            et.setText(min.toString())
        }
    }

    @JvmStatic
    fun setVisibleIfDisabled(view: View) {
        if (!view.isEnabled) {
            view.isEnabled = true
            view.visibility = View.VISIBLE
        }
    }

    @JvmStatic
    fun handleNumChanged(view: View, str: String, min: Number): Number {
        val num: Number = if (str.isEmpty()) min else str.toFloat()
        if (num.toFloat() <= min.toFloat()) {
            setGoneIfEnabled(view)
        }
        return num
    }

    @JvmStatic
    fun setGoneIfEnabled(view: View) {
        if (view.isEnabled) {
            view.isEnabled = false
            view.visibility = View.GONE
        }
    }

    @JvmStatic
    fun setText(view: EditText, num: Number) {
        view.setText(num.toString())
    }

    class ProgressHandler : Handler() {
        private var progress: ProgressDialog? = null
        private var blurLayout: View? = null
        private var msg: String? = null
        private var count = 0

        fun setProgress(context: Context?, msg: String, blurLayout: View?) {
            progress = ProgressDialog(context)
            this.blurLayout = blurLayout
            this.msg = msg + DOT
            progress!!.setMessage(this.msg)
            progress!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        }

        fun setTitle(title: String?) {
            progress!!.setTitle(title)
        }

        fun show() {
            if (progress != null) {
                progress!!.show()
                if (blurLayout != null) {
                    blurLayout!!.visibility = View.VISIBLE
                    blurLayout!!.top = 0
                }
                Thread {
                    try {
                        while (progress!!.isShowing) {
                            Thread.sleep(10)
                            progress!!.setMessage(msg!!.substring(0, msg!!.length - 1 - ++count % 3))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }

        fun dismiss() {
            if (progress != null) {
                progress!!.dismiss()
                blurLayout!!.visibility = View.GONE
            }
        }

        companion object {
            private const val DOT = "..."
        }
    }
}
