package com.danyastry.e2echat.feature.pqxdh.ui

import androidx.lifecycle.ViewModel
import com.danyastry.e2echat.feature.pqxdh.HarvestDemo
import com.danyastry.e2echat.feature.pqxdh.PreKeyDemo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PqxdhViewModel : ViewModel() {

    private var harvest = HarvestDemo()
    private var preKeys = PreKeyDemo()

    private val _state = MutableStateFlow(PqxdhUiState(oneTimeKemPreKeysLeft = preKeys.oneTimeKemPreKeysLeft))
    val state: StateFlow<PqxdhUiState> = _state.asStateFlow()

    fun onRecord() {
        if (harvest.isRecorded) return
        val recordings = harvest.record()
        _state.update { it.copy(recordings = recordings) }
    }

    fun onAttack() {
        if (!harvest.isRecorded || _state.value.attacks.isNotEmpty()) return
        val attacks = harvest.attack()
        _state.update { it.copy(attacks = attacks) }
    }

    fun onResetHarvest() {
        harvest = HarvestDemo()
        _state.update { it.copy(recordings = emptyList(), attacks = emptyList()) }
    }

    fun onStartChat() {
        val chat = preKeys.startChat()
        _state.update { it.copy(chats = it.chats + chat, oneTimeKemPreKeysLeft = chat.oneTimeKemPreKeysLeft) }
    }

    fun onResetPreKeys() {
        preKeys = PreKeyDemo()
        _state.update { it.copy(chats = emptyList(), oneTimeKemPreKeysLeft = preKeys.oneTimeKemPreKeysLeft) }
    }
}
