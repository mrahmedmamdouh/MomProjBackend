package com.evelolvetech.data.repository.impl.admin

import com.evelolvetech.data.models.admin.*
import com.evelolvetech.data.models.venue.ApprovedVenue
import com.evelolvetech.data.models.venue.VenueReview
import com.evelolvetech.data.models.venue.VenueStatus
import com.evelolvetech.data.repository.api.admin.*
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.*

class VenueRepositoryImpl(db: MongoDatabase) : VenueRepository {
    private val venues: MongoCollection<ApprovedVenue> = db.getCollection("approved_venues")
    private val reviews: MongoCollection<VenueReview> = db.getCollection("venue_reviews")

    override suspend fun createVenue(venue: ApprovedVenue) = try { venues.insertOne(venue); true } catch (e: Exception) { false }
    override suspend fun getVenueById(id: String) = venues.findOne(ApprovedVenue::id eq id)
    override suspend fun getActiveVenues(page: Int, size: Int) =
        venues.find(ApprovedVenue::status eq VenueStatus.ACTIVE.name).sort(descending(ApprovedVenue::name)).skip(page * size).limit(size).toList()
    override suspend fun getVenuesByCity(city: String, page: Int, size: Int) =
        venues.find(and(ApprovedVenue::city regex Regex(city, RegexOption.IGNORE_CASE), ApprovedVenue::status eq VenueStatus.ACTIVE.name)).skip(page * size).limit(size).toList()
    override suspend fun getVenuesByType(venueType: String, page: Int, size: Int) =
        venues.find(and(ApprovedVenue::venueType eq venueType, ApprovedVenue::status eq VenueStatus.ACTIVE.name)).skip(page * size).limit(size).toList()
    override suspend fun getVenuesByCityAndType(city: String, venueType: String) =
        venues.find(and(ApprovedVenue::city regex Regex(city, RegexOption.IGNORE_CASE), ApprovedVenue::venueType eq venueType, ApprovedVenue::status eq VenueStatus.ACTIVE.name)).toList()
    override suspend fun searchVenues(query: String, page: Int, size: Int): List<ApprovedVenue> {
        val regex = Regex(query, RegexOption.IGNORE_CASE)
        return venues.find(or(ApprovedVenue::name regex regex, ApprovedVenue::description regex regex, ApprovedVenue::address regex regex, ApprovedVenue::tags contains query.lowercase())).skip(page * size).limit(size).toList()
    }
    override suspend fun getAllVenues(page: Int, size: Int) = venues.find().sort(descending(ApprovedVenue::createdAt)).skip(page * size).limit(size).toList()
    override suspend fun updateVenue(id: String, venue: ApprovedVenue) = try { venues.replaceOne(ApprovedVenue::id eq id, venue).modifiedCount > 0 } catch (e: Exception) { false }
    override suspend fun updateVenueStatus(id: String, status: String) = try {
        venues.updateOne(ApprovedVenue::id eq id, combine(setValue(ApprovedVenue::status, status), setValue(ApprovedVenue::updatedAt, System.currentTimeMillis()))).modifiedCount > 0
    } catch (e: Exception) { false }
    override suspend fun deleteVenue(id: String) = try { venues.deleteOne(ApprovedVenue::id eq id).deletedCount > 0 } catch (e: Exception) { false }
    override suspend fun countByCity(city: String) = venues.countDocuments(ApprovedVenue::city regex Regex(city, RegexOption.IGNORE_CASE))
    override suspend fun countByStatus(status: String) = venues.countDocuments(ApprovedVenue::status eq status)
    override suspend fun createReview(review: VenueReview) = try { reviews.insertOne(review); true } catch (e: Exception) { false }
    override suspend fun getReviewsByVenueId(venueId: String) = reviews.find(VenueReview::venueId eq venueId).sort(descending(VenueReview::createdAt)).toList()
}

class AuditLogRepositoryImpl(db: MongoDatabase) : AuditLogRepository {
    private val logs: MongoCollection<AuditLog> = db.getCollection("audit_logs")
    override suspend fun log(entry: AuditLog) = try { logs.insertOne(entry); true } catch (e: Exception) { false }
    override suspend fun getByAdmin(adminId: String, page: Int, size: Int) = logs.find(AuditLog::adminId eq adminId).sort(descending(AuditLog::createdAt)).skip(page * size).limit(size).toList()
    override suspend fun getByTarget(targetType: String, targetId: String) = logs.find(and(AuditLog::targetType eq targetType, AuditLog::targetId eq targetId)).sort(descending(AuditLog::createdAt)).toList()
    override suspend fun getAll(page: Int, size: Int) = logs.find().sort(descending(AuditLog::createdAt)).skip(page * size).limit(size).toList()
    override suspend fun getByDateRange(from: Long, to: Long, page: Int, size: Int) = logs.find(and(AuditLog::createdAt gte from, AuditLog::createdAt lte to)).sort(descending(AuditLog::createdAt)).skip(page * size).limit(size).toList()
    override suspend fun countAll() = logs.countDocuments()
}

