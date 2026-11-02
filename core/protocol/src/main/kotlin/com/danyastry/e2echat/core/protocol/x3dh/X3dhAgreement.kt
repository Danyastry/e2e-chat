package com.danyastry.e2echat.core.protocol.x3dh

internal class X3dhAgreement(
    val header: InitialHeader,
    val associatedData: ByteArray,
    val dhOutputs: List<ByteArray>,
)
