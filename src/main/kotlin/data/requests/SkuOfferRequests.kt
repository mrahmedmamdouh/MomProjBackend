package com.evelolvetech.data.requests

data class CreateSkuOfferRequest(
    val skuId: String,
    val sellerId: String,
    val listPrice: Double,
    val salePrice: Double,
    val currency: String?,
    val activeFrom: Long = System.currentTimeMillis(),
    val activeTo: Long? = System.currentTimeMillis() + 86400000
)

data class UpdateSkuOfferRequest(
    val listPrice: Double?,
    val salePrice: Double?,
    val currency: String?,
    val isActive: Boolean?,
    val activeFrom: Long?,
    val activeTo: Long?
)
