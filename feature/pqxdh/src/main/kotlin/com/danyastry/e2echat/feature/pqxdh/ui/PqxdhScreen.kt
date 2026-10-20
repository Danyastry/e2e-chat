package com.danyastry.e2echat.feature.pqxdh.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danyastry.e2echat.core.ui.theme.ChatTheme
import com.danyastry.e2echat.feature.pqxdh.Handshake
import com.danyastry.e2echat.feature.pqxdh.NewChat
import com.danyastry.e2echat.feature.pqxdh.QuantumAttack
import com.danyastry.e2echat.feature.pqxdh.R
import com.danyastry.e2echat.feature.pqxdh.Recording

@Composable
fun PqxdhRoute(
    modifier: Modifier = Modifier,
    viewModel: PqxdhViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PqxdhScreen(
        state = state,
        actions = PqxdhActions(
            onRecord = viewModel::onRecord,
            onAttack = viewModel::onAttack,
            onResetHarvest = viewModel::onResetHarvest,
            onStartChat = viewModel::onStartChat,
            onResetPreKeys = viewModel::onResetPreKeys,
        ),
        modifier = modifier,
    )
}

data class PqxdhActions(
    val onRecord: () -> Unit = {},
    val onAttack: () -> Unit = {},
    val onResetHarvest: () -> Unit = {},
    val onStartChat: () -> Unit = {},
    val onResetPreKeys: () -> Unit = {},
)

@Composable
fun PqxdhScreen(
    state: PqxdhUiState,
    actions: PqxdhActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Hint(stringResource(R.string.pqxdh_intro))
        HarvestSection(state, actions)
        HorizontalDivider()
        PreKeySection(state, actions)
    }
}

@Composable
private fun HarvestSection(state: PqxdhUiState, actions: PqxdhActions) {
    Text(text = stringResource(R.string.pqxdh_harvest_title), style = MaterialTheme.typography.titleMedium)
    val recorded = state.recordings.isNotEmpty()
    Button(onClick = actions.onRecord, enabled = !recorded, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.pqxdh_record))
    }
    if (recorded) ResultCard(state.recordings) { RecordingRow(it) }
    Button(onClick = actions.onAttack, enabled = recorded && state.attacks.isEmpty(), modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.pqxdh_attack))
    }
    if (state.attacks.isNotEmpty()) {
        ResultCard(state.attacks) { AttackRow(it) }
        Hint(stringResource(R.string.pqxdh_simulation))
    }
    ResetButton(actions.onResetHarvest)
}

@Composable
private fun RecordingRow(recording: Recording) {
    Text(
        text = stringResource(R.string.pqxdh_recording, recording.handshake.label, recording.bundleBytes, recording.headerBytes),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun AttackRow(attack: QuantumAttack) {
    Text(text = attack.handshake.label, style = MaterialTheme.typography.titleSmall)
    Verdict(stringResource(R.string.pqxdh_dh_broken, attack.dhOutputsBroken), isSafe = null)
    Verdict(
        text = stringResource(if (attack.isReadable) R.string.pqxdh_x3dh_broken else R.string.pqxdh_pqxdh_safe),
        isSafe = !attack.isReadable,
    )
    attack.attackerRead.forEach { read ->
        Verdict(
            text = if (read != null) stringResource(R.string.pqxdh_read, read) else stringResource(R.string.pqxdh_not_read),
            isSafe = read == null,
        )
    }
}

@Composable
private fun PreKeySection(state: PqxdhUiState, actions: PqxdhActions) {
    Text(text = stringResource(R.string.pqxdh_prekeys_title), style = MaterialTheme.typography.titleMedium)
    Hint(stringResource(R.string.pqxdh_prekeys_intro))
    Hint(pluralStringResource(R.plurals.pqxdh_prekeys_left, state.oneTimeKemPreKeysLeft, state.oneTimeKemPreKeysLeft))
    Button(onClick = actions.onStartChat, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.pqxdh_start_chat))
    }
    if (state.chats.isNotEmpty()) ResultCard(state.chats) { ChatRow(it) }
    ResetButton(actions.onResetPreKeys)
}

@Composable
private fun ChatRow(chat: NewChat) {
    Text(
        text = if (chat.isLastResort) {
            stringResource(R.string.pqxdh_chat_last_resort, chat.number)
        } else {
            stringResource(R.string.pqxdh_chat_one_time, chat.number, chat.kemPreKeyId)
        },
        style = MaterialTheme.typography.bodyMedium,
    )
    Verdict(stringResource(if (chat.agreed) R.string.pqxdh_agreed else R.string.pqxdh_not_agreed), isSafe = chat.agreed)
}

private val Handshake.label: String
    get() = when (this) {
        Handshake.X3DH -> "X3DH"
        Handshake.PQXDH -> "PQXDH"
    }

@Composable
private fun Hint(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Verdict(text: String, isSafe: Boolean?) {
    val color = when (isSafe) {
        true -> MaterialTheme.colorScheme.primary
        false -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = color)
}

@Composable
private fun <T> ResultCard(items: List<T>, row: @Composable ColumnScope.(T) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                row(item)
            }
        }
    }
}

@Composable
private fun ResetButton(onReset: () -> Unit) {
    OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.pqxdh_reset))
    }
}

@Preview(showBackground = true)
@Composable
private fun PqxdhScreenPreview() {
    ChatTheme {
        PqxdhScreen(
            state = PqxdhUiState(
                recordings = listOf(Recording(Handshake.X3DH, 200, 72, emptyList()), Recording(Handshake.PQXDH, 1836, 1644, emptyList())),
                attacks = listOf(
                    QuantumAttack(Handshake.X3DH, 4, listOf("The launch is on Friday")),
                    QuantumAttack(Handshake.PQXDH, 4, listOf(null)),
                ),
                oneTimeKemPreKeysLeft = 3,
            ),
            actions = PqxdhActions(),
        )
    }
}
