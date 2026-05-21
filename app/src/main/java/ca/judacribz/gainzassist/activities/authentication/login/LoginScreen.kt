package ca.judacribz.gainzassist.activities.authentication.login

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Button
import android.widget.EditText
import android.view.Gravity
import android.text.InputType
import android.text.TextWatcher
import android.text.Editable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.google.android.material.textfield.TextInputLayout
import ca.judacribz.gainzassist.R

@Composable
private fun LegacySocialImageButton(
    @DrawableRes imageRes: Int,
    @DrawableRes backgroundRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.size(100.dp),
        factory = { context ->
            ImageButton(context).apply {
                background = ContextCompat.getDrawable(context, backgroundRes)
                setImageResource(imageRes)
                scaleType = ImageView.ScaleType.FIT_CENTER

                val paddingPx = (15 * context.resources.displayMetrics.density).toInt()
                setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

                setContentDescription(contentDescription)
                setOnClickListener { onClick() }
            }
        },
        update = { view ->
            view.setOnClickListener { onClick() }
        }
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    loginImage: Bitmap? = null,
    signUpImage: Bitmap? = null,
    actions: LoginActions
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C3E50)) // fallback bg color
    ) {
        // Background Image (simulating the blur layout and login_bg)
        Image(
            painter = painterResource(id = R.drawable.login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Social Login Buttons
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LegacySocialImageButton(
                    imageRes = R.drawable.facebook,
                    backgroundRes = R.drawable.anim_facebook_ripple,
                    contentDescription = "Facebook Login",
                    onClick = actions::onFacebookSignInClick
                )

                Spacer(modifier = Modifier.width(30.dp))

                LegacySocialImageButton(
                    imageRes = R.drawable.google,
                    backgroundRes = R.drawable.anim_google_ripple,
                    contentDescription = "Google Login",
                    onClick = actions::onGoogleSignInClick
                )
            }


            // Main Image Box (Crossfade for Login/SignUp toggle)
            val scale = remember { Animatable(1f) }

            LaunchedEffect(state.imageBounceTrigger) {
                if (state.imageBounceTrigger > 0) {
                    scale.animateTo(
                        targetValue = 1.1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .scale(scale.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // No ripple for this bounce image
                        onClick = actions::onImageBounceClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = state.isLoginMode, label = "MainImage") { isLogin ->
                    if (isLogin) {
                        loginImage?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = stringResource(id = R.string.cd_login_img),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        signUpImage?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = stringResource(id = R.string.cd_sign_up_img),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            // Input Fields
            AndroidView<TextInputLayout>(
                modifier = Modifier.size(width = 275.dp, height = 65.dp),
                factory = { context ->
                    val emailInputLayout = TextInputLayout(context).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val newEmailField = EditText(context).apply {
                        id = R.id.et_email
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
                        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                        setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_mail_dark, 0)
                        hint = context.getString(R.string.hint_email)
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

                        setTextAppearance(context, R.style.BlueEditTextStyle)
                        background = ContextCompat.getDrawable(context, R.drawable.anim_edit_text_ripple_blue)
                        gravity = Gravity.CENTER

                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            override fun afterTextChanged(s: Editable?) {
                                actions.onEmailChanged(s?.toString() ?: "")
                            }
                        })
                    }
                    emailInputLayout.addView(newEmailField)
                    emailInputLayout
                },
                update = { view ->
                    val et = view.findViewById<EditText>(R.id.et_email)
                    if (et != null && et.text.toString() != state.email) {
                        et.setText(state.email)
                        et.setSelection(state.email.length)
                    }
                    view.error = state.emailError
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            AndroidView<TextInputLayout>(
                modifier = Modifier.size(width = 275.dp, height = 65.dp).padding(bottom = 10.dp),
                factory = { context ->
                    val passwordInputLayout = TextInputLayout(context).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                    val newPasswordField = EditText(context).apply {
                        id = R.id.et_password
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
                        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                        setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_pass_dark, 0)
                        hint = context.getString(R.string.hint_password)
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                        setTextAppearance(context, R.style.BlueEditTextStyle)
                        background = ContextCompat.getDrawable(context, R.drawable.anim_edit_text_ripple_blue)
                        gravity = Gravity.CENTER

                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            override fun afterTextChanged(s: Editable?) {
                                actions.onPasswordChanged(s?.toString() ?: "")
                            }
                        })
                    }
                    passwordInputLayout.addView(newPasswordField)
                    passwordInputLayout
                },
                update = { view ->
                    val et = view.findViewById<EditText>(R.id.et_password)
                    if (et != null && et.text.toString() != state.password) {
                        et.setText(state.password)
                        et.setSelection(state.password.length)
                    }
                    view.error = state.passwordError
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            AndroidView(
                modifier = Modifier.size(width = 275.dp, height = 50.dp),
                factory = { context ->
                    Button(context).apply {
                        background = ContextCompat.getDrawable(context, R.drawable.anim_button_ripple_blue)
                        setTextColor(ContextCompat.getColorStateList(context, R.drawable.selector_btn_text_default))
                        gravity = Gravity.CENTER
                        elevation = 4f * context.resources.displayMetrics.density
                        translationZ = 4f * context.resources.displayMetrics.density

                        val paddingTopPx = (10 * context.resources.displayMetrics.density).toInt()
                        val paddingSidePx = (20 * context.resources.displayMetrics.density).toInt()
                        setPadding(paddingSidePx, paddingTopPx, paddingSidePx, paddingTopPx)

                        setOnClickListener {
                            if (state.isLoginMode) actions.onLoginClick() else actions.onSignUpClick()
                        }
                    }
                },
                update = { view ->
                    view.text = view.context.getString(if (state.isLoginMode) R.string.login else R.string.sign_up)
                    view.setOnClickListener {
                        if (state.isLoginMode) actions.onLoginClick() else actions.onSignUpClick()
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Toggle Text
            AnimatedContent(
                targetState = state.isLoginMode,
                transitionSpec = {
                    if (targetState) {
                        // Going to login: slide in from left, out to right
                        slideInHorizontally(animationSpec = tween(300)) { -it } togetherWith slideOutHorizontally(animationSpec = tween(300)) { it }
                    } else {
                        // Going to sign up: slide in from right, out to left
                        slideInHorizontally(animationSpec = tween(300)) { it } togetherWith slideOutHorizontally(animationSpec = tween(300)) { -it }
                    }
                },
                label = "toggleTextAnimation"
            ) { isLogin ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { actions.onToggleMode() }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLogin) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color(0xFFD4D4D4), fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.txt_no_account))
                                    append(" ")
                                }
                                withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.txt_sign_up_here))
                                }
                            },
                            fontSize = 18.sp
                        )
                    } else {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color(0xFFD4D4D4), fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.txt_yes_account))
                                    append(" ")
                                }
                                withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                                    append(stringResource(R.string.txt_login_here))
                                }
                            },
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        state = LoginUiState(),
        actions = object : LoginActions {
            override fun onEmailChanged(email: String) {}
            override fun onPasswordChanged(password: String) {}
            override fun onToggleMode() {}
            override fun onLoginClick() {}
            override fun onSignUpClick() {}
            override fun onGoogleSignInClick() {}
            override fun onFacebookSignInClick() {}
            override fun onImageBounceClick() {}
        }
    )
}
