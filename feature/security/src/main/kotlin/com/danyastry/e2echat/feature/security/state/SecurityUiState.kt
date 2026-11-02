package com.danyastry.e2echat.feature.security.state

import com.danyastry.e2echat.feature.security.model.DeliveryResult
import com.danyastry.e2echat.feature.security.model.Demo
import com.danyastry.e2echat.feature.security.model.RecordedMessage
import com.danyastry.e2echat.feature.security.model.TimelineEntry

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
