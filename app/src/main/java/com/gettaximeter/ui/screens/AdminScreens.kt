package com.gettaximeter.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gettaximeter.data.model.*
import com.gettaximeter.ui.viewmodel.TaxiViewModel

@Composable
fun AdminDashboardScreen(viewModel: TaxiViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dispatch", "Live Trips", "Drivers", "Tariffs", "Receipts")

    Scaffold(
        topBar = {
            // High fidelity premium header replicating the top navigation bar from the Vibrant Palette Design HTML
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo and Brand Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "GET TAXI METER",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                            // System Online Badge row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4ADE80)) // green-400
                                )
                                Text(
                                    text = "SYSTEM ONLINE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Dispatcher Profile and Logout
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Harish Kumar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "MASTER DISPATCHER",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        // Circular avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { viewModel.logout() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Log Out",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        label = { 
                            Text(
                                text = label.toUpperCase(), 
                                fontSize = 9.sp, 
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ) 
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.AddLocation
                                    1 -> Icons.Default.DirectionsCar
                                    2 -> Icons.Default.People
                                    3 -> Icons.Default.Settings
                                    else -> Icons.Default.ReceiptLong
                                },
                                contentDescription = label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> DispatchFormTab(viewModel)
                1 -> LiveTripsTab(viewModel)
                2 -> DriversManagementTab(viewModel)
                3 -> TariffSettingsTab(viewModel)
                4 -> ReceiptsAndReportsTab(viewModel)
            }
        }
    }
}

