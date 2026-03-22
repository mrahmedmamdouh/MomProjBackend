package com.evelolvetech.data.requests

data class UpdatePersonaFieldRequest(
    val field: String,
    val value: String
)

data class ExtractPersonaRequest(
    val message: String
)

data class FormCirclesRequest(
    val numClusters: Int = 5,
    val maxCircleSize: Int = 7
)

data class FormCirclesFromClusterRequest(
    val clusterId: String,
    val maxCircleSize: Int = 7
)
