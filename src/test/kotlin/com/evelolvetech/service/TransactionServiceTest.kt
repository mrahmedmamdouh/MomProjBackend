package com.evelolvetech.service

import com.mongodb.client.ClientSession
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*

class TransactionServiceTest {

    private lateinit var transactionService: TransactionService
    private lateinit var mockMongoClient: MongoClient
    private lateinit var mockMongoDatabase: MongoDatabase
    private lateinit var mockClientSession: ClientSession

    @BeforeEach
    fun setUp() {
        mockMongoClient = mockk()
        mockMongoDatabase = mockk()
        mockClientSession = mockk(relaxed = true)
        transactionService = TransactionService(mockMongoClient, mockMongoDatabase)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `withTransaction should execute operation and commit when successful`() = runBlocking {
        val expectedResult = "success"
        val operation: suspend (ClientSession) -> String = { expectedResult }

        every { mockMongoClient.startSession() } returns mockClientSession
        every { mockClientSession.startTransaction(any()) } just Runs
        every { mockClientSession.commitTransaction() } just Runs
        every { mockClientSession.close() } just Runs

        val result = transactionService.withTransaction(operation)

        assertEquals(expectedResult, result)
        verify { mockMongoClient.startSession() }
        verify { mockClientSession.startTransaction(any()) }
        verify { mockClientSession.commitTransaction() }
        verify { mockClientSession.close() }
        verify(exactly = 0) { mockClientSession.abortTransaction() }
    }

    @Test
    fun `withTransaction should abort transaction when operation throws exception`() = runBlocking {
        val expectedException = RuntimeException("Test exception")
        val operation: suspend (ClientSession) -> String = { throw expectedException }

        every { mockMongoClient.startSession() } returns mockClientSession
        every { mockClientSession.startTransaction(any()) } just Runs
        every { mockClientSession.abortTransaction() } just Runs
        every { mockClientSession.close() } just Runs

        val thrownException = assertFailsWith<RuntimeException> {
            transactionService.withTransaction(operation)
        }

        assertEquals(expectedException, thrownException)
        verify { mockMongoClient.startSession() }
        verify { mockClientSession.startTransaction(any()) }
        verify { mockClientSession.abortTransaction() }
        verify { mockClientSession.close() }
        verify(exactly = 0) { mockClientSession.commitTransaction() }
    }

    @Test
    fun `withTransaction should handle null session gracefully`() = runBlocking {
        val expectedResult = "success"
        val operation: suspend (ClientSession) -> String = { expectedResult }

        every { mockMongoClient.startSession() } returns null

        val result = transactionService.withTransaction(operation)

        assertEquals(expectedResult, result)
        verify { mockMongoClient.startSession() }
        verify(exactly = 0) { mockClientSession.startTransaction(any()) }
        verify(exactly = 0) { mockClientSession.commitTransaction() }
        verify(exactly = 0) { mockClientSession.abortTransaction() }
        verify(exactly = 0) { mockClientSession.close() }
    }

    @Test
    fun `withTransactionAndRollback should execute operation and commit when successful`() = runBlocking {
        val expectedResult = "success"
        val operation: suspend (ClientSession) -> String = { expectedResult }
        val rollback: suspend () -> Unit = mockk(relaxed = true)

        every { mockMongoClient.startSession() } returns mockClientSession
        every { mockClientSession.startTransaction(any()) } just Runs
        every { mockClientSession.commitTransaction() } just Runs
        every { mockClientSession.close() } just Runs

        val result = transactionService.withTransactionAndRollback(operation, rollback)

        assertEquals(expectedResult, result)
        verify { mockMongoClient.startSession() }
        verify { mockClientSession.startTransaction(any()) }
        verify { mockClientSession.commitTransaction() }
        verify { mockClientSession.close() }
        verify(exactly = 0) { mockClientSession.abortTransaction() }
        coVerify(exactly = 0) { rollback() }
    }

    @Test
    fun `withTransactionAndRollback should execute rollback and abort when operation fails`() = runBlocking {
        val expectedException = RuntimeException("Test exception")
        val operation: suspend (ClientSession) -> String = { throw expectedException }
        val rollback: suspend () -> Unit = mockk(relaxed = true)

        every { mockMongoClient.startSession() } returns mockClientSession
        every { mockClientSession.startTransaction(any()) } just Runs
        every { mockClientSession.abortTransaction() } just Runs
        every { mockClientSession.close() } just Runs
        coEvery { rollback() } just Runs

        val thrownException = assertFailsWith<RuntimeException> {
            transactionService.withTransactionAndRollback(operation, rollback)
        }

        assertEquals(expectedException, thrownException)
        verify { mockMongoClient.startSession() }
        verify { mockClientSession.startTransaction(any()) }
        verify { mockClientSession.abortTransaction() }
        verify { mockClientSession.close() }
        verify(exactly = 0) { mockClientSession.commitTransaction() }
        coVerify { rollback() }
    }

    @Test
    fun `withTransactionAndRollback should prioritize rollback exception over operation exception`() = runBlocking {
        val operationException = RuntimeException("Operation exception")
        val rollbackException = RuntimeException("Rollback exception")
        val operation: suspend (ClientSession) -> String = { throw operationException }
        val rollback: suspend () -> Unit = { throw rollbackException }

        every { mockMongoClient.startSession() } returns mockClientSession
        every { mockClientSession.startTransaction(any()) } just Runs
        every { mockClientSession.abortTransaction() } just Runs
        every { mockClientSession.close() } just Runs

        val thrownException = assertFailsWith<RuntimeException> {
            transactionService.withTransactionAndRollback(operation, rollback)
        }

        assertEquals(rollbackException, thrownException)
        verify { mockMongoClient.startSession() }
        verify { mockClientSession.startTransaction(any()) }
        verify { mockClientSession.abortTransaction() }
        verify { mockClientSession.close() }
        verify(exactly = 0) { mockClientSession.commitTransaction() }
    }

    @Test
    fun `withTransactionAndRollback should handle null session gracefully`() = runBlocking {
        val expectedResult = "success"
        val operation: suspend (ClientSession) -> String = { expectedResult }
        val rollback: suspend () -> Unit = mockk(relaxed = true)

        every { mockMongoClient.startSession() } returns null

        val result = transactionService.withTransactionAndRollback(operation, rollback)

        assertEquals(expectedResult, result)
        verify { mockMongoClient.startSession() }
        verify(exactly = 0) { mockClientSession.startTransaction(any()) }
        verify(exactly = 0) { mockClientSession.commitTransaction() }
        verify(exactly = 0) { mockClientSession.abortTransaction() }
        verify(exactly = 0) { mockClientSession.close() }
        coVerify(exactly = 0) { rollback() }
    }
}
