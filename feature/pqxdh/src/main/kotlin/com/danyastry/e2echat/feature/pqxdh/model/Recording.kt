package com.danyastry.e2echat.feature.pqxdh.model

data class Recording(val handshake: Handshake, val bundleBytes: Int, val headerBytes: Int, val messages: List<String>)
