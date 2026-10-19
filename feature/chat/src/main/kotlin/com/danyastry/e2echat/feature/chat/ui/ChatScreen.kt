package com.danyastry.e2echat.feature.chat.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.danyastry.e2echat.feature.chat.R

@Composable
fun ChatRoute(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ChatScreen(
        state = state,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::onSend,
        onOnlineChange = viewModel::onOnlineChange,
        modifier = modifier,
    )
}

@Composable
fun ChatScreen(
    state: ChatUiState,
    onInputChange: (Person, String) -> Unit,
    onSend: (Person) -> Unit,
    onOnlineChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.chat_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PhoneCard(
            title = stringResource(R.string.chat_user_1),
            bubbles = state.user1,
            input = state.user1Input,
            canSend = true,
            onInputChange = { onInputChange(Person.USER_1, it) },
            onSend = { onSend(Person.USER_1) },
        )
        ServerCard(state)
        PhoneCard(
            title = stringResource(R.string.chat_user_2),
            bubbles = state.user2,
            input = state.user2Input,
            canSend = state.isUser2Online,
            onInputChange = { onInputChange(Person.USER_2, it) },
            onSend = { onSend(Person.USER_2) },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.chat_online), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = state.isUser2Online, onCheckedChange = onOnlineChange)
            }
            if (!state.isUser2Online) {
                Text(
                    text = stringResource(R.string.chat_offline_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PhoneCard(
    title: String,
    bubbles: List<Bubble>,
    input: String,
    canSend: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    extra: @Composable ColumnScope.() -> Unit = {},
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            extra()
            if (bubbles.isEmpty()) {
                Text(
                    text = stringResource(R.string.chat_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            bubbles.forEach { MessageBubble(it) }
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                label = { Text(stringResource(R.string.chat_input_label)) },
                singleLine = true,
                enabled = canSend,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = onSend, enabled = canSend && input.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.chat_send))
            }
        }
    }
}

@Composable
private fun MessageBubble(bubble: Bubble) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (bubble.isMine) Arrangement.End else Arrangement.Start) {
        Surface(
            color = if (bubble.isMine) colors.primary else colors.surface,
            contentColor = if (bubble.isMine) colors.onPrimary else colors.onSurface,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            Text(text = bubble.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun ServerCard(state: ChatUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.chat_server_title), style = MaterialTheme.typography.titleMedium)
            if (state.waitingForUser2 > 0) {
                Text(
                    text = pluralStringResource(R.plurals.chat_waiting_count, state.waitingForUser2, state.waitingForUser2),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (state.server.isEmpty()) {
                Text(
                    text = stringResource(R.string.chat_server_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.server.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider()
                val from = stringResource(if (entry.from == Person.USER_1) R.string.chat_user_1 else R.string.chat_user_2)
                val to = stringResource(if (entry.from == Person.USER_1) R.string.chat_user_2 else R.string.chat_user_1)
                val kind = stringResource(if (entry.kind == EnvelopeKind.PRE_KEY) R.string.chat_kind_prekey else R.string.chat_kind_message)
                Text(text = stringResource(R.string.chat_server_entry, entry.id, from, to, kind), style = MaterialTheme.typography.bodyMedium)
                MonoText(stringResource(R.string.chat_server_header, entry.ratchetKey, entry.previousChainLength, entry.messageNumber, entry.bytes))
                MonoText(stringResource(R.string.chat_server_preview, entry.preview))
                if (entry.isWaiting) {
                    Text(
                        text = stringResource(R.string.chat_waiting),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
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
private fun ChatScreenPreview() {
    ChatTheme {
        ChatScreen(
            state = ChatUiState(
                user1 = listOf(Bubble("Hi, are you there?", isMine = true), Bubble("Yes!", isMine = false)),
                user2 = listOf(Bubble("Hi, are you there?", isMine = false), Bubble("Yes!", isMine = true)),
                server = listOf(
                    ServerEntry(2, Person.USER_2, EnvelopeKind.MESSAGE, "ba:e9:78:63", 0, 0, 72, "9f2c11d0a4b7e3f6c1d2e3f4", isWaiting = false),
                    ServerEntry(1, Person.USER_1, EnvelopeKind.PRE_KEY, "22:6f:a1:f8", 0, 0, 144, "5b e1 02 7c", isWaiting = false),
                ),
            ),
            onInputChange = { _, _ -> },
            onSend = {},
            onOnlineChange = {},
        )
    }
}
