package com.danyastry.e2echat.feature.security.ui

import androidx.lifecycle.ViewModel
import com.danyastry.e2echat.feature.security.ForwardSecrecyDemo
import com.danyastry.e2echat.feature.security.HealingDemo
import com.danyastry.e2echat.feature.security.ReorderDemo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SecurityViewModel : ViewModel() {

    private var forwardSecrecy = ForwardSecrecyDemo()
    private var healing = HealingDemo()
    private var reorder = ReorderDemo()

    private val _state = MutableStateFlow(SecurityUiState())
    val state: StateFlow<SecurityUiState> = _state.asStateFlow()

    fun onDemoChange(demo: Demo) {
        _state.update { it.copy(demo = demo) }
    }

    fun onConverse() {
        if (_state.value.hasConversation) return
        forwardSecrecy.converse()
        _state.update { it.copy(hasConversation = true) }
    }

    fun onStealAndAttack() {
        if (!_state.value.hasConversation) return
        _state.update { it.copy(stolenAttack = forwardSecrecy.stealAndAttack()) }
    }

    fun onResetForwardSecrecy() {
        forwardSecrecy = ForwardSecrecyDemo()
        _state.update { it.copy(hasConversation = false, stolenAttack = null) }
    }

    fun onSteal() {
        if (healing.isStolen) return
        healing.steal()
        _state.update { it.copy(isStolen = true) }
    }

    fun onUser1Sends() {
        val entry = healing.user1Sends()
        _state.update { it.copy(timeline = it.timeline + entry) }
    }

    fun onUser2Replies() {
        val entry = healing.user2Replies()
        _state.update { it.copy(timeline = it.timeline + entry) }
    }

    fun onResetHealing() {
        healing = HealingDemo()
        _state.update { it.copy(isStolen = false, timeline = emptyList()) }
    }

    fun onSendThree() {
        reorder = ReorderDemo()
        reorder.sendThree()
        _state.update { it.copy(heldCount = reorder.heldCount, deliveries = emptyList(), hasReplayed = false, hasTampered = false) }
    }

    fun onDeliverOutOfOrder() {
        if (_state.value.heldCount == 0) return
        val results = reorder.deliverOutOfOrder()
        _state.update { it.copy(heldCount = 0, deliveries = results) }
    }

    fun onReplay() {
        if (_state.value.deliveries.isEmpty() || _state.value.hasReplayed) return
        val result = reorder.replay()
        _state.update { it.copy(deliveries = it.deliveries + result, hasReplayed = true) }
    }

    fun onTamper() {
        if (_state.value.deliveries.isEmpty() || _state.value.hasTampered) return
        val results = reorder.tamper()
        _state.update { it.copy(deliveries = it.deliveries + results, hasTampered = true) }
    }
}