class SystemConfigRepositoryImpl(db: MongoDatabase) : SystemConfigRepository {
    private val configs: MongoCollection<SystemConfig> = db.getCollection("system_config")
    override suspend fun getConfig(): SystemConfig {
        return configs.findOne(SystemConfig::id eq "system_config") ?: SystemConfig().also { configs.insertOne(it) }
    }
    override suspend fun updateConfig(config: SystemConfig) = try { configs.replaceOne(SystemConfig::id eq "system_config", config.copy(updatedAt = System.currentTimeMillis())); true } catch (e: Exception) { false }
}

class AdminAlertRepositoryImpl(db: MongoDatabase) : AdminAlertRepository {
    private val alerts: MongoCollection<AdminAlert> = db.getCollection("admin_alerts")
    override suspend fun createAlert(alert: AdminAlert) = try { alerts.insertOne(alert); true } catch (e: Exception) { false }
    override suspend fun getUnread(page: Int, size: Int) = alerts.find(AdminAlert::isRead eq false).sort(descending(AdminAlert::createdAt)).skip(page * size).limit(size).toList()
    override suspend fun getAll(page: Int, size: Int) = alerts.find().sort(descending(AdminAlert::createdAt)).skip(page * size).limit(size).toList()
    override suspend fun markRead(id: String) = try { alerts.updateOne(AdminAlert::id eq id, setValue(AdminAlert::isRead, true)).modifiedCount > 0 } catch (e: Exception) { false }
    override suspend fun resolve(id: String, adminId: String) = try {
        alerts.updateOne(AdminAlert::id eq id, combine(setValue(AdminAlert::isResolved, true), setValue(AdminAlert::resolvedBy, adminId), setValue(AdminAlert::resolvedAt, System.currentTimeMillis()))).modifiedCount > 0
    } catch (e: Exception) { false }
    override suspend fun countUnread() = alerts.countDocuments(AdminAlert::isRead eq false)
    override suspend fun deleteOlderThan(timestamp: Long) = alerts.deleteMany(AdminAlert::createdAt lt timestamp).deletedCount
}

class EmergencyResourceRepositoryImpl(db: MongoDatabase) : EmergencyResourceRepository {
    private val resources: MongoCollection<EmergencyResource> = db.getCollection("emergency_resources")
    override suspend fun create(resource: EmergencyResource) = try { resources.insertOne(resource); true } catch (e: Exception) { false }
    override suspend fun getById(id: String) = resources.findOne(EmergencyResource::id eq id)
    override suspend fun getActive(country: String) = resources.find(and(EmergencyResource::isActive eq true, EmergencyResource::country eq country)).sort(ascending(EmergencyResource::displayOrder)).toList()
    override suspend fun getAll() = resources.find().sort(ascending(EmergencyResource::displayOrder)).toList()
    override suspend fun update(id: String, resource: EmergencyResource) = try { resources.replaceOne(EmergencyResource::id eq id, resource).modifiedCount > 0 } catch (e: Exception) { false }
    override suspend fun delete(id: String) = try { resources.deleteOne(EmergencyResource::id eq id).deletedCount > 0 } catch (e: Exception) { false }
}

class ContentReportRepositoryImpl(db: MongoDatabase) : ContentReportRepository {
    private val reports: MongoCollection<ContentReport> = db.getCollection("content_reports")
    override suspend fun create(report: ContentReport) = try { reports.insertOne(report); true } catch (e: Exception) { false }
    override suspend fun getById(id: String) = reports.findOne(ContentReport::id eq id)
    override suspend fun getPending(page: Int, size: Int) = reports.find(ContentReport::status eq "PENDING").sort(descending(ContentReport::createdAt)).skip(page * size).limit(size).toList()
    override suspend fun getAll(page: Int, size: Int) = reports.find().sort(descending(ContentReport::createdAt)).skip(page * size).limit(size).toList()
    override suspend fun updateStatus(id: String, status: String, reviewedBy: String, action: String) = try {
        reports.updateOne(ContentReport::id eq id, combine(setValue(ContentReport::status, status), setValue(ContentReport::reviewedBy, reviewedBy), setValue(ContentReport::reviewedAt, System.currentTimeMillis()), setValue(ContentReport::action, action))).modifiedCount > 0
    } catch (e: Exception) { false }
    override suspend fun countPending() = reports.countDocuments(ContentReport::status eq "PENDING")
}
