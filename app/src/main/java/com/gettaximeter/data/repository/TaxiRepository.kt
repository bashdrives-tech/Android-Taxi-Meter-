package com.gettaximeter.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gettaximeter.data.db.AppDatabase
import com.gettaximeter.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class TaxiRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val tripDao = db.tripDao()
    private val driverDao = db.driverDao()
    private val gpsLogDao = db.gpsLogDao()

    // Firebase state tracking
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    // Session State
    private val _currentUserRole = MutableStateFlow<UserRole?>(null)
    val currentUserRole: StateFlow<UserRole?> = _currentUserRole.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    // Configuration states
    private val _currentTariff = MutableStateFlow(Tariff())
    val currentTariff: StateFlow<Tariff> = _currentTariff.asStateFlow()

    private val _currentRentalRules = MutableStateFlow(RentalRules())
    val currentRentalRules: StateFlow<RentalRules> = _currentRentalRules.asStateFlow()

    init {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            Log.d("TaxiRepository", "Firebase initialized successfully")
            
            // Real-time synchronization listeners
            firestore?.let { fs ->
                // Listen to drivers collection
                fs.collection("drivers").addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("TaxiRepository", "Error listening to drivers collection", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            for (doc in snapshot.documents) {
                                try {
                                    val driver = doc.toObject(Driver::class.java)
                                    if (driver != null) {
                                        driverDao.insertDriver(driver)
                                    }
                                } catch (e: Exception) {
                                    Log.e("TaxiRepository", "Failed parsing driver doc", e)
                                }
                            }
                        }
                    }
                }

                // Listen to trips collection
                fs.collection("trips").addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("TaxiRepository", "Error listening to trips collection", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            for (doc in snapshot.documents) {
                                try {
                                    val trip = doc.toObject(Trip::class.java)
                                    if (trip != null) {
                                        tripDao.insertTrip(trip)
                                    }
                                } catch (e: Exception) {
                                    Log.e("TaxiRepository", "Failed parsing trip doc", e)
                                }
                            }
                        }
                    }
                }

                // Listen to settings collection for tariff and rental rules
                fs.collection("settings").document("tariff").addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("TaxiRepository", "Error listening to tariff settings", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val tariff = snapshot.toObject(Tariff::class.java)
                            if (tariff != null) {
                                _currentTariff.value = tariff
                            }
                        } catch (e: Exception) {
                            Log.e("TaxiRepository", "Failed parsing tariff", e)
                        }
                    }
                }

                fs.collection("settings").document("rental_rules").addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("TaxiRepository", "Error listening to rental rules settings", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val rules = snapshot.toObject(RentalRules::class.java)
                            if (rules != null) {
                                _currentRentalRules.value = rules
                            }
                        } catch (e: Exception) {
                            Log.e("TaxiRepository", "Failed parsing rental rules", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TaxiRepository", "Firebase not configured or missing configuration files. Falling back to local offline sandbox.", e)
        }

        // Seed some sample data initially
        seedSampleData()
    }

    private fun seedSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if drivers exist
            val existingDrivers = db.driverDao().getDriverById("DRV001")
            if (existingDrivers == null) {
                // Seed premium test drivers
                val d1 = Driver("DRV001", "Rajesh Kumar", "+91 98765 43210", isEnabled = true, isOnline = true, 12.9716, 77.5946)
                val d2 = Driver("DRV002", "Amit Sharma", "+91 98765 43211", isEnabled = true, isOnline = true, 12.9279, 77.6271)
                val d3 = Driver("DRV003", "Suresh Patel", "+91 98765 43212", isEnabled = false, isOnline = false, 12.9562, 77.7011)
                
                driverDao.insertDriver(d1)
                driverDao.insertDriver(d2)
                driverDao.insertDriver(d3)

                // Seed some sample trips
                val t1 = Trip(
                    id = "TRIP101",
                    customerName = "Arjun Mehra",
                    customerMobile = "+91 99000 11223",
                    pickupLocation = "Indiranagar Metro Station",
                    dropLocation = "Kempegowda International Airport",
                    tripType = TripType.AIRPORT_TRANSFER,
                    status = TripStatus.ENDED,
                    driverId = "DRV001",
                    driverName = "Rajesh Kumar",
                    otp = "5821",
                    startTimestamp = System.currentTimeMillis() - 7200000, // 2 hrs ago
                    endTimestamp = System.currentTimeMillis() - 3600000,   // 1 hr ago
                    baseFare = 100.0,
                    farePerKm = 30.0,
                    totalKm = 38.5,
                    waitingMinutes = 12.0,
                    calculatedFare = 100.0 + (38.5 * 30.0) + (12.0 * 2.25),
                    isSynced = true
                ).apply {
                    gstAmount = calculatedFare * 0.05
                    totalWithGst = calculatedFare + gstAmount
                }

                val t2 = Trip(
                    id = "TRIP102",
                    customerName = "Priya Sen",
                    customerMobile = "+91 88776 65544",
                    pickupLocation = "Koramangala 4th Block",
                    dropLocation = "Whitefield IT Park",
                    tripType = TripType.RUNNING_METER,
                    status = TripStatus.ASSIGNED,
                    driverId = "DRV001",
                    driverName = "Rajesh Kumar",
                    otp = "9430",
                    isSynced = false
                )

                val t3 = Trip(
                    id = "TRIP103",
                    customerName = "Kabir Singh",
                    customerMobile = "+91 77665 54433",
                    pickupLocation = "MG Road, Bangalore",
                    dropLocation = "Electronic City",
                    tripType = TripType.RENTAL_PACKAGE,
                    status = TripStatus.STARTED,
                    driverId = "DRV002",
                    driverName = "Amit Sharma",
                    otp = "2145",
                    startTimestamp = System.currentTimeMillis() - 1800000, // 30 mins ago
                    baseFare = 80.0,
                    rentalHoursIncluded = 4,
                    rentalKmIncluded = 40,
                    rentalExtraKmRate = 20.0,
                    rentalExtraHourRate = 150.0,
                    isSynced = false
                )

                tripDao.insertTrip(t1)
                tripDao.insertTrip(t2)
                tripDao.insertTrip(t3)
            }
        }
    }

    suspend fun findDriverByPhone(phone: String): Driver? = withContext(Dispatchers.IO) {
        val cleanInput = phone.replace("\\D".toRegex(), "")
        if (cleanInput.isBlank()) return@withContext null
        val allDrivers = driverDao.getAllDriversList()
        return@withContext allDrivers.find {
            val cleanDrv = it.mobile.replace("\\D".toRegex(), "")
            cleanInput.endsWith(cleanDrv) || cleanDrv.endsWith(cleanInput)
        }
    }

    // --- Authentication ---
    suspend fun loginWithPhone(phone: String, otpCode: String, role: UserRole): Boolean = withContext(Dispatchers.IO) {
        if (otpCode != "123456" && otpCode != "1234") {
            return@withContext false
        }
        val cleanPhoneInput = phone.replace("\\D".toRegex(), "")
        if (role == UserRole.ADMIN) {
            _currentUserRole.value = role
            _currentUserId.value = "ADMIN_USER_${cleanPhoneInput.takeLast(4)}"
            _currentUserName.value = "Dispatcher ${cleanPhoneInput.takeLast(4)}"
            return@withContext true
        } else {
            var matchedDriver = findDriverByPhone(phone)
            if (matchedDriver == null) {
                // Auto-register driver so they can log in instantly
                val driverId = "DRV${Random.nextInt(100, 999)}"
                val newDriver = Driver(
                    id = driverId,
                    name = "Driver ${cleanPhoneInput.takeLast(4)}",
                    mobile = phone,
                    isEnabled = true,
                    isOnline = true
                )
                driverDao.insertDriver(newDriver)
                firestore?.collection("drivers")?.document(driverId)?.set(newDriver)
                matchedDriver = newDriver
            }
            
            _currentUserRole.value = role
            _currentUserId.value = matchedDriver.id
            _currentUserName.value = matchedDriver.name
            
            // Set driver status online
            matchedDriver.isOnline = true
            driverDao.insertDriver(matchedDriver)
            firestore?.collection("drivers")?.document(matchedDriver.id)?.set(matchedDriver)
            
            return@withContext true
        }
    }

    suspend fun logoutWithPresence() = withContext(Dispatchers.IO) {
        val role = _currentUserRole.value
        val userId = _currentUserId.value
        if (role == UserRole.DRIVER && userId != null) {
            val driver = driverDao.getDriverById(userId)
            if (driver != null) {
                driver.isOnline = false
                driverDao.insertDriver(driver)
                firestore?.collection("drivers")?.document(driver.id)?.set(driver)
            }
        }
        _currentUserRole.value = null
        _currentUserId.value = null
        _currentUserName.value = null
    }

    fun logout() {
        _currentUserRole.value = null
        _currentUserId.value = null
        _currentUserName.value = null
    }

    // --- Tariff Settings ---
    fun updateTariff(newTariff: Tariff) {
        _currentTariff.value = newTariff
        // In real Firebase mode, push updated tariff to Firestore settings collection
        firestore?.collection("settings")?.document("tariff")?.set(newTariff)
    }

    fun updateRentalRules(newRules: RentalRules) {
        _currentRentalRules.value = newRules
        firestore?.collection("settings")?.document("rental_rules")?.set(newRules)
    }

    // --- Trip Management ---
    fun getTrips(): Flow<List<Trip>> = tripDao.getAllTripsFlow()

    fun getTripById(tripId: String): Flow<Trip?> = tripDao.getTripByIdFlow(tripId)

    suspend fun createTrip(
        customerName: String,
        customerMobile: String,
        pickupLocation: String,
        dropLocation: String,
        tripType: TripType,
        driverId: String
    ): String = withContext(Dispatchers.IO) {
        val tripId = "TRIP${Random.nextInt(100, 999)}"
        val otp = String.format("%04d", Random.nextInt(1000, 9999))
        val driver = driverDao.getDriverById(driverId)
        val driverName = driver?.name ?: "Unknown Driver"

        val activeTariff = _currentTariff.value
        val activeRental = _currentRentalRules.value

        val trip = Trip(
            id = tripId,
            customerName = customerName,
            customerMobile = customerMobile,
            pickupLocation = pickupLocation,
            dropLocation = dropLocation,
            tripType = tripType,
            status = TripStatus.ASSIGNED,
            driverId = driverId,
            driverName = driverName,
            otp = otp,
            baseFare = activeTariff.baseFare,
            farePerKm = activeTariff.farePerKm,
            waitingChargePerMin = activeTariff.waitingChargePerMin,
            isHillChargeEnabled = activeTariff.hillChargeActive,
            isNightChargeEnabled = activeTariff.nightChargeActive,
            rentalHoursIncluded = activeRental.hoursIncluded,
            rentalKmIncluded = activeRental.kmIncluded,
            rentalExtraKmRate = activeRental.extraKmRate,
            rentalExtraHourRate = activeRental.extraHourRate,
            lastUpdated = System.currentTimeMillis()
        )

        tripDao.insertTrip(trip)

        // Sync with Firestore if active
        firestore?.collection("trips")?.document(tripId)?.set(trip)
            ?.addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val updatedTrip = trip.copy(isSynced = true)
                    tripDao.insertTrip(updatedTrip)
                }
            }

        return@withContext tripId
    }

    suspend fun startTrip(tripId: String, enteredOtp: String): Boolean = withContext(Dispatchers.IO) {
        val trip = tripDao.getTripById(tripId) ?: return@withContext false
        if (trip.otp == enteredOtp || enteredOtp == "1234") { // Sandbox OTP bypass for testing
            trip.status = TripStatus.STARTED
            trip.startTimestamp = System.currentTimeMillis()
            trip.lastUpdated = System.currentTimeMillis()
            tripDao.insertTrip(trip)

            firestore?.collection("trips")?.document(tripId)?.set(trip)
            return@withContext true
        }
        return@withContext false
    }

    suspend fun updateLiveMeter(
        tripId: String,
        currentKm: Double,
        waitingMinutes: Double,
        isHillActive: Boolean,
        isNightActive: Boolean
    ) = withContext(Dispatchers.IO) {
        val trip = tripDao.getTripById(tripId) ?: return@withContext
        if (trip.status != TripStatus.STARTED) return@withContext

        trip.totalKm = currentKm
        trip.waitingMinutes = waitingMinutes
        trip.isHillChargeEnabled = isHillActive
        trip.isNightChargeEnabled = isNightActive

        // Calculate current live fare
        trip.calculatedFare = calculateFare(trip)
        trip.gstAmount = trip.calculatedFare * (_currentTariff.value.gstPercent / 100.0)
        trip.totalWithGst = trip.calculatedFare + trip.gstAmount
        trip.lastUpdated = System.currentTimeMillis()

        tripDao.insertTrip(trip)
        firestore?.collection("trips")?.document(tripId)?.set(trip)
    }

    suspend fun endTrip(tripId: String) = withContext(Dispatchers.IO) {
        val trip = tripDao.getTripById(tripId) ?: return@withContext
        if (trip.status != TripStatus.STARTED) return@withContext

        trip.status = TripStatus.ENDED
        trip.endTimestamp = System.currentTimeMillis()
        trip.lastUpdated = System.currentTimeMillis()

        trip.calculatedFare = calculateFare(trip)
        trip.gstAmount = trip.calculatedFare * (_currentTariff.value.gstPercent / 100.0)
        trip.totalWithGst = trip.calculatedFare + trip.gstAmount

        tripDao.insertTrip(trip)
        firestore?.collection("trips")?.document(tripId)?.set(trip)
    }

    // --- Tariff Engine (Modular Calculation) ---
    fun calculateFare(trip: Trip): Double {
        return when (trip.tripType) {
            TripType.FIXED_FARE -> {
                // For fixed fare, baseFare is entered by Admin as total fare
                trip.baseFare
            }
            TripType.RUNNING_METER, TripType.AIRPORT_TRANSFER, TripType.OUTSTATION -> {
                var fare = trip.baseFare + (trip.totalKm * trip.farePerKm) + (trip.waitingMinutes * trip.waitingChargePerMin)
                if (trip.isHillChargeEnabled) {
                    fare += 300.0 // Premium fixed hill charge
                }
                if (trip.isNightChargeEnabled) {
                    fare *= 1.25 // 25% surcharge for night shift
                }
                fare
            }
            TripType.RENTAL_PACKAGE -> {
                // Rental packages: base price for included hours/KMs, extra for additions
                val basePackagePrice = trip.baseFare // Base set by dispatcher (e.g., 800)
                
                val elapsedMs = (trip.endTimestamp ?: System.currentTimeMillis()) - (trip.startTimestamp ?: System.currentTimeMillis())
                val elapsedHours = Math.max(1, (elapsedMs / 3600000.0).toInt())
                
                val extraHours = Math.max(0, elapsedHours - trip.rentalHoursIncluded)
                val extraKm = Math.max(0.0, trip.totalKm - trip.rentalKmIncluded)

                basePackagePrice + (extraHours * trip.rentalExtraHourRate) + (extraKm * trip.rentalExtraKmRate)
            }
        }
    }

    // --- Driver Management ---
    fun getDrivers(): Flow<List<Driver>> = driverDao.getAllDriversFlow()

    suspend fun registerDriver(name: String, mobile: String): String = withContext(Dispatchers.IO) {
        val driverId = "DRV${Random.nextInt(100, 999)}"
        val driver = Driver(
            id = driverId,
            name = name,
            mobile = mobile,
            isEnabled = true,
            isOnline = false
        )
        driverDao.insertDriver(driver)
        firestore?.collection("drivers")?.document(driverId)?.set(driver)
        return@withContext driverId
    }

    suspend fun toggleDriverEnabled(driverId: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        val driver = driverDao.getDriverById(driverId) ?: return@withContext
        driver.isEnabled = isEnabled
        driverDao.insertDriver(driver)
        firestore?.collection("drivers")?.document(driverId)?.set(driver)
    }

    suspend fun updateDriverPresence(driverId: String, isOnline: Boolean) = withContext(Dispatchers.IO) {
        val driver = driverDao.getDriverById(driverId) ?: return@withContext
        driver.isOnline = isOnline
        driverDao.insertDriver(driver)
        firestore?.collection("drivers")?.document(driverId)?.set(driver)
    }

    suspend fun updateDriverLocation(driverId: String, lat: Double, lng: Double) = withContext(Dispatchers.IO) {
        val driver = driverDao.getDriverById(driverId) ?: return@withContext
        driver.currentLat = lat
        driver.currentLng = lng
        driver.lastUpdated = System.currentTimeMillis()
        driverDao.insertDriver(driver)
        
        // Push live GPS to firestore for active admin telemetry
        firestore?.collection("drivers")?.document(driverId)?.update(
            "currentLat", lat,
            "currentLng", lng,
            "lastUpdated", System.currentTimeMillis()
        )
    }

    // --- GPS Logging ---
    suspend fun addGPSLog(tripId: String, lat: Double, lng: Double, speed: Float, isMock: Boolean) = withContext(Dispatchers.IO) {
        val log = GPSLog(
            tripId = tripId,
            lat = lat,
            lng = lng,
            speed = speed,
            isMock = isMock
        )
        gpsLogDao.insertGPSLog(log)
    }

    suspend fun getGPSLogsForTrip(tripId: String): List<GPSLog> = withContext(Dispatchers.IO) {
        return@withContext gpsLogDao.getLogsForTrip(tripId)
    }

    // Unsynced Syncing
    suspend fun syncOfflineData() = withContext(Dispatchers.IO) {
        val unsyncedTrips = tripDao.getUnsyncedTrips()
        for (trip in unsyncedTrips) {
            try {
                firestore?.collection("trips")?.document(trip.id)?.set(trip)
                trip.isSynced = true
                tripDao.insertTrip(trip)
            } catch (e: Exception) {
                Log.e("TaxiRepository", "Failed to sync trip ${trip.id}", e)
            }
        }
    }
}
