package com.evelolvetech.data.requests

data class CreateSellerRequest(
    val name: String
)

data class UpdateSellerRequest(
    val name: String?,
    val status: String?
)
