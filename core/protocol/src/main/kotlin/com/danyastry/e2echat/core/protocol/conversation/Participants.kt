package com.danyastry.e2echat.core.protocol.conversation

object Participants {
    const val USER_1 = "User 1"
    const val USER_2 = "User 2"

    fun other(name: String): String = if (name == USER_1) USER_2 else USER_1
}
