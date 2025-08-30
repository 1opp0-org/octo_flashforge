package net.amazingdomain.octo.core.networking

interface ListenerProcessor {
    suspend fun process(input: String): String
}