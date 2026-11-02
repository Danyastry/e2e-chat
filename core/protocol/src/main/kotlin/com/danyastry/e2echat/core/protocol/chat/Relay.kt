package com.danyastry.e2echat.core.protocol.chat


class Relay {

    private val queues = mutableMapOf<String, ArrayDeque<Envelope>>()
    private val relayed = mutableListOf<Envelope>()
    private var nextId = 1

    val log: List<Envelope>
        get() = relayed.toList()

    fun newId(): Int = nextId++

    fun post(envelope: Envelope) {
        relayed += envelope
        queues.getOrPut(envelope.to) { ArrayDeque() }.addLast(envelope)
    }

    fun take(user: String): List<Envelope> {
        val queue = queues[user] ?: return emptyList()
        return queue.toList().also { queue.clear() }
    }

    fun waiting(user: String): Int = queues[user]?.size ?: 0

    fun isWaiting(envelope: Envelope): Boolean = queues[envelope.to]?.contains(envelope) == true
}
