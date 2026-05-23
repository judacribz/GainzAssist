package ca.judacribz.gainzassist.activities.authentication.login

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import ca.judacribz.gainzassist.R

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
                indication = rememberRipple(color = ColorAccent),
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
                painter = painterResource(id = imageRes),
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun LoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    iconRes: Int,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        BasicTextField(
            value = value,
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
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
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
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(24.dp)
                    )
                }
            }
        )
        if (error != null) {
            Text(
                text = error,
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
                indication = rememberRipple(color = Color.White),
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
    loginImage: Bitmap? = null,
    signUpImage: Bitmap? = null,
    actions: LoginActions
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C3E50))
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_bg),
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

            // Social Login Buttons
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

            // Main Image Box with Bounce Animation
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

            // Input Fields
            LoginInputField(
                value = state.email,
                onValueChange = actions::onEmailChanged,
                hint = stringResource(id = R.string.hint_email),
                iconRes = R.drawable.ic_mail_dark,
                keyboardType = KeyboardType.Email,
                error = state.emailError
            )

            Spacer(modifier = Modifier.height(10.dp))

            LoginInputField(
                value = state.password,
                onValueChange = actions::onPasswordChanged,
                hint = stringResource(id = R.string.hint_password),
                iconRes = R.drawable.ic_pass_dark,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                error = state.passwordError,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            ActionButton(
                text = stringResource(if (state.isLoginMode) R.string.login else R.string.sign_up),
                onClick = { if (state.isLoginMode) actions.onLoginClick() else actions.onSignUpClick() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Toggle Text
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

        if (state.isLoading) {
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

@Preview(showBackground = true)
@Composable
fun LoginScreenSignUpModePreview() {
    LoginScreen(
        state = LoginUiState(isLoginMode = false),
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

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    LoginScreen(
        state = LoginUiState(isLoading = true),
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
