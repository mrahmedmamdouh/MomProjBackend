package com.evelolvetech.util

sealed class UserRegistrationException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NidCreationException(message: String = "Failed to create NID record", cause: Throwable? = null) : UserRegistrationException(message, cause)

class MomCreationException(message: String = "Failed to create Mom record", cause: Throwable? = null) : UserRegistrationException(message, cause)

class MomAuthCreationException(message: String = "Failed to create Mom authentication record", cause: Throwable? = null) : UserRegistrationException(message, cause)

class DoctorCreationException(message: String = "Failed to create Doctor record", cause: Throwable? = null) : UserRegistrationException(message, cause)

class DoctorAuthCreationException(message: String = "Failed to create Doctor authentication record", cause: Throwable? = null) : UserRegistrationException(message, cause)

class UserEntryCreationException(message: String = "Failed to create User entry record", cause: Throwable? = null) : UserRegistrationException(message, cause)

class TransactionException(message: String, cause: Throwable? = null) : Exception(message, cause)

sealed class CartOperationException(message: String, cause: Throwable? = null) : Exception(message, cause)

class CartUpdateException(message: String = "Failed to update cart", cause: Throwable? = null) : CartOperationException(message, cause)

class CartItemRemovalException(message: String = "Failed to remove cart item", cause: Throwable? = null) : CartOperationException(message, cause)

class CartClearException(message: String = "Failed to clear cart", cause: Throwable? = null) : CartOperationException(message, cause)
