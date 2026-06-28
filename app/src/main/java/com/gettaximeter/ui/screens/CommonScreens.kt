package com.gettaximeter.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gettaximeter.data.model.*
import com.gettaximeter.ui.viewmodel.TaxiViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RoleSelectScreen(viewModel: TaxiViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer decorative border resembling the frame of the mockup
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(6.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(28.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular White card enclosing Rose icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalTaxi,
                        contentDescription = "App Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "GET TAXI METER",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Enterprise Dispatch & Live Meter System",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "SELECT YOUR PORTAL ROLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful sidebar-like custom Dispatcher selection item
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo("login_admin") },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Monitor,
                                contentDescription = "Admin",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Dispatcher Portal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Central telemetry & bookings control",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful custom Driver selection item
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo("login_driver") },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Driver",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Taxi Driver App",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Verify OTP & simulate active travel meter",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: TaxiViewModel, role: UserRole) {
    var phone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.AccountCircle,
                        contentDescription = "Login Role",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (role == UserRole.ADMIN) "DISPATCHER SIGN IN" else "DRIVER AUTHORIZATION",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Verify using Mobile number and standard OTP",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("+91 XXXXX XXXXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isOtpSent
                )

                if (isOtpSent) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("Enter 6-digit OTP Code") },
                        placeholder = { Text("123456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!isOtpSent) {
                            if (phone.isNotBlank()) {
                                viewModel.verifyPhoneForOtp(phone, role) {
                                    isOtpSent = true
                                }
                            }
                        } else {
                            viewModel.login(phone, otpCode, role)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = (if (!isOtpSent) "Send Verification Code" else "Verify & Continue").toUpperCase(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { viewModel.navigateTo("role_select") }) {
                    Text("Cancel and Go Back", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ReceiptScreen(viewModel: TaxiViewModel, trip: Trip) {
    val durationMin = if (trip.startTimestamp != null && trip.endTimestamp != null) {
        ((trip.endTimestamp!! - trip.startTimestamp!!) / 60000).toInt()
    } else 0

    val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val startTimeStr = trip.startTimestamp?.let { format.format(Date(it)) } ?: "N/A"
    val endTimeStr = trip.endTimestamp?.let { format.format(Date(it)) } ?: "N/A"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "TRIP COMPLETED SUCCESSFULLY",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = Color(0xFF2E7D32)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Digital Receipt Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GET TAXI METER",
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                    Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                        Text(
                            text = "#${trip.id}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))

                // Detail Items
                ReceiptRow("Customer Name", trip.customerName)
                ReceiptRow("Mobile Number", trip.customerMobile)
                ReceiptRow("Assigned Driver", trip.driverName)
                ReceiptRow("Trip Tariff Type", trip.tripType.name.replace("_", " "))
                
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))

                ReceiptRow("Pickup Place", trip.pickupLocation)
                ReceiptRow("Dropoff Place", trip.dropLocation)
                ReceiptRow("Start Time", startTimeStr)
                ReceiptRow("End Time", endTimeStr)

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))

                Text("METRICS SUMMARY", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                ReceiptRow("Total Traveled Distance", "${trip.totalKm} KM")
                ReceiptRow("Total Clock Duration", "$durationMin Minutes")
                ReceiptRow("Total Speed Stops Waiting", "${trip.waitingMinutes} Minutes")

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))

                Text("TARIFF BREAKDOWN", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                ReceiptRow("Base Minimum Fare", "₹${trip.baseFare}")
                ReceiptRow("Distance Charges (Fare / KM)", "₹${trip.farePerKm}")
                ReceiptRow("Waiting Charge per Minute", "₹${trip.waitingChargePerMin}")
                ReceiptRow("Subtotal Meter Fare", "₹${String.format("%.2f", trip.calculatedFare)}")
                ReceiptRow("GST tax (5%)", "₹${String.format("%.2f", trip.gstAmount)}")

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("GRAND TOTAL DUE", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text(
                        text = "₹${String.format("%.2f", trip.totalWithGst)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Receipt Sharing Options
        Button(
            onClick = { viewModel.shareToWhatsApp(trip) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)), // Vibrant green-600
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = "WhatsApp")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send Receipt via WhatsApp".toUpperCase(), fontWeight = FontWeight.Black, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.shareReceiptPdf(trip) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Print, contentDescription = "PDF")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Printable PDF".toUpperCase(), fontWeight = FontWeight.Black, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            if (viewModel.currentUserRole.value == UserRole.ADMIN) {
                viewModel.navigateTo("admin_dashboard")
            } else {
                viewModel.navigateTo("driver_home")
            }
        }) {
            Text("Close & Back to Portal Home", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.End)
    }
}
