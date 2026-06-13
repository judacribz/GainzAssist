package ca.gainzassist.activities.main.view.tab_screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import ca.gainzassist.ui.components.GainzButton

data class SettingsUiState(
    val signedInText: String,
    val versionText: String
)

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSignOutClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAccountDeletionClick: () -> Unit,
    onContactSupportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(15.dp)
    ) {
        // Account Section
        Text(
            text = stringResource(id = R.string.settings_account),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = uiState.signedInText,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        GainzButton(
            text = stringResource(id = R.string.settings_sign_out),
            onClick = onSignOutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        // Privacy & Support Section
        Text(
            text = stringResource(id = R.string.settings_privacy_support),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        GainzButton(
            text = stringResource(id = R.string.settings_privacy_policy),
            onClick = onPrivacyPolicyClick,
            modifier = Modifier.fillMaxWidth()
        )
        GainzButton(
            text = stringResource(id = R.string.settings_account_deletion),
            onClick = onAccountDeletionClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        GainzButton(
            text = stringResource(id = R.string.settings_contact_support),
            onClick = onContactSupportClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp)
        )

        // About Section
        Text(
            text = stringResource(id = R.string.settings_about),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = uiState.versionText,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = stringResource(id = R.string.settings_app_description)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview_SignedIn() {
    SettingsScreen(
        uiState = SettingsUiState(
            signedInText = "Signed in as: example@example.com",
            versionText = "Version 1.1.0 (2)"
        ),
        onSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview_EmailUnavailable() {
    SettingsScreen(
        uiState = SettingsUiState(
            signedInText = "Email Unavailable",
            versionText = "Version 1.1.0 (2)"
        ),
        onSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
    )
}

@Preview(showBackground = true, widthDp = 320, heightDp = 480)
@Composable
fun SettingsScreenPreview_SmallPhone() {
    SettingsScreen(
        uiState = SettingsUiState(
            signedInText = "Signed in as: example@example.com",
            versionText = "Version 1.1.0 (2)"
        ),
        onSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
    )
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun SettingsScreenPreview_LargeFont() {
    SettingsScreen(
        uiState = SettingsUiState(
            signedInText = "Signed in as: example@example.com",
            versionText = "Version 1.1.0 (2)"
        ),
        onSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
    )
}
