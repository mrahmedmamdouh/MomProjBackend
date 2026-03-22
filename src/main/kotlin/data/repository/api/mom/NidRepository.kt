package com.evelolvetech.data.repository.api.mom

import com.evelolvetech.data.models.Nid
import com.mongodb.client.ClientSession

interface NidRepository {
    suspend fun createNid(nid: Nid): Boolean
    suspend fun createNid(nid: Nid, session: ClientSession): Boolean
    suspend fun getNidById(id: String): Nid?
    suspend fun updateNid(id: String, imageFront: String?, imageBack: String?): Boolean
    suspend fun deleteNid(id: String): Boolean
    suspend fun deleteNid(id: String, session: ClientSession): Boolean
}