@Composable
fun DispatchFormTab(viewModel: TaxiViewModel) {
    var customerName by remember { mutableStateOf("") }
    var customerMobile by remember { mutableStateOf("") }
    var pickup by remember { mutableStateOf("") }
    var drop by remember { mutableStateOf("") }
    var tripType by remember { mutableStateOf(TripType.RUNNING_METER) }
    var selectedDriverId by remember { mutableStateOf("") }
    var expandedDriverMenu by remember { mutableStateOf(false) }

    val drivers by viewModel.drivers.collectAsState()
    val activeDrivers = drivers.filter { it.isEnabled }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "DISPATCH NEW TRIP ASSIGNMENT", 
            fontSize = 15.sp, 
            fontWeight = FontWeight.Black, 
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Card styled with primaryContainer background (soft rose-50) and a primary border, matching the quick dispatch container style in design
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "CUSTOMER & LOCATION DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User", tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customerMobile,
                    onValueChange = { customerMobile = it },
                    label = { Text("Customer Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Mobile", tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pickup,
                    onValueChange = { pickup = it },
                    label = { Text("Pickup Location Spot") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Pickup", tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = drop,
                    onValueChange = { drop = it },
                    label = { Text("Drop Location Destination") },
                    leadingIcon = { Icon(Icons.Default.Navigation, contentDescription = "Drop", tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trip Type Selection
        Text(
            text = "TARIFF TYPE CATEGORY", 
            fontSize = 11.sp, 
            fontWeight = FontWeight.Black, 
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TripType.values().forEach { type ->
                val isSelected = tripType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White)
                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .clickable { tripType = type }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.name.replace("_", " ").toUpperCase(),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.secondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Driver Dropdown
        Text(
            text = "ASSIGN TO DRIVER VEHICLE", 
            fontSize = 11.sp, 
            fontWeight = FontWeight.Black, 
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            val driverLabel = activeDrivers.find { it.id == selectedDriverId }?.let { "${it.name} (${it.id})" } ?: "Select Available Driver"
            Button(
                onClick = { expandedDriverMenu = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(driverLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            }

            DropdownMenu(
                expanded = expandedDriverMenu,
                onDismissRequest = { expandedDriverMenu = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                activeDrivers.forEach { driver ->
                    DropdownMenuItem(
                        text = { Text("${driver.name} - ${if (driver.isOnline) "🟢 Online" else "⚪ Offline"}", fontWeight = FontWeight.Bold) },
                        onClick = {
                            selectedDriverId = driver.id
                            expandedDriverMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (selectedDriverId.isBlank()) {
                    viewModel.registerNewDriver("Rajesh Kumar", "+91 98765 43210") // auto seed fallback driver if none selected
                    selectedDriverId = "DRV001"
                }
                viewModel.createAndAssignTrip(
                    customerName, customerMobile, pickup, drop, tripType, selectedDriverId
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.AddLocation, contentDescription = "Dispatch")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "DISPATCH TRIP NOW (OTP GENERATED)", 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun LiveTripsTab(viewModel: TaxiViewModel) {
    val trips by viewModel.trips.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val activeTrips = trips.filter { it.status == TripStatus.ASSIGNED || it.status == TripStatus.STARTED }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "LIVE METER TRACKING", 
            fontSize = 15.sp, 
            fontWeight = FontWeight.Black, 
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Live Telemetry Map locator for tracking drivers and active trips
        LiveTelemetryMap(drivers = drivers, trips = trips)

        if (activeTrips.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("No ongoing taxi trips right now.", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activeTrips.forEach { trip ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TRIP ID: #${trip.id}", fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Badge(
                                    containerColor = if (trip.status == TripStatus.STARTED) Color(0xFF16A34A) else Color(0xFFEA580C)
                                ) {
                                    Text(
                                        text = trip.status.name.toUpperCase(), 
                                        color = Color.White, 
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = "Client", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Client: ${trip.customerName} (${trip.customerMobile})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = "Driver", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Driver: ${trip.driverName} (${trip.driverId})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalTaxi, contentDescription = "Type", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Category: ${trip.tripType.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0xFFF1F5F9))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("START SECURITY OTP", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                                    Text(trip.otp, fontWeight = FontWeight.Black, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("LIVE METER ESTIMATE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                                    Text("₹${String.format("%.2f", trip.totalWithGst)}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF16A34A))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriversManagementTab(viewModel: TaxiViewModel) {
    var newDriverName by remember { mutableStateOf("") }
    var newDriverMobile by remember { mutableStateOf("") }

    val drivers by viewModel.drivers.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "ONBOARD NEW OPERATOR DRIVER", 
            fontSize = 15.sp, 
            fontWeight = FontWeight.Black, 
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = newDriverName,
                    onValueChange = { newDriverName = it },
                    label = { Text("Driver Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newDriverMobile,
                    onValueChange = { newDriverMobile = it },
                    label = { Text("Driver Verified Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.registerNewDriver(newDriverName, newDriverMobile)
                        newDriverName = ""
                        newDriverMobile = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REGISTER DRIVER & GENERATE ID", 
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ACTIVE OPERATOR ROSTER", 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        drivers.forEach { driver ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(driver.name, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("ID: ${driver.id} • ${driver.mobile}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        // Real status row with green pill
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (driver.isOnline) Color(0xFF16A34A) else Color.Gray)
                            )
                            Text(
                                text = if (driver.isOnline) "ONLINE TELEMETRY" else "IDLE OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (driver.isOnline) Color(0xFF16A34A) else Color.Gray
                            )
                        }
                    }

                    Switch(
                        checked = driver.isEnabled,
                        onCheckedChange = { isChecked ->
                            viewModel.toggleDriver(driver.id, isChecked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TariffSettingsTab(viewModel: TaxiViewModel) {
    val tariff by viewModel.currentTariff.collectAsState()
    val rentalRules by viewModel.currentRentalRules.collectAsState()

    var baseFare by remember { mutableStateOf(tariff.baseFare.toString()) }
    var farePerKm by remember { mutableStateOf(tariff.farePerKm.toString()) }
    var waitingCharge by remember { mutableStateOf(tariff.waitingChargePerMin.toString()) }
    var hillToggle by remember { mutableStateOf(tariff.hillChargeActive) }
    var nightToggle by remember { mutableStateOf(tariff.nightChargeActive) }
    var gstPercent by remember { mutableStateOf(tariff.gstPercent.toString()) }

    var rentalHours by remember { mutableStateOf(rentalRules.hoursIncluded.toString()) }
    var rentalKm by remember { mutableStateOf(rentalRules.kmIncluded.toString()) }
    var rentalExtraKm by remember { mutableStateOf(rentalRules.extraKmRate.toString()) }
    var rentalExtraHour by remember { mutableStateOf(rentalRules.extraHourRate.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "REMOTE TARIFF ENGINE CALIBRATION", 
            fontSize = 15.sp, 
            fontWeight = FontWeight.Black, 
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "STANDARD METER RATES", 
                    fontWeight = FontWeight.Black, 
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = baseFare,
                    onValueChange = { baseFare = it },
                    label = { Text("Base Ticket Minimum Fare (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = farePerKm,
                    onValueChange = { farePerKm = it },
                    label = { Text("Running Rate per Kilometer (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = waitingCharge,
                    onValueChange = { waitingCharge = it },
                    label = { Text("Traffic Stop Idle Waiting Charge/Min (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = gstPercent,
                    onValueChange = { gstPercent = it },
                    label = { Text("Government Service Tax Percent GST (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Apply Hill Terrain Premium (Flat ₹300)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = hillToggle, onCheckedChange = { hillToggle = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Apply Late Night Surcharge (+25%)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = nightToggle, onCheckedChange = { nightToggle = it })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "HOURLY RENTAL PACKAGES SETTINGS", 
                    fontWeight = FontWeight.Black, 
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rentalHours,
                        onValueChange = { rentalHours = it },
                        label = { Text("Hours Base") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = rentalKm,
                        onValueChange = { rentalKm = it },
                        label = { Text("KM Limit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rentalExtraKm,
                        onValueChange = { rentalExtraKm = it },
                        label = { Text("Extra KM ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = rentalExtraHour,
                        onValueChange = { rentalExtraHour = it },
                        label = { Text("Extra Hour ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.updateTariff(
                    baseFare.toDoubleOrNull() ?: 80.0,
                    farePerKm.toDoubleOrNull() ?: 28.0,
                    waitingCharge.toDoubleOrNull() ?: 2.25,
                    hillToggle,
                    nightToggle,
                    gstPercent.toDoubleOrNull() ?: 5.0
                )
                viewModel.updateRentalRules(
                    rentalHours.toIntOrNull() ?: 4,
                    rentalKm.toIntOrNull() ?: 40,
                    rentalExtraKm.toDoubleOrNull() ?: 20.0,
                    rentalExtraHour.toDoubleOrNull() ?: 150.0
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Save, contentDescription = "Save")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PUBLISH PRICING TARIFFS REMOTELY", 
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ReceiptsAndReportsTab(viewModel: TaxiViewModel) {
    val trips by viewModel.trips.collectAsState()
    val completedTrips = trips.filter { it.status == TripStatus.ENDED }

    val totalCompletedCount = completedTrips.size
    val totalRevenue = completedTrips.sumOf { it.totalWithGst }
    val totalKm = completedTrips.sumOf { it.totalKm }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "DISPATCHER BUSINESS ANALYTICS", 
            fontSize = 15.sp, 
            fontWeight = FontWeight.Black, 
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Analytics Cards Row using our Vibrant Palette aesthetic (e.g. bold numbers in Rose 600, elegant Slate text headers)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalyticsCard("Trips", totalCompletedCount.toString(), Icons.Default.DirectionsCar, modifier = Modifier.weight(1f))
            AnalyticsCard("Mileage", "${String.format("%.1f", totalKm)} KM", Icons.Default.Timeline, modifier = Modifier.weight(1.2f))
            AnalyticsCard("Revenue", "₹${String.format("%.0f", totalRevenue)}", Icons.Default.Payments, modifier = Modifier.weight(1.2f))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "COMPLETED BILLING RECEIPTS", 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (completedTrips.isEmpty()) {
            Text("No completed digital receipts logged yet.", color = Color.Gray, fontWeight = FontWeight.Bold)
        } else {
            completedTrips.forEach { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { viewModel.selectTrip(trip); viewModel.navigateTo("receipt_screen") },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ID: #${trip.id} • ${trip.customerName}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary, fontSize = 15.sp)
                            Text("Type: ${trip.tripType.name.replace("_", " ").toUpperCase()} • ${trip.totalKm} KM", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "₹${String.format("%.2f", trip.totalWithGst)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(label.toUpperCase(), fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun LiveTelemetryMap(drivers: List<Driver>, trips: List<Trip>) {
    val onlineDrivers = drivers.filter { it.isOnline }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep navy background
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Blueprint pattern and grid lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val mapWidth = size.width
                val mapHeight = size.height
                
                // Draw grid lines
                val gridSize = 40f
                for (x in 0..(mapWidth / gridSize).toInt()) {
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(x * gridSize, 0f),
                        end = Offset(x * gridSize, mapHeight),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(mapHeight / gridSize).toInt()) {
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(0f, y * gridSize),
                        end = Offset(mapWidth, y * gridSize),
                        strokeWidth = 1f
                    )
                }
                
                // Draw Bangalore NH roads
                val majestic = Offset(mapWidth * 0.25f, mapHeight * 0.45f)
                val mgRoad = Offset(mapWidth * 0.45f, mapHeight * 0.45f)
                val indiranagar = Offset(mapWidth * 0.65f, mapHeight * 0.42f)
                val whitefield = Offset(mapWidth * 0.88f, mapHeight * 0.48f)
                val koramangala = Offset(mapWidth * 0.55f, mapHeight * 0.65f)
                val electronicCity = Offset(mapWidth * 0.65f, mapHeight * 0.88f)
                val airport = Offset(mapWidth * 0.75f, mapHeight * 0.15f)
                
                // Main NH Road Lines
                drawLine(color = Color(0xFF334155), start = majestic, end = mgRoad, strokeWidth = 3f)
                drawLine(color = Color(0xFF334155), start = mgRoad, end = indiranagar, strokeWidth = 3f)
                drawLine(color = Color(0xFF334155), start = indiranagar, end = whitefield, strokeWidth = 3f)
                drawLine(color = Color(0xFF334155), start = mgRoad, end = koramangala, strokeWidth = 3f)
                drawLine(color = Color(0xFF334155), start = koramangala, end = electronicCity, strokeWidth = 3f)
                drawLine(color = Color(0xFF334155), start = mgRoad, end = airport, strokeWidth = 3f)
                
                // Draw active trips pickup/drop lines
                trips.filter { it.status == TripStatus.STARTED }.forEach { t ->
                    // Find active driver
                    val d = onlineDrivers.find { it.id == t.driverId }
                    if (d != null) {
                        // Draw line from MG road to current position
                        val ratioX = ((d.currentLng - 77.5500) / (77.7200 - 77.5500)).coerceIn(0.1, 0.9)
                        val ratioY = (1.0 - ((d.currentLat - 12.9000) / (13.0000 - 12.9000))).coerceIn(0.1, 0.9)
                        val driverPos = Offset(
                            (ratioX * mapWidth).toFloat(),
                            (ratioY * mapHeight).toFloat()
                        )
                        
                        // Draw animated travel radar link
                        drawLine(
                            color = Color(0xFFEAB308),
                            start = mgRoad,
                            end = driverPos,
                            strokeWidth = 2f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                }
            }
            
            // Compose-level overlay labels (to avoid drawing text directly in canvas which requires Native Canvas calls)
            // Let's overlay the landmarks and drivers dynamically!
            val majestic = Offset(0.25f, 0.45f)
            val mgRoad = Offset(0.45f, 0.45f)
            val indiranagar = Offset(0.65f, 0.42f)
            val whitefield = Offset(0.88f, 0.48f)
            val koramangala = Offset(0.55f, 0.65f)
            val electronicCity = Offset(0.65f, 0.88f)
            val airport = Offset(0.75f, 0.15f)
            
            val landmarkOffsets = listOf(
                "Majestic Hub" to majestic,
                "MG Road" to mgRoad,
                "Indiranagar" to indiranagar,
                "Whitefield" to whitefield,
                "Koramangala" to koramangala,
                "Electronic City" to electronicCity,
                "Int. Airport" to airport
            )
            
            // Draw landmark text labels
            landmarkOffsets.forEach { (name, pos) ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (pos.x * 280).dp, // Simple relative coordinate placement
                            y = (pos.y * 240).dp
                        )
                ) {
                    Text(
                        text = name,
                        color = Color(0xFF64748B),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
            
            // Overlay Live Drivers
            onlineDrivers.forEach { d ->
                // Standard coordinate mapping inside map boundaries
                val ratioX = ((d.currentLng - 77.5500) / (77.7200 - 77.5500)).coerceIn(0.1, 0.9)
                val ratioY = (1.0 - ((d.currentLat - 12.9000) / (13.0000 - 12.9000))).coerceIn(0.1, 0.9)
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (ratioX * 280).dp, // Relative placement inside card width
                            y = (ratioY * 220).dp
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        // Pulsing online telemetry car bubble
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEAB308))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = d.id,
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = "${d.name} (${d.id})",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Map Telemetry Header Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(Color(0xFF1E293B).copy(alpha = 0.9f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4ADE80))
                    )
                    Text(
                        text = "LIVE SATELLITE FLEET TELEMETRY",
                        color = Color(0xFF4ADE80),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                
                Text(
                    text = "${onlineDrivers.size} ACTIVE VEHICLES",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .background(Color(0xFF334155), RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
