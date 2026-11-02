package com.danyastry.e2echat.core.protocol.x3dh

class X3dhResult(val sharedSecret: ByteArray, val associatedData: ByteArray, val dhOutputs: List<ByteArray>)
