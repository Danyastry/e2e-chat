package com.danyastry.e2echat.feature.x3dh.state

data class X3dhUiState(
    val published: PublishedInfo? = null,
    val serverOneTimeLeft: Int = 0,
    val isServerMalicious: Boolean = false,
    val bundle: BundleInfo? = null,
    val isSignatureValid: Boolean? = null,
    val user1: SecretInfo? = null,
    val headerBytes: Int = 0,
    val user2: SecretInfo? = null,
    val user2OneTimeHeld: Int = 0,
    val handshakes: Int = 0,
) {
    val canFetch: Boolean
        get() = published != null && bundle == null

    val canInitiate: Boolean
        get() = bundle != null && isSignatureValid == null

    val canRespond: Boolean
        get() = user1 != null && user2 == null

    val secretsMatch: Boolean?
        get() = if (user1 == null || user2 == null) null else user1.sharedSecret == user2.sharedSecret

    val canStartAgain: Boolean
        get() = user2 != null || isSignatureValid == false
}
