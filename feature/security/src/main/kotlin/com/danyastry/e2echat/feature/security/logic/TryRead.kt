package com.danyastry.e2echat.feature.security.logic

import com.danyastry.e2echat.core.protocol.chat.Envelope
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet

fun DoubleRatchet.tryRead(envelope: Envelope): String? = runCatching { decrypt(envelope.message).decodeToString() }.getOrNull()
