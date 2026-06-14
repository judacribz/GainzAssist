package ca.gainzassist.activities.authentication.login.view

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R

// Original Colors from resources
private val ColorBg = Color(0xFF000000) // @color/colorPrimaryDark
private val ColorAccent = Color(0xFF6B6B6B)
private val ColorGrey = Color(0xFFD4D4D4)
private val ColorBlue = Color(0xFF125C81)
private val ColorBlueDark = Color(0xFF05425E)
private val ColorFacebookBlue = Color(0xFF3B5998)
private val ColorGoogleWhite = Color(0xFFECEFF1)
private val ColorSocialOuter = Color(0xFF215466)

@Composable
private fun SocialButton(
    imageRes: Int,
    innerColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .padding(4.dp)
            .shadow(2.dp, RoundedCornerShape(10.dp))
            .background(ColorSocialOuter, RoundedCornerShape(10.dp))
            .border(1.dp, ColorBg, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ColorAccent),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .background(innerColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

data class LoginInputFieldState(
    val value: String,
    val hint: String,
    val iconRes: Int,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val isPassword: Boolean = false,
    val error: String? = null
)

@Composable
private fun LoginInputField(
    state: LoginInputFieldState,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        BasicTextField(
            value = state.value,
            onValueChange = onValueChange,
            modifier = Modifier
                .width(275.dp)
                .height(65.dp)
                .background(ColorGrey, RoundedCornerShape(20.dp))
                .border(2.5.dp, ColorBlue, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp),
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(Color.Black),
            visualTransformation = if (state.isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = state.keyboardType),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.value.isEmpty()) {
                        Text(
                            text = state.hint,
                            style = TextStyle(
                                color = Color.Gray,
                                fontStyle = FontStyle.Italic,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    innerTextField()
                    
                    Image(
                        painter = painterResource(state.iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(24.dp)
                    )
                }
            }
        )
        if (state.error != null) {
            Text(
                text = state.error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(275.dp)
            .height(50.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .background(ColorBlue, RoundedCornerShape(20.dp))
            .border(1.dp, ColorBg, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ColorBlueDark, ColorBlue)
                ),
                shape = RoundedCornerShape(21.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    actions: LoginActions,
    loginImage: Bitmap? = null,
    signUpImage: Bitmap? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C3E50))
    ) {
        Image(
            painter = painterResource(R.drawable.login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .then(if (state.isLoading) Modifier.blur(10.dp) else Modifier)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
                .then(if (state.isLoading) Modifier.blur(10.dp) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            LoginSocialButtonsSection(state = state, actions = actions)

            LoginMainImageSection(
                state = state,
                actions = actions,
                loginImage = loginImage,
                signUpImage = signUpImage,
                modifier = Modifier.weight(1f)
            )

            LoginInputFieldsSection(state = state, actions = actions)

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            ActionButton(
                text = stringResource(if (state.isLoginMode) R.string.login else R.string.sign_up),
                onClick = { if (state.isLoginMode) actions.onLoginClick() else actions.onSignUpClick() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            LoginToggleTextSection(state = state, actions = actions)
        }

        if (state.isLoading) {
            LoginLoadingOverlay()
        }
    }
}

@Composable
private fun LoginMainImageSection(
    state: LoginUiState,
    actions: LoginActions,
    loginImage: Bitmap?,
    signUpImage: Bitmap?,
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
            .fillMaxWidth()
            .scale(scale.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = actions::onImageBounceClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = state.isLoginMode, label = "MainImage") { isLogin ->
            val bitmap = if (isLogin) loginImage else signUpImage
            val cd = stringResource(if (isLogin) R.string.cd_login_img else R.string.cd_sign_up_img)
            
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = cd,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun LoginLoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .pointerInput(Unit) {
                // Consume all touch events
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun LoginSocialButtonsSection(state: LoginUiState, actions: LoginActions) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.isFacebookEnabled) {
            SocialButton(
                imageRes = R.drawable.facebook,
                innerColor = ColorFacebookBlue,
                contentDescription = stringResource(R.string.cd_facebook_login),
                onClick = actions::onFacebookSignInClick
            )

            Spacer(modifier = Modifier.width(30.dp))
        }

        SocialButton(
            imageRes = R.drawable.google,
            innerColor = ColorGoogleWhite,
            contentDescription = stringResource(R.string.cd_google_login),
            onClick = actions::onGoogleSignInClick
        )
    }
}

@Composable
private fun LoginInputFieldsSection(state: LoginUiState, actions: LoginActions) {
    LoginInputField(
        state = LoginInputFieldState(
            value = state.email,
            hint = stringResource(R.string.hint_email),
            iconRes = R.drawable.ic_mail_dark,
            keyboardType = KeyboardType.Email,
            error = state.emailError
        ),
        onValueChange = actions::onEmailChanged
    )

    Spacer(modifier = Modifier.height(10.dp))

    LoginInputField(
        state = LoginInputFieldState(
            value = state.password,
            hint = stringResource(R.string.hint_password),
            iconRes = R.drawable.ic_pass_dark,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            error = state.passwordError
        ),
        onValueChange = actions::onPasswordChanged,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun LoginToggleTextSection(state: LoginUiState, actions: LoginActions) {
    AnimatedContent(
        targetState = state.isLoginMode,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally(animationSpec = tween(300)) { -it } togetherWith slideOutHorizontally(animationSpec = tween(300)) { it }
            } else {
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
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFFD4D4D4), fontWeight = FontWeight.Bold)) {
                        append(stringResource(if (isLogin) R.string.txt_no_account else R.string.txt_yes_account))
                        append(" ")
                    }
                    withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                        append(stringResource(if (isLogin) R.string.txt_sign_up_here else R.string.txt_login_here))
                    }
                },
                fontSize = 18.sp
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        state = LoginUiState(),
        actions = object : LoginActions {
            override fun onEmailChanged(email: String) { /* no-op */ }
            override fun onPasswordChanged(password: String) { /* no-op */ }
            override fun onToggleMode() { /* no-op */ }
            override fun onLoginClick() { /* no-op */ }
            override fun onSignUpClick() { /* no-op */ }
            override fun onGoogleSignInClick() { /* no-op */ }
            override fun onFacebookSignInClick() { /* no-op */ }
            override fun onImageBounceClick() { /* no-op */ }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenSignUpModePreview() {
    LoginScreen(
        state = LoginUiState(isLoginMode = false),
        actions = object : LoginActions {
            override fun onEmailChanged(email: String) { /* no-op */ }
            override fun onPasswordChanged(password: String) { /* no-op */ }
            override fun onToggleMode() { /* no-op */ }
            override fun onLoginClick() { /* no-op */ }
            override fun onSignUpClick() { /* no-op */ }
            override fun onGoogleSignInClick() { /* no-op */ }
            override fun onFacebookSignInClick() { /* no-op */ }
            override fun onImageBounceClick() { /* no-op */ }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    LoginScreen(
        state = LoginUiState(isLoading = true),
        actions = object : LoginActions {
            override fun onEmailChanged(email: String) { /* no-op */ }
            override fun onPasswordChanged(password: String) { /* no-op */ }
            override fun onToggleMode() { /* no-op */ }
            override fun onLoginClick() { /* no-op */ }
            override fun onSignUpClick() { /* no-op */ }
            override fun onGoogleSignInClick() { /* no-op */ }
            override fun onFacebookSignInClick() { /* no-op */ }
            override fun onImageBounceClick() { /* no-op */ }
        }
    )
}
