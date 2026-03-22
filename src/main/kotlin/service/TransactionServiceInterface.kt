package com.evelolvetech.service

import com.mongodb.client.ClientSession

interface TransactionServiceInterface {
    suspend fun <T> withTransaction(
        operation: suspend (ClientSession) -> T
    ): T

    suspend fun <T> withTransactionAndRollback(
        operation: suspend (ClientSession) -> T,
        rollback: suspend () -> Unit
    ): T
}
