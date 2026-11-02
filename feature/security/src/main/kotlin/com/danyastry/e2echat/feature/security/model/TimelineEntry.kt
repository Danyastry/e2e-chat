package com.danyastry.e2echat.feature.security.model

data class TimelineEntry(val from: String, val text: String, val view: AttackerView, val attackerRead: String?)
