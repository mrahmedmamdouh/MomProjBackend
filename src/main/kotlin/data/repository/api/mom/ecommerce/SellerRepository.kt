package com.evelolvetech.data.repository.api.mom.ecommerce

import com.evelolvetech.data.models.Seller

interface SellerRepository {
    suspend fun createSeller(seller: Seller): Boolean
    suspend fun getSellerById(id: String): Seller?
    suspend fun getAllSellers(): List<Seller>
    suspend fun updateSeller(id: String, name: String?, status: String?): Boolean
    suspend fun deleteSeller(id: String): Boolean
    suspend fun getActiveSellers(): List<Seller>
}
