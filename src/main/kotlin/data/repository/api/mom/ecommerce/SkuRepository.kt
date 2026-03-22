package com.evelolvetech.data.repository.api.mom.ecommerce

import com.evelolvetech.data.models.Sku

interface SkuRepository {
    suspend fun createSku(sku: Sku): Boolean
    suspend fun getSkuById(id: String): Sku?
    suspend fun getSkusByProductId(productId: String): List<Sku>
    suspend fun updateSku(id: String, skuCode: String?, title: String?, taxClass: String?, isActive: Boolean?): Boolean
    suspend fun deleteSku(id: String): Boolean
    suspend fun getActiveSkus(): List<Sku>
}
