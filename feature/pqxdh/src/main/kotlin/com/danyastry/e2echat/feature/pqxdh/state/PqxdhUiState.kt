package com.danyastry.e2echat.feature.pqxdh.state

import com.danyastry.e2echat.feature.pqxdh.model.NewChat
import com.danyastry.e2echat.feature.pqxdh.model.QuantumAttack
import com.danyastry.e2echat.feature.pqxdh.model.Recording

data class PqxdhUiState(
    val recordings: List<Recording> = emptyList(),
    val attacks: List<QuantumAttack> = emptyList(),
    val chats: List<NewChat> = emptyList(),
    val oneTimeKemPreKeysLeft: Int = 0,
)
