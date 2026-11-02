package com.danyastry.e2echat.core.protocol.pqxdh

class InvalidKemPreKeyException : Exception("The ML-KEM prekey is not signed by the identity key")
