package com.danyastry.e2echat.feature.security.ui

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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.danyastry.e2echat.feature.security.AttackerView
import com.danyastry.e2echat.feature.security.Delivery
import com.danyastry.e2echat.feature.security.DeliveryResult
import com.danyastry.e2echat.feature.security.R
import com.danyastry.e2echat.feature.security.RecordedMessage
import com.danyastry.e2echat.feature.security.TimelineEntry

@Composable
fun SecurityRoute(
    modifier: Modifier = Modifier,
    viewModel: SecurityViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SecurityScreen(
        state = state,
        actions = SecurityActions(
            onDemoChange = viewModel::onDemoChange,
            onConverse = viewModel::onConverse,
            onStealAndAttack = viewModel::onStealAndAttack,
            onResetForwardSecrecy = viewModel::onResetForwardSecrecy,
            onSteal = viewModel::onSteal,
            onUser1Sends = viewModel::onUser1Sends,
            onUser2Replies = viewModel::onUser2Replies,
            onResetHealing = viewModel::onResetHealing,
            onSendThree = viewModel::onSendThree,
            onDeliverOutOfOrder = viewModel::onDeliverOutOfOrder,
            onReplay = viewModel::onReplay,
            onTamper = viewModel::onTamper,
        ),
        modifier = modifier,
    )
}

data class SecurityActions(
    val onDemoChange: (Demo) -> Unit = {},
    val onConverse: () -> Unit = {},
    val onStealAndAttack: () -> Unit = {},
    val onResetForwardSecrecy: () -> Unit = {},
    val onSteal: () -> Unit = {},
    val onUser1Sends: () -> Unit = {},
    val onUser2Replies: () -> Unit = {},
    val onResetHealing: () -> Unit = {},
    val onSendThree: () -> Unit = {},
    val onDeliverOutOfOrder: () -> Unit = {},
    val onReplay: () -> Unit = {},
    val onTamper: () -> Unit = {},
)

@Composable
fun SecurityScreen(
    state: SecurityUiState,
    actions: SecurityActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DemoSelector(state.demo, actions.onDemoChange)
        when (state.demo) {
            Demo.FORWARD_SECRECY -> ForwardSecrecySection(state, actions)
            Demo.HEALING -> HealingSection(state, actions)
            Demo.REORDER -> ReorderSection(state, actions)
        }
    }
}

@Composable
private fun DemoSelector(selected: Demo, onDemoChange: (Demo) -> Unit) {
    val labels = mapOf(
        Demo.FORWARD_SECRECY to R.string.security_tab_forward,
        Demo.HEALING to R.string.security_tab_healing,
        Demo.REORDER to R.string.security_tab_reorder,
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        Demo.entries.forEachIndexed { index, demo ->
            SegmentedButton(
                selected = demo == selected,
                onClick = { onDemoChange(demo) },
                shape = SegmentedButtonDefaults.itemShape(index, Demo.entries.size),
            ) {
                Text(stringResource(labels.getValue(demo)))
            }
        }
    }
}

@Composable
private fun ForwardSecrecySection(state: SecurityUiState, actions: SecurityActions) {
    Intro(R.string.security_forward_title, R.string.security_forward_intro)
    Button(onClick = actions.onConverse, enabled = !state.hasConversation, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.security_converse))
    }
    if (state.hasConversation) Hint(stringResource(R.string.security_conversed))
    Button(onClick = actions.onStealAndAttack, enabled = state.hasConversation && state.stolenAttack == null, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.security_steal_and_attack))
    }
    state.stolenAttack?.let { messages ->
        ResultCard(messages) { RecordedRow(it) }
        Hint(stringResource(R.string.security_forward_summary))
    }
    ResetButton(actions.onResetForwardSecrecy)
}

@Composable
private fun RecordedRow(message: RecordedMessage) {
    Text(text = stringResource(R.string.security_recorded, message.number, message.text), style = MaterialTheme.typography.bodyMedium)
    val read = message.attackerRead
    Verdict(
        text = if (read == null) stringResource(R.string.security_locked) else stringResource(R.string.security_read, read),
        isSafe = read == null,
    )
}

@Composable
private fun HealingSection(state: SecurityUiState, actions: SecurityActions) {
    Intro(R.string.security_healing_title, R.string.security_healing_intro)
    Button(onClick = actions.onSteal, enabled = !state.isStolen, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.security_steal))
    }
    if (state.isStolen) Hint(stringResource(R.string.security_stolen))
    Button(onClick = actions.onUser1Sends, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.security_user1_sends))
    }
    Button(onClick = actions.onUser2Replies, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.security_user2_replies))
    }
    if (state.timeline.isEmpty()) {
        Hint(stringResource(R.string.security_healing_hint))
    } else {
        ResultCard(state.timeline) { TimelineRow(it) }
    }
    ResetButton(actions.onResetHealing)
}

@Composable
private fun TimelineRow(entry: TimelineEntry) {
    Text(text = entry.text, style = MaterialTheme.typography.bodyMedium)
    val (text, isSafe) = when (entry.view) {
        AttackerView.NOT_STOLEN_YET -> R.string.security_view_not_stolen to null
        AttackerView.READ -> R.string.security_view_read to false
        AttackerView.LOCKED_OUT -> R.string.security_view_locked to true
        AttackerView.NOT_TARGETED -> R.string.security_view_not_targeted to null
    }
    Verdict(stringResource(text), isSafe)
}

@Composable
private fun ReorderSection(state: SecurityUiState, actions: SecurityActions) {
    Intro(R.string.security_reorder_title, R.string.security_reorder_intro)
    Button(onClick = actions.onSendThree, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.security_send_three))
    }
    if (state.heldCount > 0) Hint(pluralStringResource(R.plurals.security_held, state.heldCount, state.heldCount))
    Button(onClick = actions.onDeliverOutOfOrder, enabled = state.heldCount > 0, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.security_deliver))
    }
    val delivered = state.deliveries.isNotEmpty()
    Button(onClick = actions.onReplay, enabled = delivered && !state.hasReplayed, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.security_replay))
    }
    Button(onClick = actions.onTamper, enabled = delivered && !state.hasTampered, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.security_tamper))
    }
    if (delivered) ResultCard(state.deliveries) { DeliveryRow(it) }
}

@Composable
private fun DeliveryRow(result: DeliveryResult) {
    val text = result.text
    Verdict(
        text = if (result.delivery == Delivery.READ && text != null) {
            stringResource(R.string.security_delivery_read, result.label, text)
        } else {
            stringResource(R.string.security_delivery_rejected, result.label)
        },
        isSafe = if (result.delivery == Delivery.REJECTED) true else null,
    )
}

@Composable
private fun Intro(title: Int, intro: Int) {
    Text(text = stringResource(title), style = MaterialTheme.typography.titleMedium)
    Hint(stringResource(intro))
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
        Text(stringResource(R.string.security_reset))
    }
}

@Preview(showBackground = true)
@Composable
private fun SecurityScreenPreview() {
    ChatTheme {
        SecurityScreen(
            state = SecurityUiState(
                hasConversation = true,
                stolenAttack = listOf(RecordedMessage(1, "Message 1 from User 1", null), RecordedMessage(2, "Message 2 from User 1", null)),
            ),
            actions = SecurityActions(),
        )
    }
}
