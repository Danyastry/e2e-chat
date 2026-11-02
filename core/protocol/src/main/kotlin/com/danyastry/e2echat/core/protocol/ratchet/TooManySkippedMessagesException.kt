package com.danyastry.e2echat.core.protocol.ratchet

class TooManySkippedMessagesException(skipped: Int) : Exception("Refusing to skip $skipped messages")
