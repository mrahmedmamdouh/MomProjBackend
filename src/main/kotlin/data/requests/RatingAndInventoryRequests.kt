package com.evelolvetech.data.requests

data class CreateRatingRequest(
    val productId: String,
    val rating: Int,
    val title: String = "",
    val comment: String = ""
)

data class UpdateRatingRequest(
    val rating: Int,
    val title: String? = null,
    val comment: String? = null
)

data class CreateInventoryRequest(
    val skuId: String,
    val onHand: Int
)

data class UpdateInventoryRequest(
    val onHand: Int? = null,
    val reserved: Int? = null
)

data class ReserveInventoryRequest(
    val skuId: String,
    val quantity: Int
)

data class ReleaseInventoryRequest(
    val skuId: String,
    val quantity: Int
)

data class AuthorizePaymentRequest(
    val transactionRef: String
)
