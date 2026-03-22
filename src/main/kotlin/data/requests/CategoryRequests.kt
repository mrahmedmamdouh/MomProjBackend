package com.evelolvetech.data.requests

data class CreateCategoryRequest(
    val name: String,
    val slug: String? = null
)

data class UpdateCategoryRequest(
    val name: String?,
    val slug: String?
)
