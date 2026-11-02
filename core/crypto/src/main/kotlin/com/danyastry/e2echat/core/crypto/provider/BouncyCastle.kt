package com.danyastry.e2echat.core.crypto.provider

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Provider

object BouncyCastle {

    val provider: Provider = BouncyCastleProvider()

    @Suppress("DEPRECATION")
    val version: String
        get() = provider.version.toString()

    fun supports(type: String, algorithm: String): Boolean = provider.getService(type, algorithm) != null
}
