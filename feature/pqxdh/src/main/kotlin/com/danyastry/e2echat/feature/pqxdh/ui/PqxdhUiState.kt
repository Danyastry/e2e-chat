package com.danyastry.e2echat.feature.pqxdh.ui

import com.danyastry.e2echat.feature.pqxdh.NewChat
import com.danyastry.e2echat.feature.pqxdh.QuantumAttack
import com.danyastry.e2echat.feature.pqxdh.Recording

data class PqxdhUiState(
    val recordings: List<Recording> = emptyList(),
    val attacks: List<QuantumAttack> = emptyList(),
    val chats: List<NewChat> = emptyList(),
    val oneTimeKemPreKeysLeft: Int = 0,
)
