package com.danyastry.e2echat.feature.security.ui

import com.danyastry.e2echat.feature.security.DeliveryResult
import com.danyastry.e2echat.feature.security.RecordedMessage
import com.danyastry.e2echat.feature.security.TimelineEntry

enum class Demo { FORWARD_SECRECY, HEALING, REORDER }

data class SecurityUiState(
    val demo: Demo = Demo.FORWARD_SECRECY,
    val hasConversation: Boolean = false,
    val stolenAttack: List<RecordedMessage>? = null,
    val isStolen: Boolean = false,
    val timeline: List<TimelineEntry> = emptyList(),
    val heldCount: Int = 0,
    val deliveries: List<DeliveryResult> = emptyList(),
    val hasReplayed: Boolean = false,
    val hasTampered: Boolean = false,
)
