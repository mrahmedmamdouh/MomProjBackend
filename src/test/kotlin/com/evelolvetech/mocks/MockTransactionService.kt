package com.evelolvetech.mocks

import com.evelolvetech.service.TransactionServiceInterface
import com.mongodb.client.ClientSession
import io.mockk.mockk

class MockTransactionService : TransactionServiceInterface {

    private fun createTestClientSession(): ClientSession = mockk<ClientSession>(relaxed = true)

    override suspend fun <T> withTransaction(operation: suspend (ClientSession) -> T): T {
        val testSession = createTestClientSession()
        return operation(testSession)
    }

    override suspend fun <T> withTransactionAndRollback(
        operation: suspend (ClientSession) -> T,
        rollback: suspend () -> Unit
    ): T {
        val testSession = createTestClientSession()
        return try {
            operation(testSession)
        } catch (e: Exception) {
            rollback()
            throw e
        }
    }
}
