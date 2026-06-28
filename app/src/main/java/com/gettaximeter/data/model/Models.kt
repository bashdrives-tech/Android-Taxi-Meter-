package com.gettaximeter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN, DRIVER
}

enum class TripType {
    FIXED_FARE,
    RUNNING_METER,
    RENTAL_PACKAGE,
    AIRPORT_TRANSFER,
    OUTSTATION
}

enum class TripStatus {
    ASSIGNED,
    STARTED,
    ENDED,
    CANCELLED
}

data class Tariff(
    var baseFare: Double = 80.0,
    var farePerKm: Double = 28.0,
    var waitingChargePerMin: Double = 2.25,
    var hillChargeActive: Boolean = false,
    var nightChargeActive: Boolean = false,
    var gstPercent: Double = 5.0
)

data class RentalRules(
    var hoursIncluded: Int = 4,
    var kmIncluded: Int = 40,
    var extraKmRate: Double = 20.0,
    var extraHourRate: Double = 150.0
)

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val id: String = "",
    val customerName: String = "",
    val customerMobile: String = "",
    val pickupLocation: String = "",
    val dropLocation: String = "",
    val tripType: TripType = TripType.RUNNING_METER,
    var status: TripStatus = TripStatus.ASSIGNED,
    val driverId: String = "",
    val driverName: String = "",
    val otp: String = "",
    var startTimestamp: Long? = null,
    var endTimestamp: Long? = null,
    
    // Configured fares at assignment time
    var baseFare: Double = 80.0,
    var farePerKm: Double = 28.0,
    var waitingChargePerMin: Double = 2.25,
    var isHillChargeEnabled: Boolean = false,
    var isNightChargeEnabled: Boolean = false,
    
    // For Rental Package
    var rentalHoursIncluded: Int = 4,
    var rentalKmIncluded: Int = 40,
    var rentalExtraKmRate: Double = 20.0,
    var rentalExtraHourRate: Double = 150.0,
    
    // Live fields updated during trip
    var totalKm: Double = 0.0,
    var waitingMinutes: Double = 0.0,
    var calculatedFare: Double = 0.0,
    var gstAmount: Double = 0.0,
    var totalWithGst: Double = 0.0,
    
    // Sync Metadata
    var isSynced: Boolean = false,
    var lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "drivers")
data class Driver(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val mobile: String = "",
    var isEnabled: Boolean = true,
    var isOnline: Boolean = false,
    var currentLat: Double = 0.0,
    var currentLng: Double = 0.0,
    var lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "gps_logs")
data class GPSLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: String,
    val lat: Double,
    val lng: Double,
    val speed: Float, // speed in m/s
    val timestamp: Long = System.currentTimeMillis(),
    val isMock: Boolean = false
)
