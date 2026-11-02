package com.danyastry.e2echat.core.protocol.x3dh

class InvalidSignedPreKeyException : Exception("The signed prekey is not signed by the identity key")
