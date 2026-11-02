package com.danyastry.e2echat.core.crypto.cipher

class AuthenticationException : Exception("The message was changed or encrypted with another key")
