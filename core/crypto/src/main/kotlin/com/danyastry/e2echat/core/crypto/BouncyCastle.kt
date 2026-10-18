package com.danyastry.e2echat.core.crypto

import java.security.Provider
import org.bouncycastle.jce.provider.BouncyCastleProvider

object BouncyCastle {

    val provider: Provider = BouncyCastleProvider()

    @Suppress("DEPRECATION")
    val version: String
        get() = provider.version.toString()

    fun supports(type: String, algorithm: String): Boolean = provider.getService(type, algorithm) != null
}
