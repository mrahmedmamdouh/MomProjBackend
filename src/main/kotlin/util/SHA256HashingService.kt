package com.evelolvetech.util

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SHA256HashingService : HashingService {

    override fun generateSaltedHash(value: String, saltLength: Int): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
        val hash = hash(value, salt)
        return SaltedHash(
            hash = hash,
            salt = bytesToHex(salt)
        )
    }

    override fun verify(value: String, saltedHash: SaltedHash): Boolean {
        return hash(value, hexToBytes(saltedHash.salt)) == saltedHash.hash
    }

    private fun hash(value: String, salt: ByteArray): String {
        val spec = PBEKeySpec(value.toCharArray(), salt, 120_000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return bytesToHex(hash)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
