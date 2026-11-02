package com.danyastry.e2echat.feature.pqxdh.model

data class QuantumAttack(val handshake: Handshake, val dhOutputsBroken: Int, val attackerRead: List<String?>) {
    val isReadable: Boolean
        get() = attackerRead.all { it != null }
}
