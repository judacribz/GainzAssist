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

data class SettingsScreenActions(
    val onSignOutClick: () -> Unit = {},
    val onPrivacyPolicyClick: () -> Unit = {},
    val onAccountDeletionClick: () -> Unit = {},
    val onContactSupportClick: () -> Unit = {}
)

private val ContainerPadding = 15.dp
private val SectionTitleFontSize = 18.sp
private val SectionBottomPadding = 8.dp
private val ItemBottomPadding = 8.dp
private val ItemTopPadding = 8.dp
private val VersionBottomPadding = 4.dp
private val SectionSpacing = 24.dp

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    actions: SettingsScreenActions = SettingsScreenActions()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(ContainerPadding)
    ) {
        // Account Section
        Text(
            text = stringResource(R.string.settings_account),
            fontSize = SectionTitleFontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = SectionBottomPadding)
        )
        Text(
            text = uiState.signedInText,
            modifier = Modifier.padding(bottom = ItemBottomPadding)
        )
        GainzButton(
            text = stringResource(R.string.settings_sign_out),
            onClick = actions.onSignOutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SectionSpacing)
        )

        // Privacy & Support Section
        Text(
            text = stringResource(R.string.settings_privacy_support),
            fontSize = SectionTitleFontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = SectionBottomPadding)
        )
        GainzButton(
            text = stringResource(R.string.settings_privacy_policy),
            onClick = actions.onPrivacyPolicyClick,
            modifier = Modifier.fillMaxWidth()
        )
        GainzButton(
            text = stringResource(R.string.settings_account_deletion),
            onClick = actions.onAccountDeletionClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ItemTopPadding)
        )
        GainzButton(
            text = stringResource(R.string.settings_contact_support),
            onClick = actions.onContactSupportClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ItemTopPadding, bottom = SectionSpacing)
        )

        // About Section
        Text(
            text = stringResource(R.string.settings_about),
            fontSize = SectionTitleFontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = SectionBottomPadding)
        )
        Text(
            text = uiState.versionText,
            modifier = Modifier.padding(bottom = VersionBottomPadding)
        )
        Text(
            text = stringResource(R.string.settings_app_description)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreviewSignedIn() {
    SettingsScreen(
        uiState = SettingsUiState(
            signedInText = "Signed in as: example1@example.com",
            versionText = "Version 1.1.0 (1)"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreviewEmailUnavailable() {
    SettingsScreen(
        uiState = SettingsUiState(
            signedInText = "Email Unavailable",
            versionText = "Version 1.1.0 (2)"
        )
    )
}

@Preview(showBackground = true, widthDp = 320, heightDp = 480)
@Composable
fun SettingsScreenPreviewSmallPhone() {
    SettingsScreen(
        uiState = SettingsUiState(
            signedInText = "Signed in as: example2@example.com",
            versionText = "Version 1.1.0 (3)"
        )
    )
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun SettingsScreenPreviewLargeFont() {
    SettingsScreen(
        uiState = SettingsUiState(
            signedInText = "Signed in as: example@example.com",
            versionText = "Version 1.1.0 (4)"
        )
    )
}
