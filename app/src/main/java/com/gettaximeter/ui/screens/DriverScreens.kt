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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gettaximeter.data.model.*
import com.gettaximeter.ui.viewmodel.TaxiViewModel
import kotlinx.coroutines.delay

@Composable
fun DriverHomeScreen(viewModel: TaxiViewModel) {
    val trips by viewModel.trips.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val driverId by viewModel.currentUserId.collectAsState()
    val driverName by viewModel.currentUserName.collectAsState()

    val currentDriver = drivers.find { it.id == driverId }
    val isOnline = currentDriver?.isOnline ?: false
    
    var enteringOtpTripId by remember { mutableStateOf<String?>(null) }
    var startOtp by remember { mutableStateOf("") }

    val myAssignedTrips = trips.filter { 
        it.driverId == driverId && (it.status == TripStatus.ASSIGNED || it.status == TripStatus.STARTED)
    }

    Scaffold(
        topBar = {
            // Elegant top bar replicating the design theme top navigation header
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) Color(0xFF4ADE80) else Color.Gray)
                                )
                                Text(
                                    text = if (isOnline) "GPS ONLINE ACTIVE" else "GPS TRANSMITTER OFFLINE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Logout / Profile block
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = driverName ?: "Driver",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "ID: $driverId",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Online Presence Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOnline) MaterialTheme.colorScheme.primaryContainer else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isOnline) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isOnline) "🟢 ACTIVE TELEMETRY ONLINE" else "⚪ IDLE TRANSMITTER OFFLINE",
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp,
                                color = if (isOnline) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Text(
                                text = if (isOnline) "Broadcasting GPS and awaiting assignments" else "Invisible to system dispatch console",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else Color.Gray
                            )
                        }

                        Switch(
                            checked = isOnline,
                            onCheckedChange = { online ->
                                driverId?.let { id ->
                                    viewModel.updateDriverPresence(id, online)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "YOUR DISPATCHED ASSIGNMENTS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (myAssignedTrips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CellTower, contentDescription = "None", tint = Color.Gray, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No active assignments assigned yet.", 
                                color = Color.Gray, 
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Dispatcher assignments appear in real-time.", 
                                color = Color.Gray.copy(alpha = 0.7f), 
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    myAssignedTrips.forEach { trip ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("TRIP ID: #${trip.id}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                        Text(
                                            text = trip.tripType.name.replace("_", " ").toUpperCase(), 
                                            color = MaterialTheme.colorScheme.primary, 
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                                DetailLine(Icons.Default.Person, "Client Name: ${trip.customerName} (${trip.customerMobile})")
                                DetailLine(Icons.Default.LocationOn, "Pickup Spot: ${trip.pickupLocation}")
                                DetailLine(Icons.Default.Navigation, "Dropoff Spot: ${trip.dropLocation}")

                                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                                if (trip.status == TripStatus.STARTED) {
                                    Button(
                                        onClick = {
                                            viewModel.selectTrip(trip)
                                            viewModel.navigateTo("live_meter")
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Speed, contentDescription = "Meter")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "REOPEN LIVE METER SCREEN", 
                                            fontWeight = FontWeight.Black,
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { enteringOtpTripId = trip.id },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "START TAXI TRIP (OTP REQUIRED)", 
                                            fontWeight = FontWeight.Black,
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // OTP Verification Dialog Overlay
            enteringOtpTripId?.let { tripId ->
                AlertDialog(
                    onDismissRequest = { enteringOtpTripId = null },
                    title = { 
                        Text(
                            text = "SECURITY TRIP VALIDATION", 
                            fontWeight = FontWeight.Black, 
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp
                        ) 
                    },
                    text = {
                        Column {
                            Text(
                                text = "Please enter the 4-digit safety authorization code provided by the dispatcher client to verify and start the meter.", 
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = startOtp,
                                onValueChange = { if (it.length <= 4) startOtp = it },
                                label = { Text("4-Digit Secure OTP") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.startTripWithOtp(tripId, startOtp)
                                enteringOtpTripId = null
                                startOtp = ""
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Verify & Release Meter", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { enteringOtpTripId = null }) {
                            Text("Dismiss", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LiveMeterScreen(viewModel: TaxiViewModel) {
    val trip by viewModel.selectedTrip.collectAsState()
    val isSimulating by viewModel.isSimulatingTravel.collectAsState()

    var radarAngle by remember { mutableStateOf(0f) }

    // Run custom map radar animations
    LaunchedEffect(isSimulating) {
        while (isSimulating) {
            radarAngle = (radarAngle + 4f) % 360f
            delay(30)
        }
    }

    if (trip == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active running trip selected.", fontWeight = FontWeight.Bold)
        }
        return
    }

    Scaffold(
        topBar = {
            // Live meter header matching our portal theme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Car",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "LIVE METER CONSOLE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            color = Color.White
                        )
                    }
                    Badge(containerColor = Color.White.copy(alpha = 0.2f)) {
                        Text(
                            text = "TRIP #${trip!!.id}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Live Fare Big Bento Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CURRENT RUNNING FARE", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format("%.2f", trip!!.totalWithGst)}",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Includes 5% GST • Base: ₹${trip!!.baseFare}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Bento Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
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
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Timeline, contentDescription = "Distance", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("TOTAL MILEAGE", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                            Text("${trip!!.totalKm} KM", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
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
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = "Waiting", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("IDLE WAIT TIME", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                            Text("${trip!!.waitingMinutes} Mins", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High fidelity custom map preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)), // clean slate/water theme
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)
                            
                            // Draw map coordinate grids
                            drawCircle(
                                color = Color.White.copy(alpha = 0.8f),
                                radius = 100f,
                                style = Stroke(width = 2f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.5f),
                                radius = 200f,
                                style = Stroke(width = 2f)
                            )

                            // Radar lines
                            if (isSimulating) {
                                drawLine(
                                    color = Color(0xFFE11D48).copy(alpha = 0.3f), // Primary rose-600 copy
                                    start = center,
                                    end = Offset(
                                        center.x + 300f * Math.cos(Math.toRadians(radarAngle.toDouble())).toFloat(),
                                        center.y + 300f * Math.sin(Math.toRadians(radarAngle.toDouble())).toFloat()
                                    ),
                                    strokeWidth = 4f
                                )
                            }

                            // Pickup Pin
                            drawCircle(
                                color = Color(0xFFE11D48),
                                radius = 8f,
                                center = Offset(center.x - 120f, center.y + 40f)
                            )

                            // Route path
                            drawLine(
                                color = Color(0xFFE11D48),
                                start = Offset(center.x - 120f, center.y + 40f),
                                end = if (isSimulating) Offset(center.x + 20f, center.y - 10f) else Offset(center.x - 120f, center.y + 40f),
                                strokeWidth = 6f
                            )

                            // Car marker
                            drawCircle(
                                color = Color(0xFF1E293B),
                                radius = 10f,
                                center = if (isSimulating) Offset(center.x + 20f, center.y - 10f) else Offset(center.x - 120f, center.y + 40f)
                            )
                        }

                        // Map Indicator Overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Pin", tint = Color(0xFFE11D48), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSimulating) "Vehicle en route: Traveling to dropoff" else "Vehicle stationary: Map GPS locked",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Customer details list
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "ACTIVE CLIENT & TRIP DETAILS", 
                            fontWeight = FontWeight.Black, 
                            fontStyle = FontStyle.Italic,
                            fontSize = 11.sp, 
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DetailLine(Icons.Default.Person, "Client Name: ${trip!!.customerName}")
                        DetailLine(Icons.Default.Phone, "Mobile Number: ${trip!!.customerMobile}")
                        DetailLine(Icons.Default.LocationOn, "Pickup Place: ${trip!!.pickupLocation}")
                        DetailLine(Icons.Default.Navigation, "Dropoff Place: ${trip!!.dropLocation}")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Simulation Control
                Button(
                    onClick = { viewModel.toggleTravelSimulation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSimulating) Color(0xFFEA580C) else Color(0xFF16A34A)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Simulate"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = (if (isSimulating) "PAUSE GPS AUTO SIMULATOR" else "ACTIVATE GPS AUTO SIMULATOR").uppercase(),
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // End Trip Button
                Button(
                    onClick = { viewModel.endActiveTrip(trip!!.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "End")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "END TAXI TRIP & REALIZE RECEIPT", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DetailLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
    }
}
