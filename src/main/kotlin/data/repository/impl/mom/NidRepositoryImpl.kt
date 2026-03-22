package com.evelolvetech.data.repository.impl.mom

import com.evelolvetech.data.models.Nid
import com.evelolvetech.data.repository.api.mom.NidRepository
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class NidRepositoryImpl(
    db: MongoDatabase
) : NidRepository {

    private val nids: MongoCollection<Nid> = db.getCollection<Nid>()

    override suspend fun createNid(nid: Nid): Boolean {
        return try {
            nids.insertOne(nid)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createNid(nid: Nid, session: ClientSession): Boolean {
        return try {
            nids.insertOne(session, nid)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getNidById(id: String): Nid? {
        return nids.findOne(Nid::id eq id)
    }

    override suspend fun updateNid(id: String, imageFront: String?, imageBack: String?): Boolean {
        return try {
            val updates = mutableListOf<Bson>()
            imageFront?.let { updates.add(Updates.set(Nid::imageFront.name, it)) }
            imageBack?.let { updates.add(Updates.set(Nid::imageBack.name, it)) }

            if (updates.isNotEmpty()) {
                val result = nids.updateOne(
                    Nid::id eq id,
                    Updates.combine(updates)
                )
                result.modifiedCount > 0
            } else false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteNid(id: String): Boolean {
        return try {
            val result = nids.deleteOne(Nid::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteNid(id: String, session: ClientSession): Boolean {
        return try {
            val result = nids.deleteOne(session, Nid::id eq id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }
}
