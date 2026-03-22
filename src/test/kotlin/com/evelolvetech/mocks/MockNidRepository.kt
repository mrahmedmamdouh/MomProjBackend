package com.evelolvetech.mocks

import com.evelolvetech.data.models.Nid
import com.evelolvetech.data.repository.api.mom.NidRepository
import com.mongodb.client.ClientSession

class MockNidRepository : NidRepository {
    val createdNids = mutableListOf<Nid>()

    override suspend fun createNid(nid: Nid): Boolean {
        createdNids.add(nid)
        return true
    }

    override suspend fun createNid(nid: Nid, session: ClientSession): Boolean {
        createdNids.add(nid)
        return true
    }

    override suspend fun getNidById(id: String): Nid? = null
    override suspend fun updateNid(id: String, imageFront: String?, imageBack: String?): Boolean = true
    override suspend fun deleteNid(id: String): Boolean = true
    override suspend fun deleteNid(id: String, session: ClientSession): Boolean = true
}
