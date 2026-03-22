package com.evelolvetech.mocks

import com.evelolvetech.util.HashingService
import com.evelolvetech.util.SaltedHash

class MockHashingService : HashingService {
    val verifyCalls = mutableListOf<Pair<String, SaltedHash>>()
    var verifyResult = false

    override fun verify(password: String, saltedHash: SaltedHash): Boolean {
        verifyCalls.add(password to saltedHash)
        return when (password) {
            "password123" -> true
            "wrongpassword" -> false
            else -> verifyResult
        }
    }

    override fun generateSaltedHash(value: String, saltLength: Int): SaltedHash {
        return SaltedHash("mockhash", "mocksalt")
    }
}
