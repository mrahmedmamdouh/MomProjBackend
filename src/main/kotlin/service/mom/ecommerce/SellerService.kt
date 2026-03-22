package com.evelolvetech.service.mom.ecommerce

import com.evelolvetech.data.models.Seller
import com.evelolvetech.data.repository.api.mom.ecommerce.SellerRepository
import com.evelolvetech.data.requests.CreateSellerRequest
import com.evelolvetech.data.requests.UpdateSellerRequest
import org.bson.types.ObjectId

class SellerService(
    private val sellerRepository: SellerRepository
) {
    suspend fun createSeller(request: CreateSellerRequest): Boolean {
        val seller = Seller(
            id = ObjectId().toString(),
            name = request.name
        )
        return sellerRepository.createSeller(seller)
    }

    suspend fun getSellerById(id: String): Seller? {
        return sellerRepository.getSellerById(id)
    }

    suspend fun getAllSellers(): List<Seller> {
        return sellerRepository.getAllSellers()
    }

    suspend fun getActiveSellers(): List<Seller> {
        return sellerRepository.getActiveSellers()
    }

    suspend fun updateSeller(id: String, request: UpdateSellerRequest): Boolean {
        return sellerRepository.updateSeller(id, request.name, request.status)
    }

    suspend fun deleteSeller(id: String): Boolean {
        return sellerRepository.deleteSeller(id)
    }

    fun validateCreateSellerRequest(request: CreateSellerRequest): ValidationEvent {
        if (request.name.isBlank()) {
            return ValidationEvent.ErrorFieldEmpty
        }
        return ValidationEvent.Success
    }

    sealed class ValidationEvent {
        object ErrorFieldEmpty : ValidationEvent()
        object Success : ValidationEvent()
    }
}
