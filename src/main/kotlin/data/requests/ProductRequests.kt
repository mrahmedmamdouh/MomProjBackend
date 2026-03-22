package com.evelolvetech.data.requests

data class CreateProductRequest(
    val name: String,
    val slug: String,
    val description: String,
    val defaultSellerId: String,
    val categoryIds: List<String>,
    val minSessionsToPurchase: Int = 0
)

data class UpdateProductRequest(
    val name: String?,
    val slug: String?,
    val description: String?,
    val status: String?,
    val defaultSellerId: String?,
    val categoryIds: List<String>?,
    val minSessionsToPurchase: Int?
)
