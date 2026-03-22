package com.evelolvetech.data.requests

data class CreateSkuRequest(
    val productId: String,
    val skuCode: String,
    val title: String,
    val taxClass: String = "STANDARD"
)

data class UpdateSkuRequest(
    val skuCode: String?,
    val title: String?,
    val taxClass: String?,
    val isActive: Boolean?
)
