package com.evelolvetech.data.repository.api.admin

import com.evelolvetech.data.models.venue.ApprovedVenue
import com.evelolvetech.data.models.venue.VenueReview

interface VenueRepository {
    suspend fun createVenue(venue: ApprovedVenue): Boolean
    suspend fun getVenueById(id: String): ApprovedVenue?
    suspend fun getActiveVenues(page: Int = 0, size: Int = 50): List<ApprovedVenue>
    suspend fun getVenuesByCity(city: String, page: Int = 0, size: Int = 50): List<ApprovedVenue>
    suspend fun getVenuesByType(venueType: String, page: Int = 0, size: Int = 50): List<ApprovedVenue>
    suspend fun getVenuesByCityAndType(city: String, venueType: String): List<ApprovedVenue>
    suspend fun searchVenues(query: String, page: Int = 0, size: Int = 50): List<ApprovedVenue>
    suspend fun getAllVenues(page: Int = 0, size: Int = 50): List<ApprovedVenue>
    suspend fun updateVenue(id: String, venue: ApprovedVenue): Boolean
    suspend fun updateVenueStatus(id: String, status: String): Boolean
    suspend fun deleteVenue(id: String): Boolean
    suspend fun countByCity(city: String): Long
    suspend fun countByStatus(status: String): Long
    suspend fun createReview(review: VenueReview): Boolean
    suspend fun getReviewsByVenueId(venueId: String): List<VenueReview>
}
