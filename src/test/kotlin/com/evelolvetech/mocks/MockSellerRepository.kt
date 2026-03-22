package com.evelolvetech.mocks

import com.evelolvetech.data.models.Seller
import com.evelolvetech.data.repository.api.mom.ecommerce.SellerRepository

class MockSellerRepository : SellerRepository {
    private val sellers = mutableMapOf<String, Seller>()

    init {
        sellers["seller_happy"] = Seller(
            id = "seller_happy",
            name = "Happy Health Store"
        )
        sellers["seller_essentials"] = Seller(
            id = "seller_essentials",
            name = "Essentials Plus"
        )
    }

    override suspend fun createSeller(seller: Seller): Boolean {
        sellers[seller.id] = seller
        return true
    }

    override suspend fun getSellerById(id: String): Seller? {
        return sellers[id]
    }

    override suspend fun getAllSellers(): List<Seller> {
        return sellers.values.toList()
    }

    override suspend fun updateSeller(id: String, name: String?, status: String?): Boolean {
        val seller = sellers[id] ?: return false
        val updatedSeller = seller.copy(
            name = name ?: seller.name
        )
        sellers[id] = updatedSeller
        return true
    }

    override suspend fun deleteSeller(id: String): Boolean {
        return sellers.remove(id) != null
    }

    override suspend fun getActiveSellers(): List<Seller> {
        return sellers.values.toList()
    }
}
