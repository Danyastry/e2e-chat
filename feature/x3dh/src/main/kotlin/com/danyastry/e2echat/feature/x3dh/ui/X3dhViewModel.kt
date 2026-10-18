package com.danyastry.e2echat.feature.x3dh.ui

import androidx.lifecycle.ViewModel
import com.danyastry.e2echat.core.crypto.Fingerprint
import com.danyastry.e2echat.core.crypto.X25519
import com.danyastry.e2echat.core.protocol.Initiation
import com.danyastry.e2echat.core.protocol.InvalidSignedPreKeyException
import com.danyastry.e2echat.core.protocol.KeyServer
import com.danyastry.e2echat.core.protocol.PreKeyBundle
import com.danyastry.e2echat.core.protocol.X3dhResult
import com.danyastry.e2echat.core.protocol.X3dhUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class X3dhViewModel : ViewModel() {

    private val _state = MutableStateFlow(X3dhUiState())
    val state: StateFlow<X3dhUiState> = _state.asStateFlow()

    private var server = KeyServer()
    private var user1 = X3dhUser(USER_1)
    private var user2 = X3dhUser(USER_2)
    private var bundle: PreKeyBundle? = null
    private var initiation: Initiation? = null

    fun onPublish() {
        server = KeyServer()
        user1 = X3dhUser(USER_1)
        user2 = X3dhUser(USER_2)
        bundle = null
        initiation = null
        val keys = user2.publishedKeys()
        server.publish(USER_2, keys)
        _state.update {
            X3dhUiState(
                published = PublishedInfo(Fingerprint.of(keys.identityKey), Fingerprint.of(keys.signedPreKey), keys.signedPreKeySignature.size, keys.oneTimePreKeys.size),
                serverOneTimeLeft = server.oneTimePreKeysLeft(USER_2),
                isServerMalicious = it.isServerMalicious,
                user2OneTimeHeld = user2.oneTimePreKeysHeld,
            )
        }
    }

    fun onMaliciousChange(isMalicious: Boolean) {
        _state.update { it.copy(isServerMalicious = isMalicious) }
    }

    fun onFetch() {
        if (!_state.value.canFetch) return
        val real = server.fetchBundle(USER_2)
        val isForged = _state.value.isServerMalicious
        val delivered = if (isForged) real.withSignedPreKey(X25519.generate().publicKey) else real
        bundle = delivered
        _state.update {
            it.copy(
                bundle = BundleInfo(Fingerprint.of(delivered.signedPreKey), delivered.oneTimePreKeyId, isForged),
                serverOneTimeLeft = server.oneTimePreKeysLeft(USER_2),
            )
        }
    }

    fun onInitiate() {
        val fetched = bundle ?: return
        try {
            val started = user1.initiate(fetched)
            initiation = started
            _state.update { it.copy(isSignatureValid = true, user1 = started.result.toInfo(), headerBytes = started.header.size) }
        } catch (_: InvalidSignedPreKeyException) {
            _state.update { it.copy(isSignatureValid = false) }
        }
    }

    fun onRespond() {
        val started = initiation ?: return
        val result = user2.respond(started.header)
        _state.update { it.copy(user2 = result.toInfo(), user2OneTimeHeld = user2.oneTimePreKeysHeld, handshakes = it.handshakes + 1) }
    }

    fun onStartAgain() {
        bundle = null
        initiation = null
        _state.update { it.copy(bundle = null, isSignatureValid = null, user1 = null, headerBytes = 0, user2 = null) }
    }

    private fun X3dhResult.toInfo() = SecretInfo(dhOutputs.map(Fingerprint::of), Fingerprint.of(sharedSecret))

    private fun PreKeyBundle.withSignedPreKey(key: ByteArray) =
        PreKeyBundle(identityKey, signingKey, signedPreKeyId, key, signedPreKeySignature, oneTimePreKeyId, oneTimePreKey)

    companion object {
        const val USER_1 = "User 1"
        const val USER_2 = "User 2"
    }
}
