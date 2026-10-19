package com.danyastry.e2echat.feature.ratchet.ui

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danyastry.e2echat.core.ui.theme.ChatTheme
import com.danyastry.e2echat.feature.ratchet.R

@Composable
fun RatchetRoute(
    modifier: Modifier = Modifier,
    viewModel: RatchetViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RatchetScreen(
        state = state,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::onSend,
        onRestart = viewModel::onRestart,
        modifier = modifier,
    )
}

@Composable
fun RatchetScreen(
    state: RatchetUiState,
    onInputChange: (String) -> Unit,
    onSend: (Participant) -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.ratchet_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = state.input,
            onValueChange = onInputChange,
            label = { Text(stringResource(R.string.ratchet_input_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        state.user1?.let { user ->
            ParticipantCard(stringResource(R.string.ratchet_user_1), user) {
                Button(onClick = { onSend(Participant.USER_1) }, enabled = state.input.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.ratchet_send))
                }
            }
        }
        state.user2?.let { user ->
            ParticipantCard(stringResource(R.string.ratchet_user_2), user) {
                Button(onClick = { onSend(Participant.USER_2) }, enabled = state.canUser2Send && state.input.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.ratchet_send))
                }
                if (!state.canUser2Send) {
                    Text(
                        text = stringResource(R.string.ratchet_waiting),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        LogCard(state.log)
        OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.ratchet_restart))
        }
    }
}

@Composable
private fun ParticipantCard(title: String, user: ParticipantState, actions: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            val none = stringResource(R.string.ratchet_no_chain)
            MonoText(stringResource(R.string.ratchet_root, user.rootKey))
            MonoText(stringResource(R.string.ratchet_ratchet_key, user.ratchetKey))
            MonoText(stringResource(R.string.ratchet_sending, user.sendingChain ?: none, user.sent))
            MonoText(stringResource(R.string.ratchet_receiving, user.receivingChain ?: none, user.received))
            actions()
        }
    }
}

@Composable
private fun LogCard(log: List<LogEntry>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.ratchet_log_title), style = MaterialTheme.typography.titleSmall)
            if (log.isEmpty()) {
                Text(
                    text = stringResource(R.string.ratchet_log_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            log.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider()
                Text(
                    text = stringResource(
                        if (entry.sender == Participant.USER_1) R.string.ratchet_log_from_user_1 else R.string.ratchet_log_from_user_2,
                        entry.readText,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                MonoText(stringResource(R.string.ratchet_log_header, entry.ratchetKey, entry.previousChainLength, entry.messageNumber))
                Text(
                    text = stringResource(if (entry.receiverMadeDhStep) R.string.ratchet_log_dh_step else R.string.ratchet_log_chain_step),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entry.receiverMadeDhStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MonoText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
}

@Preview(showBackground = true)
@Composable
private fun RatchetScreenPreview() {
    val user = ParticipantState("3a:91:0c:5e:77:12:d4:8b", "c2:08:fe:41:9a:6d:30:e5", "5b:e1:02:7c:aa:93:4f:10", 2, null, 0)
    ChatTheme {
        RatchetScreen(
            state = RatchetUiState(
                user1 = user,
                user2 = user.copy(sendingChain = null, receivingChain = "9d:44:b7:20:6e:c1:08:f3", sent = 0, received = 2),
                log = listOf(LogEntry(Participant.USER_1, "c2:08:fe:41:9a:6d:30:e5", 0, 1, 32, receiverMadeDhStep = false, readText = "Hello")),
            ),
            onInputChange = {},
            onSend = {},
            onRestart = {},
        )
    }
}
