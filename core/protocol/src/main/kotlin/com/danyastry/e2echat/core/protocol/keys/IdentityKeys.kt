package com.danyastry.e2echat.core.protocol.keys

import com.danyastry.e2echat.core.crypto.curve.Ed25519
import com.danyastry.e2echat.core.crypto.curve.KeyPair
import com.danyastry.e2echat.core.crypto.curve.X25519
import java.security.SecureRandom

class IdentityKeys(val dh: KeyPair, val signing: KeyPair) {
    companion object {
        fun generate(random: SecureRandom = SecureRandom()) = IdentityKeys(X25519.generate(random), Ed25519.generate(random))
    }
}
