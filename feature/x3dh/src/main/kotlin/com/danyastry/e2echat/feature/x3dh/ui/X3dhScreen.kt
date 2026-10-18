package com.danyastry.e2echat.feature.x3dh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danyastry.e2echat.core.ui.theme.ChatTheme
import com.danyastry.e2echat.feature.x3dh.R

@Composable
fun X3dhRoute(
    modifier: Modifier = Modifier,
    viewModel: X3dhViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    X3dhScreen(
        state = state,
        onPublish = viewModel::onPublish,
        onMaliciousChange = viewModel::onMaliciousChange,
        onFetch = viewModel::onFetch,
        onInitiate = viewModel::onInitiate,
        onRespond = viewModel::onRespond,
        onStartAgain = viewModel::onStartAgain,
        modifier = modifier,
    )
}

@Composable
fun X3dhScreen(
    state: X3dhUiState,
    onPublish: () -> Unit,
    onMaliciousChange: (Boolean) -> Unit,
    onFetch: () -> Unit,
    onInitiate: () -> Unit,
    onRespond: () -> Unit,
    onStartAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.x3dh_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StepCard(stringResource(R.string.x3dh_step_publish)) {
            Button(onClick = onPublish, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (state.published == null) R.string.x3dh_publish else R.string.x3dh_publish_again))
            }
            state.published?.let { keys ->
                MonoText(stringResource(R.string.x3dh_identity_key, keys.identityKey))
                MonoText(stringResource(R.string.x3dh_signed_prekey, keys.signedPreKey))
                MonoText(pluralStringResource(R.plurals.x3dh_signature, keys.signatureBytes, keys.signatureBytes))
                MonoText(pluralStringResource(R.plurals.x3dh_one_time_uploaded, keys.oneTimePreKeys, keys.oneTimePreKeys))
            }
        }
        StepCard(stringResource(R.string.x3dh_step_fetch)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.x3dh_malicious), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(R.string.x3dh_malicious_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = state.isServerMalicious, onCheckedChange = onMaliciousChange)
            }
            Button(onClick = onFetch, enabled = state.canFetch, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.x3dh_fetch))
            }
            state.bundle?.let { bundle ->
                MonoText(stringResource(R.string.x3dh_signed_prekey, bundle.signedPreKey))
                MonoText(
                    bundle.oneTimePreKeyId?.let { stringResource(R.string.x3dh_bundle_one_time, it) }
                        ?: stringResource(R.string.x3dh_bundle_no_one_time),
                )
                MonoText(pluralStringResource(R.plurals.x3dh_server_left, state.serverOneTimeLeft, state.serverOneTimeLeft))
            }
        }
        StepCard(stringResource(R.string.x3dh_step_initiate)) {
            Button(onClick = onInitiate, enabled = state.canInitiate, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.x3dh_initiate))
            }
            if (state.isSignatureValid == false) {
                Text(
                    text = stringResource(R.string.x3dh_signature_invalid),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            state.user1?.let { secret ->
                SecretDetails(secret)
                MonoText(pluralStringResource(R.plurals.x3dh_header, state.headerBytes, state.headerBytes))
            }
        }
        StepCard(stringResource(R.string.x3dh_step_respond)) {
            Button(onClick = onRespond, enabled = state.canRespond, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.x3dh_respond))
            }
            state.user2?.let { secret ->
                SecretDetails(secret)
                state.secretsMatch?.let { match ->
                    Text(
                        text = stringResource(if (match) R.string.x3dh_match else R.string.x3dh_mismatch),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (match) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    )
                }
                if (state.bundle?.oneTimePreKeyId != null) {
                    MonoText(pluralStringResource(R.plurals.x3dh_user2_held, state.user2OneTimeHeld, state.user2OneTimeHeld))
                }
            }
        }
        if (state.canStartAgain) {
            OutlinedButton(onClick = onStartAgain, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.x3dh_start_again))
            }
            Text(
                text = stringResource(R.string.x3dh_start_again_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SecretDetails(secret: SecretInfo) {
    secret.dhOutputs.forEachIndexed { index, output -> MonoText(stringResource(R.string.x3dh_dh, index + 1, output)) }
    Text(
        text = stringResource(R.string.x3dh_dh_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    MonoText(stringResource(R.string.x3dh_secret, secret.sharedSecret))
}

@Composable
private fun StepCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            content()
        }
    }
}

@Composable
private fun MonoText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
}

@Preview(showBackground = true)
@Composable
private fun X3dhScreenPreview() {
    val secret = SecretInfo(listOf("3a:91:0c:5e", "5b:e1:02:7c", "9d:44:b7:20", "c2:08:fe:41"), "15:41:59:93:c0:9a:64:85")
    ChatTheme {
        X3dhScreen(
            state = X3dhUiState(
                published = PublishedInfo("3a:91:0c:5e:77:12:d4:8b", "c2:08:fe:41:9a:6d:30:e5", 64, 3),
                serverOneTimeLeft = 2,
                bundle = BundleInfo("c2:08:fe:41:9a:6d:30:e5", 1, isForged = false),
                isSignatureValid = true,
                user1 = secret,
                headerBytes = 72,
                user2 = secret,
                user2OneTimeHeld = 2,
            ),
            onPublish = {},
            onMaliciousChange = {},
            onFetch = {},
            onInitiate = {},
            onRespond = {},
            onStartAgain = {},
        )
    }
}
