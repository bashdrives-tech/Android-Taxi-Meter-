package com.gettaximeter.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gettaximeter.GetTaxiMeterApp
import com.gettaximeter.data.model.*
import com.gettaximeter.data.repository.TaxiRepository
import com.gettaximeter.service.LocationTrackingService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class TaxiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaxiRepository(application)
    private val context = application.applicationContext

    // Session State
    val currentUserRole: StateFlow<UserRole?> = repository.currentUserRole
    val currentUserId: StateFlow<String?> = repository.currentUserId
    val currentUserName: StateFlow<String?> = repository.currentUserName

    // Active screen navigation
    private val _currentScreen = MutableStateFlow("role_select")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Loaded flows from Room
    val trips: StateFlow<List<Trip>> = repository.getTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val drivers: StateFlow<List<Driver>> = repository.getDrivers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Selected objects
    private val _selectedTrip = MutableStateFlow<Trip?>(null)
    val selectedTrip: StateFlow<Trip?> = _selectedTrip.asStateFlow()

    val currentTariff: StateFlow<Tariff> = repository.currentTariff
    val currentRentalRules: StateFlow<RentalRules> = repository.currentRentalRules

    // Feedback notifications
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Live GPS Travel Simulation state
    private var simulationJob: Job? = null
    private val _isSimulatingTravel = MutableStateFlow(false)
    val isSimulatingTravel: StateFlow<Boolean> = _isSimulatingTravel.asStateFlow()

    init {
        // Collect active selected trip details automatically if any is in started mode
        viewModelScope.launch {
            trips.collect { tripList ->
                val active = tripList.find { it.status == TripStatus.STARTED && it.driverId == currentUserId.value }
                    ?: tripList.find { it.status == TripStatus.STARTED }
                if (active != null && _selectedTrip.value?.id != active.id) {
                    _selectedTrip.value = active
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun verifyPhoneForOtp(phone: String, role: UserRole, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val cleanPhone = phone.replace("\\D".toRegex(), "")
            if (role == UserRole.ADMIN) {
                _toastMessage.value = "OTP code for login: 123456"
                onSuccess()
            } else {
                val matched = repository.findDriverByPhone(phone)
                if (matched != null) {
                    _toastMessage.value = "OTP code for login: 123456"
                    onSuccess()
                } else {
                    // Auto-register driver so they can log in instantly
                    repository.registerDriver("Driver ${cleanPhone.takeLast(4)}", phone)
                    _toastMessage.value = "New Driver Auto-Registered! OTP: 123456"
                    onSuccess()
                }
            }
        }
    }

    fun login(phone: String, otpCode: String, role: UserRole) {
        viewModelScope.launch {
            val success = repository.loginWithPhone(phone, otpCode, role)
            if (success) {
                _toastMessage.value = "Login Successful as ${role.name}"
                if (role == UserRole.ADMIN) {
                    _currentScreen.value = "admin_dashboard"
                } else {
                    _currentScreen.value = "driver_home"
                }
            } else {
                _toastMessage.value = "Invalid OTP Code. Try '123456'"
            }
        }
    }

    fun logout() {
        stopTravelSimulation()
        viewModelScope.launch {
            repository.logoutWithPresence()
            _currentScreen.value = "role_select"
        }
    }

    // --- Admin Operations ---
    fun createAndAssignTrip(
        customerName: String,
        customerMobile: String,
        pickup: String,
        drop: String,
        type: TripType,
        driverId: String
    ) {
        viewModelScope.launch {
            if (customerName.isBlank() || customerMobile.isBlank() || pickup.isBlank() || drop.isBlank()) {
                _toastMessage.value = "Please complete all dispatch details"
                return@launch
            }
            val tripId = repository.createTrip(customerName, customerMobile, pickup, drop, type, driverId)
            _toastMessage.value = "Trip $tripId dispatched successfully!"
            _currentScreen.value = "admin_dashboard"
        }
    }

    fun registerNewDriver(name: String, mobile: String) {
        viewModelScope.launch {
            if (name.isBlank() || mobile.isBlank()) {
                _toastMessage.value = "Please enter name and mobile"
                return@launch
            }
            val driverId = repository.registerDriver(name, mobile)
            _toastMessage.value = "Driver registered with ID $driverId"
        }
    }

    fun toggleDriver(driverId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleDriverEnabled(driverId, isEnabled)
            _toastMessage.value = "Driver status updated"
        }
    }

    fun updateTariff(baseFare: Double, farePerKm: Double, waiting: Double, hill: Boolean, night: Boolean, gst: Double) {
        val newTariff = Tariff(baseFare, farePerKm, waiting, hill, night, gst)
        repository.updateTariff(newTariff)
        _toastMessage.value = "Tariff configuration updated remotely"
    }

    fun updateRentalRules(hours: Int, km: Int, extraKm: Double, extraHour: Double) {
        val newRules = RentalRules(hours, km, extraKm, extraHour)
        repository.updateRentalRules(newRules)
        _toastMessage.value = "Rental rules updated remotely"
    }

    // --- Driver Operations ---
    fun updateDriverPresence(driverId: String, isOnline: Boolean) {
        viewModelScope.launch {
            repository.updateDriverPresence(driverId, isOnline)
            _toastMessage.value = if (isOnline) "You are now online" else "You are now offline"
        }
    }

    fun selectTrip(trip: Trip) {
        _selectedTrip.value = trip
    }

    fun startTripWithOtp(tripId: String, otp: String) {
        viewModelScope.launch {
            val success = repository.startTrip(tripId, otp)
            if (success) {
                _toastMessage.value = "Trip started! Live GPS Tracking Active."
                val active = repository.getTripById(tripId).firstOrNull()
                _selectedTrip.value = active
                _currentScreen.value = "live_meter"

                // Launch Foreground Tracking Service
                val serviceIntent = Intent(context, LocationTrackingService::class.java).apply {
                    action = LocationTrackingService.ACTION_START_TRACKING
                    putExtra(LocationTrackingService.EXTRA_TRIP_ID, tripId)
                }
                context.startService(serviceIntent)
            } else {
                _toastMessage.value = "Incorrect OTP. Please check with Dispatcher."
            }
        }
    }

    fun endActiveTrip(tripId: String) {
        viewModelScope.launch {
            repository.endTrip(tripId)
            stopTravelSimulation()

            // Stop Foreground Service
            val serviceIntent = Intent(context, LocationTrackingService::class.java).apply {
                action = LocationTrackingService.ACTION_STOP_TRACKING
            }
            context.startService(serviceIntent)

            _toastMessage.value = "Trip ended. Digital Receipt Generated."
            val ended = repository.getTripById(tripId).firstOrNull()
            _selectedTrip.value = ended
            _currentScreen.value = "receipt_screen"
        }
    }

    // --- Interactive GPS Travel Simulator ---
    fun toggleTravelSimulation() {
        if (_isSimulatingTravel.value) {
            stopTravelSimulation()
        } else {
            startTravelSimulation()
        }
    }

    private fun startTravelSimulation() {
        val tripId = _selectedTrip.value?.id ?: return
        _isSimulatingTravel.value = true
        _toastMessage.value = "Interactive Travel Simulator: Car moving at 45 KM/H..."

        simulationJob = viewModelScope.launch {
            var simKm = _selectedTrip.value?.totalKm ?: 0.0
            var simWait = _selectedTrip.value?.waitingMinutes ?: 0.0

            while (_isSimulatingTravel.value) {
                delay(2000) // update every 2 seconds
                
                // Randomize simulated coordinates and speed
                val speed = if (Random.nextBoolean()) 45 else 0 // toggles speed to simulate stops
                if (speed == 0) {
                    simWait += 0.5 // accumulate 30 seconds waiting charge
                } else {
                    simKm += 0.25 // accumulate 250 meters traveled
                }

                repository.updateLiveMeter(
                    tripId = tripId,
                    currentKm = String.format("%.2f", simKm).toDouble(),
                    waitingMinutes = String.format("%.1f", simWait).toDouble(),
                    isHillActive = _selectedTrip.value?.isHillChargeEnabled ?: false,
                    isNightActive = _selectedTrip.value?.isNightChargeEnabled ?: false
                )

                // Refresh selected trip state Flow
                val updated = repository.getTripById(tripId).firstOrNull()
                _selectedTrip.value = updated
            }
        }
    }

    private fun stopTravelSimulation() {
        _isSimulatingTravel.value = false
        simulationJob?.cancel()
        simulationJob = null
    }

    // --- Share Functions ---
    fun shareToWhatsApp(trip: Trip) {
        val summary = generateReceiptSummary(trip)
        val uri = Uri.parse("whatsapp://send?text=${Uri.encode(summary)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
            _toastMessage.value = "Opening WhatsApp..."
        } catch (e: Exception) {
            _toastMessage.value = "WhatsApp not installed. Sending as generic text share instead."
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, summary)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(textIntent, "Share Receipt").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    fun shareReceiptPdf(trip: Trip) {
        // PDF Simulation: Show a toast and trigger platform Share Chooser with printable text file representing receipt
        val receiptText = generateReceiptSummary(trip)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "GetTaxiMeter Receipt - #${trip.id}")
            putExtra(Intent.EXTRA_TEXT, receiptText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "Share Printable Receipt").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        _toastMessage.value = "Receipt prepared for printing / PDF share"
    }

    private fun generateReceiptSummary(trip: Trip): String {
        val durationMin = if (trip.startTimestamp != null && trip.endTimestamp != null) {
            ((trip.endTimestamp!! - trip.startTimestamp!!) / 60000).toInt()
        } else 0

        return """
            === GET TAXI METER ===
            Digital Trip Receipt
            -------------------------
            Trip ID: #${trip.id}
            Customer: ${trip.customerName}
            Mobile: ${trip.customerMobile}
            Driver: ${trip.driverName}
            
            Pickup: ${trip.pickupLocation}
            Dropoff: ${trip.dropLocation}
            Type: ${trip.tripType.name.replace("_", " ")}
            
            Metrics:
            - Distance: ${trip.totalKm} KM
            - Duration: $durationMin Mins
            - Waiting time: ${trip.waitingMinutes} Mins
            
            Tariff Breakdown:
            - Base Fare: ₹${trip.baseFare}
            - Waiting charge: ₹${trip.waitingChargePerMin}/min
            - Calculated Subtotal: ₹${String.format("%.2f", trip.calculatedFare)}
            - GST (5%): ₹${String.format("%.2f", trip.gstAmount)}
            -------------------------
            TOTAL DUE: ₹${String.format("%.2f", trip.totalWithGst)}
            -------------------------
            Thank you for riding with us!
        """.trimIndent()
    }
}
