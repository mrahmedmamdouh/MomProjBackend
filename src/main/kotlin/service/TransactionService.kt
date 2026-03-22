package com.evelolvetech.service

import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.TransactionOptions
import com.mongodb.WriteConcern
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class TransactionService(
    private val mongoClient: MongoClient?,
    private val database: MongoDatabase?
) : TransactionServiceInterface {

    override suspend fun <T> withTransaction(
        operation: suspend (ClientSession) -> T
    ): T = withContext(Dispatchers.IO) {
        val session = mongoClient?.startSession() ?: return@withContext operation(null as ClientSession)
        val transactionOptions = TransactionOptions.builder()
            .readPreference(ReadPreference.primary())
            .readConcern(ReadConcern.LOCAL)
            .writeConcern(WriteConcern.MAJORITY)
            .build()

        try {
            session.startTransaction(transactionOptions)
            val result = operation(session)
            session.commitTransaction()
            result
        } catch (e: Exception) {
            session.abortTransaction()
            throw e
        } finally {
            session.close()
        }
    }

    override suspend fun <T> withTransactionAndRollback(
        operation: suspend (ClientSession) -> T,
        rollback: suspend () -> Unit
    ): T = withContext(Dispatchers.IO) {
        val session = mongoClient?.startSession() ?: return@withContext operation(null as ClientSession)

        val transactionOptions = TransactionOptions.builder()
            .readPreference(ReadPreference.primary())
            .readConcern(ReadConcern.LOCAL)
            .writeConcern(WriteConcern.MAJORITY)
            .build()

        try {
            session.startTransaction(transactionOptions)
            val result = operation(session)
            session.commitTransaction()
            result
        } catch (e: Exception) {
            try {
                rollback()
            } catch (rollbackException: Exception) {
                throw rollbackException
            }
            session.abortTransaction()
            throw e
        } finally {
            session.close()
        }
    }
}