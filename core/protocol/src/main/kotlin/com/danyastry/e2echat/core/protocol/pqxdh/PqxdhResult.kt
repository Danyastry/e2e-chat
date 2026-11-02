package com.danyastry.e2echat.core.protocol.pqxdh

class PqxdhResult(val sharedSecret: ByteArray, val associatedData: ByteArray, val dhOutputs: List<ByteArray>, val kemSecret: ByteArray)
