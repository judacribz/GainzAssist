package ca.judacribz.gainzassist.activities.authentication.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import ca.judacribz.gainzassist.R

@Composable
fun LoginScreen(
    state: LoginUiState,
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
                Image(
                    painter = painterResource(id = R.drawable.com_facebook_button_login_logo),
                    contentDescription = "Facebook Login",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { actions.onFacebookSignInClick() }
                        .padding(15.dp)
                )

                Spacer(modifier = Modifier.width(60.dp))

                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google Login",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { actions.onGoogleSignInClick() }
                        .padding(15.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Main Image Box (Crossfade for Login/SignUp toggle)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clickable { actions.onImageBounceClick() },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = state.isLoginMode, label = "MainImage") { isLogin ->
                    if (isLogin) {
                        Image(
                            painter = painterResource(id = R.drawable.login_img),
                            contentDescription = stringResource(id = R.string.cd_login_img),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.sign_up_img),
                            contentDescription = stringResource(id = R.string.cd_sign_up_img),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Input Fields
            OutlinedTextField(
                value = state.email,
                onValueChange = actions::onEmailChanged,
                placeholder = { Text("Email", fontStyle = FontStyle.Italic) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = state.emailError != null,
                supportingText = state.emailError?.let { { Text(it) } },
                modifier = Modifier.width(275.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f)
                ),
                trailingIcon = {
                    Image(painter = painterResource(R.drawable.ic_mail_dark), contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = actions::onPasswordChanged,
                placeholder = { Text("Password", fontStyle = FontStyle.Italic) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.passwordError != null,
                supportingText = state.passwordError?.let { { Text(it) } },
                modifier = Modifier.width(275.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f)
                ),
                trailingIcon = {
                    Image(painter = painterResource(R.drawable.ic_pass_dark), contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button
            Button(
                onClick = { if (state.isLoginMode) actions.onLoginClick() else actions.onSignUpClick() },
                modifier = Modifier
                    .width(275.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text(
                    text = if (state.isLoginMode) stringResource(R.string.login) else stringResource(R.string.sign_up),
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Toggle Text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { actions.onToggleMode() }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (state.isLoginMode) {
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
