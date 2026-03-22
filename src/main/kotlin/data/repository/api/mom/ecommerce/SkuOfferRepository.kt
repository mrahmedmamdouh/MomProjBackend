package com.evelolvetech.data.repository.api.mom.ecommerce

import com.evelolvetech.data.models.SkuOffer

interface SkuOfferRepository {
    suspend fun createSkuOffer(skuOffer: SkuOffer): Boolean
    suspend fun getSkuOfferById(id: String): SkuOffer?
    suspend fun getSkuOffersBySkuId(skuId: String): List<SkuOffer>
    suspend fun getSkuOffersBySellerId(sellerId: String): List<SkuOffer>
    suspend fun updateSkuOffer(
        id: String,
        listPrice: Double?,
        salePrice: Double?,
        currency: String?,
        isActive: Boolean?,
        activeFrom: Long?,
        activeTo: Long?
    ): Boolean

    suspend fun deleteSkuOffer(id: String): Boolean
    suspend fun getActiveSkuOffers(): List<SkuOffer>
    suspend fun getBestOfferForSku(skuId: String): SkuOffer?
}
