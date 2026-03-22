package com.evelolvetech.data.requests

data class CreateRatingRequest(
    val productId: String,
    val rating: Int,
    val title: String?,
    val comment: String?
)

data class UpdateInventoryRequest(
    val onHand: Int?,
    val reserved: Int?
)
