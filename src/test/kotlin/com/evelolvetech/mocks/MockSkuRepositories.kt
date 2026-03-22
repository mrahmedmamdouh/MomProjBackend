package com.evelolvetech.mocks

import com.evelolvetech.data.models.Sku
import com.evelolvetech.data.models.SkuOffer
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuOfferRepository
import com.evelolvetech.data.repository.api.mom.ecommerce.SkuRepository


class MockSkuOfferRepository : SkuOfferRepository {
    private val offers = mutableMapOf<String, SkuOffer>()

    init {
        offers["offer_prn_happy"] = SkuOffer(
            id = "offer_prn_happy",
            skuId = "sku_prenatal_batchA",
            skuRef = "/skus/sku_prenatal_batchA",
            sellerId = "seller_happy",
            sellerRef = "/sellers/seller_happy",
            listPrice = 24.99,
            salePrice = 19.99,
            currency = "USD",
            isActive = true,
            activeFrom = System.currentTimeMillis(),
            activeTo = System.currentTimeMillis() + 86400000
        )
        offers["offer_ess_black"] = SkuOffer(
            id = "offer_ess_black",
            skuId = "sku_essentials_black",
            skuRef = "/skus/sku_essentials_black",
            sellerId = "seller_essentials",
            sellerRef = "/sellers/seller_essentials",
            listPrice = 8.00,
            salePrice = 6.00,
            currency = "USD",
            isActive = true,
            activeFrom = System.currentTimeMillis(),
            activeTo = System.currentTimeMillis() + 86400000
        )
    }

    override suspend fun createSkuOffer(offer: SkuOffer): Boolean {
        offers[offer.id] = offer
        return true
    }

    override suspend fun getSkuOfferById(id: String): SkuOffer? {
        return offers[id]
    }

    override suspend fun getSkuOffersBySkuId(skuId: String): List<SkuOffer> {
        return offers.values.filter { it.skuId == skuId }
    }

    override suspend fun getSkuOffersBySellerId(sellerId: String): List<SkuOffer> {
        return offers.values.filter { it.sellerId == sellerId }
    }

    override suspend fun updateSkuOffer(
        id: String,
        listPrice: Double?,
        salePrice: Double?,
        currency: String?,
        isActive: Boolean?,
        activeFrom: Long?,
        activeTo: Long?
    ): Boolean {
        val offer = offers[id] ?: return false
        val updatedOffer = offer.copy(
            listPrice = listPrice ?: offer.listPrice,
            salePrice = salePrice ?: offer.salePrice,
            currency = currency ?: offer.currency,
            isActive = isActive ?: offer.isActive,
            activeFrom = activeFrom ?: offer.activeFrom,
            activeTo = activeTo ?: offer.activeTo
        )
        offers[id] = updatedOffer
        return true
    }

    override suspend fun deleteSkuOffer(id: String): Boolean {
        return offers.remove(id) != null
    }

    override suspend fun getActiveSkuOffers(): List<SkuOffer> {
        return offers.values.filter { it.isActive }
    }

    override suspend fun getBestOfferForSku(skuId: String): SkuOffer? {
        return offers.values
            .filter { it.skuId == skuId && it.isActive }
            .minByOrNull { it.salePrice }
    }
}

class MockSkuRepository : SkuRepository {
    private val skus = mapOf(
        "sku_prenatal_batchA" to Sku(
            id = "sku_prenatal_batchA",
            productId = "prod_prenatal",
            productRef = "/products/prod_prenatal",
            skuCode = "PRN-BATCH-A",
            title = "Prenatal Vitamins Batch A"
        ),
        "sku_essentials_black" to Sku(
            id = "sku_essentials_black",
            productId = "prod_essentials",
            productRef = "/products/prod_essentials",
            skuCode = "ESS-BLK",
            title = "Baby Essentials Black"
        )
    )

    override suspend fun createSku(sku: Sku): Boolean = true
    override suspend fun getSkuById(id: String): Sku? = skus[id]
    override suspend fun getSkusByProductId(productId: String): List<Sku> = emptyList()
    override suspend fun updateSku(
        id: String,
        skuCode: String?,
        title: String?,
        taxClass: String?,
        isActive: Boolean?
    ): Boolean = true

    override suspend fun deleteSku(id: String): Boolean = true
    override suspend fun getActiveSkus(): List<Sku> = skus.values.toList()
}
