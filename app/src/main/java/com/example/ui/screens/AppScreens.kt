package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ai.AnalyzerResult
import com.example.data.*
import com.example.pricing.GadgetPricing
import com.example.pricing.ValuationInput
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun MarketTrendGraph(
    basePriceUkUsed: Double,
    modifier: Modifier = Modifier,
    trendType: String = "UP" // "UP", "DOWN", "STABLE"
) {
    val factor3MonthsAgo = when(trendType) {
        "UP" -> 0.88
        "DOWN" -> 1.15
        else -> 0.98
    }
    val factor2MonthsAgo = when(trendType) {
        "UP" -> 0.94
        "DOWN" -> 1.08
        else -> 1.01
    }
    val factor1MonthAgo = when(trendType) {
        "UP" -> 0.97
        "DOWN" -> 1.02
        else -> 0.99
    }
    val factorCurrent = when(trendType) {
        "UP" -> 1.05
        "DOWN" -> 0.92
        else -> 1.0
    }

    val p1 = basePriceUkUsed * factor3MonthsAgo
    val p2 = basePriceUkUsed * factor2MonthsAgo
    val p3 = basePriceUkUsed * factor1MonthAgo
    val p4 = basePriceUkUsed * factorCurrent

    val prices = listOf(p1, p2, p3, p4)
    val labels = listOf("3 Mo. Ago", "2 Mo. Ago", "1 Mo. Ago", "Current")

    val maxVal = prices.maxOrNull() ?: 1.0
    val minVal = prices.minOrNull() ?: 0.0
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    val graphColor = when(trendType) {
        "UP" -> SuccessGreen
        "DOWN" -> ErrorRed
        else -> RoyalPurple
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Naira Market Fluctuation (3-Month Trend)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(graphColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when(trendType) {
                            "UP" -> "Trending +12%"
                            "DOWN" -> "Price Down -15%"
                            else -> "Stable Market"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = graphColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                val width = size.width
                val height = size.height
                val paddingX = 40f
                val paddingY = 20f

                val usableWidth = width - (2 * paddingX)
                val usableHeight = height - (2 * paddingY)

                val points = mutableListOf<Offset>()
                for (i in prices.indices) {
                    val x = paddingX + (i * (usableWidth / (prices.size - 1)))
                    val rawY = ((prices[i] - minVal) / range).toFloat()
                    val y = height - paddingY - (rawY * usableHeight)
                    points.add(Offset(x, y))
                }

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(paddingX, paddingY),
                    end = Offset(width - paddingX, paddingY),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(paddingX, height / 2),
                    end = Offset(width - paddingX, height / 2),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(paddingX, height - paddingY),
                    end = Offset(width - paddingX, height - paddingY),
                    strokeWidth = 1f
                )

                val fillPath = Path().apply {
                    moveTo(points.first().x, height - paddingY)
                    for (pt in points) {
                        lineTo(pt.x, pt.y)
                    }
                    lineTo(points.last().x, height - paddingY)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(graphColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = paddingY,
                        endY = height - paddingY
                    )
                )

                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = graphColor,
                        start = points[i],
                        end = points[i+1],
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }

                for (i in points.indices) {
                    drawCircle(
                        color = graphColor,
                        radius = 6f,
                        center = points[i]
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = points[i]
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(label, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in prices.indices) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(labels[i], fontSize = 7.sp, color = Color.Gray)
                        Text(
                            text = "₦" + String.format(Locale.US, "%,.0f", prices[i] / 1000) + "k",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

val NIGERIAN_STATES = listOf(
    "Abuja (FCT)", "Lagos", "Niger", "Rivers", "Oyo", "Kano", "Kaduna",
    "Delta", "Enugu", "Anambra", "Edo", "Kwara", "Ogun", "Ondo", "Sokoto"
)

val CATEGORIES = listOf("Phones", "Laptops", "Tablets", "Consoles", "Accessories")

fun formatNaira(amount: Double): String {
    return try {
        val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
        formatter.maximumFractionDigits = 0
        "₦" + formatter.format(amount)
    } catch (e: Exception) {
        "₦" + String.format("%.0f", amount)
    }
}

@Composable
fun MainLayout(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedTab by remember { mutableStateOf("scanner") }

    if (currentUser == null) {
        AuthScreen(viewModel = viewModel)
    } else {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    currentUserRole = currentUser?.role ?: "USER"
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (selectedTab) {
                    "scanner" -> ScannerTab(viewModel = viewModel)
                    "valuations" -> ValuationAdjusterTab()
                    "marketplace" -> MarketplaceTab(viewModel = viewModel)
                    "directory" -> ShopDirectoryTab(viewModel = viewModel)
                    "admin" -> {
                        if (currentUser?.role == "ADMIN") {
                            AdminDashboardTab(viewModel = viewModel)
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Unauthorized. Admin access only.", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    "profile" -> ProfileTab(viewModel = viewModel, onLogout = onLogout)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    currentUserRole: String
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == "scanner",
            onClick = { onTabSelected("scanner") },
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Scan") },
            label = { Text("Scan", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_scan")
        )

        NavigationBarItem(
            selected = selectedTab == "valuations",
            onClick = { onTabSelected("valuations") },
            icon = { Icon(Icons.Default.Calculate, contentDescription = "Valuer") },
            label = { Text("Valuer", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_valuer")
        )

        NavigationBarItem(
            selected = selectedTab == "marketplace",
            onClick = { onTabSelected("marketplace") },
            icon = { Icon(Icons.Default.Storefront, contentDescription = "Market") },
            label = { Text("Market", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_market")
        )

        NavigationBarItem(
            selected = selectedTab == "directory",
            onClick = { onTabSelected("directory") },
            icon = { Icon(Icons.Default.Business, contentDescription = "Shops") },
            label = { Text("Shops", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_directory")
        )

        if (currentUserRole == "ADMIN") {
            NavigationBarItem(
                selected = selectedTab == "admin",
                onClick = { onTabSelected("admin") },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Admin") },
                label = { Text("Admin", fontSize = 11.sp) },
                modifier = Modifier.testTag("nav_admin")
            )
        }

        NavigationBarItem(
            selected = selectedTab == "profile",
            onClick = { onTabSelected("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp) },
            modifier = Modifier.testTag("nav_profile")
        )
    }
}

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedState by remember { mutableStateOf(NIGERIAN_STATES[0]) }
    var city by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("USER") }

    val authError by viewModel.authError.collectAsState()
    var stateDropdownExpanded by remember { mutableStateOf(false) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        Color(0xFF430E73)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Gadget Valuer NG",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (isSignUp) "Register Trade Account" else "Nigerian Resale Marketplace & AI Scanner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                authError?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (isSignUp) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("auth_name_input"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("auth_email_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                if (isSignUp) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number (+234...)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        OutlinedTextField(
                            value = selectedState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { stateDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = stateDropdownExpanded,
                            onDismissRequest = { stateDropdownExpanded = false }
                        ) {
                            NIGERIAN_STATES.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        selectedState = state
                                        stateDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        singleLine = true
                    )

                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        OutlinedTextField(
                            value = selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account Type") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { roleDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = roleDropdownExpanded,
                            onDismissRequest = { roleDropdownExpanded = false }
                        ) {
                            listOf("USER", "SHOP", "VENDOR").forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        selectedRole = role
                                        roleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("auth_pass_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Button(
                    onClick = {
                        if (isSignUp) {
                            viewModel.registerUser(
                                fullName, email, phone, password, selectedState, city, selectedRole
                            )
                        } else {
                            viewModel.loginUser(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        if (isSignUp) "Create Account" else "Log In securely",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { isSignUp = !isSignUp },
                    modifier = Modifier.testTag("auth_toggle_button")
                ) {
                    Text(
                        text = if (isSignUp) "Already have an account? Sign In" else "New to Gadget Valuer? Create account",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "* Entering 'kheenganaz@gmail.com' logs in as ADMIN automatically.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ScannerTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isScanning by viewModel.isScanning.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val userScans by viewModel.userScans.collectAsState()

    var manualQuery by remember { mutableStateOf("") }
    var showScanSelectDialog by remember { mutableStateOf(false) }

    var selectedCompareLeft by remember { mutableStateOf<GadgetScanEntity?>(null) }
    var selectedCompareRight by remember { mutableStateOf<GadgetScanEntity?>(null) }
    var showCompareDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "AI Gadget Scanner",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Scan gadgets instantly or estimate value with Nigerian market intelligence",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Initiate New Scan Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showScanSelectDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("scan_camera"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Camera Scan", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { showScanSelectDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("scan_upload"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Upload Image", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "— OR MANUAL SEARCH —",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = manualQuery,
                    onValueChange = { manualQuery = it },
                    placeholder = { Text("Describe model (e.g., iPhone 15 Pro)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_manual_input"),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (manualQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.scanGadget(null, manualQuery)
                                manualQuery = ""
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Submit")
                            }
                        }
                    },
                    singleLine = true
                )
            }
        }

        if (isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("AI analyzing model & valuation data in Nigeria...", fontWeight = FontWeight.Medium)
                }
            }
        }

        if (userScans.size >= 2) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Crosscompare Devices",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Compare features & market value side-by-side",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            var expLeft by remember { mutableStateOf(false) }
                            Button(
                                onClick = { expLeft = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.2f), contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    selectedCompareLeft?.gadgetName ?: "Select Left",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp
                                )
                            }
                            DropdownMenu(expanded = expLeft, onDismissRequest = { expLeft = false }) {
                                userScans.forEach { scan ->
                                    DropdownMenuItem(
                                        text = { Text(scan.gadgetName) },
                                        onClick = {
                                            selectedCompareLeft = scan
                                            expLeft = false
                                        }
                                    )
                                }
                            }
                        }

                        Text("VS", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

                        Box(modifier = Modifier.weight(1f)) {
                            var expRight by remember { mutableStateOf(false) }
                            Button(
                                onClick = { expRight = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.2f), contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    selectedCompareRight?.gadgetName ?: "Select Right",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp
                                )
                            }
                            DropdownMenu(expanded = expRight, onDismissRequest = { expRight = false }) {
                                userScans.forEach { scan ->
                                    DropdownMenuItem(
                                        text = { Text(scan.gadgetName) },
                                        onClick = {
                                            selectedCompareRight = scan
                                            expRight = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (selectedCompareLeft != null && selectedCompareRight != null) {
                                showCompareDialog = true
                            } else {
                                Toast.makeText(context, "Select two devices to compare!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Compare Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text(
            text = "Your Recent Scan History",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (userScans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history. Run an AI scan to track valuations.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                userScans.forEach { scan ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = scan.gadgetName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Conf: ${scan.confidenceScore.toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    modifier = Modifier
                                        .background(SuccessGreen.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "Brand: ${scan.brand} | Model: ${scan.model} | Storage: ${scan.storage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                            )

                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("UK Used Price", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        "${formatNaira(scan.ukUsedMin)} - ${formatNaira(scan.ukUsedMax)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Column {
                                    Text("locally Fairly Used", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        "${formatNaira(scan.fairlyUsedMin)} - ${formatNaira(scan.fairlyUsedMax)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = WarningOrange
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    "Display" to scan.screenCracksScore,
                                    "Bezel" to scan.bezelDamageScore,
                                    "Port" to scan.portWearScore,
                                    "Body" to scan.cosmeticScratchesScore
                                ).forEach { (label, score) ->
                                    val scoreColor = when {
                                        score >= 90.0 -> SuccessGreen
                                        score >= 75.0 -> WarningOrange
                                        else -> ErrorRed
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(scoreColor.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, scoreColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "$label: ${score.toInt()}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = scoreColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showScanSelectDialog) {
        Dialog(onDismissRequest = { showScanSelectDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AI Camera Scanning Simulator",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Select a demo device template to simulate AI recognition inside the development workspace:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    val demoDeviceTemplates = listOf(
                        "iPhone 15 Pro Max" to "Apple dynamic scan",
                        "Samsung Galaxy S23 Ultra" to "Samsung flagship check",
                        "Apple MacBook Air M1" to "Retina Display valuation",
                        "HP Pavilion 15 Core i7" to "Notebook performance check"
                    )

                    demoDeviceTemplates.forEach { (name, label) ->
                        OutlinedButton(
                            onClick = {
                                viewModel.scanGadget(null, name)
                                showScanSelectDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(name, fontWeight = FontWeight.Bold)
                                Text(label, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = { showScanSelectDialog = false }) {
                        Text("Cancel Operation", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    scanResult?.let { result ->
        Dialog(onDismissRequest = { viewModel.clearResult() }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 680.dp)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Scan Result: AI Success",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.clearResult() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = result.gadgetName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Category: ${result.category} | Confidence: ${result.confidenceScore.toInt()}%",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Storage" to result.storage,
                            "Color Tint" to result.color,
                            "Condition" to result.estimatedCondition
                        ).forEach { (label, value) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, fontSize = 10.sp, color = Color.Gray)
                                    Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("AI Physical Condition Diagnosis", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Confidence on wear attributes: ${result.conditionConfidenceScore.toInt()}%", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        val conditionMetrics = listOf(
                            Triple("Screen Cracks Integrity", result.screenCracksScore, "screen_tag"),
                            Triple("Bezel & Framing Status", result.bezelDamageScore, "bezel_tag"),
                            Triple("USB/Connector Port Wear", result.portWearScore, "port_tag"),
                            Triple("Cosmetic Scratches Level", result.cosmeticScratchesScore, "scratches_tag")
                        )
                        conditionMetrics.forEach { (metric, score, testTag) ->
                            val scoreColor = when {
                                score >= 90.0 -> SuccessGreen
                                score >= 75.0 -> WarningOrange
                                else -> ErrorRed
                            }
                            Column(modifier = Modifier.fillMaxWidth().testTag(testTag)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(metric, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("${score.toInt()}/100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = scoreColor)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = (score / 100.0).toFloat(),
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = scoreColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Estimated Resale Pricing (Nigeria)", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LightLavender)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("London/UK Used Range", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                            Text(
                                text = "${formatNaira(result.ukUsedMin)} - ${formatNaira(result.ukUsedMax)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Locally Fairly Used", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                text = "${formatNaira(result.fairlyUsedMin)} - ${formatNaira(result.fairlyUsedMax)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = WarningOrange
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Brand New Box Retail", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                text = "${formatNaira(result.brandNewMin)} - ${formatNaira(result.brandNewMax)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SuccessGreen
                            )
                        }
                    }

                    Text("Market Trend", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(result.marketTrend, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

                    val trendType = when {
                        result.marketTrend.lowercase().contains("high") || result.marketTrend.lowercase().contains("trending") || result.marketTrend.lowercase().contains("premium") -> "UP"
                        result.marketTrend.lowercase().contains("dropping") || result.marketTrend.lowercase().contains("decline") || result.marketTrend.lowercase().contains("low") -> "DOWN"
                        else -> "STABLE"
                    }
                    MarketTrendGraph(
                        basePriceUkUsed = result.ukUsedMin,
                        trendType = trendType,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text("Common Defects to Check", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        result.commonIssues.split(";").forEach { issue ->
                            Text(
                                text = listOf(issue).firstOrNull() ?: "",
                                fontSize = 11.sp,
                                color = ErrorRed,
                                modifier = Modifier
                                    .background(ErrorRed.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            )
                        }
                    }

                    Text("Important Pricing Factors", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(result.conditionFactors, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

                    Text("Trade Profit Advice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(result.bestResaleAdvice, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

                    Button(
                        onClick = { viewModel.clearResult() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Perfect, Close Summary")
                    }
                }
            }
        }
    }

    if (showCompareDialog) {
        Dialog(onDismissRequest = { showCompareDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val left = selectedCompareLeft!!
                    val right = selectedCompareRight!!

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Comparison Hub", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { showCompareDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1.0f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(left.gadgetName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                            Text("UK Used: ${formatNaira((left.ukUsedMin + left.ukUsedMax) / 2)}", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                        Text(" VS ", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(top = 8.dp))
                        Column(modifier = Modifier.weight(1.0f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(right.gadgetName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black, textAlign = TextAlign.Center)
                            Text("UK Used: ${formatNaira((right.ukUsedMin + right.ukUsedMax) / 2)}", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val comparisonRows = listOf(
                        "Brand" to (left.brand to right.brand),
                        "Category" to (left.category to right.category),
                        "Storage Room" to (left.storage to right.storage),
                        "UK Used Max" to (formatNaira(left.ukUsedMax) to formatNaira(right.ukUsedMax)),
                        "Fairly Used Max" to (formatNaira(left.fairlyUsedMax) to formatNaira(right.fairlyUsedMax)),
                        "Brand New Max" to (formatNaira(left.brandNewMax) to formatNaira(right.brandNewMax))
                    )

                    comparisonRows.forEach { (label, values) ->
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    values.first,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    values.second,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                    color = Color.Black
                                )
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightLavender),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Resale Comparison Advice:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            val recommendation = if (left.ukUsedMin > right.ukUsedMin) {
                                "${left.gadgetName} holds a higher capital preservation threshold in Nigeria's secondary markets."
                            } else {
                                "${right.gadgetName} holds a higher resale preservation profit, presenting faster transactions."
                            }
                            Text(recommendation, fontSize = 11.sp, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showCompareDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close Comparison")
                    }
                }
            }
        }
    }
}

@Composable
fun ValuationAdjusterTab() {
    var brand by remember { mutableStateOf("Apple") }
    var age by remember { mutableStateOf(1) }
    var storage by remember { mutableStateOf(128) }
    var condition by remember { mutableStateOf("Excellent") }
    var batteryHealth by remember { mutableStateOf(85) }
    var screenCondition by remember { mutableStateOf("Perfect") }
    var marketDemand by remember { mutableStateOf("High") }
    var repairRisk by remember { mutableStateOf("Low") }

    var dBrandExp by remember { mutableStateOf(false) }
    var dConditionExp by remember { mutableStateOf(false) }
    var dScreenExp by remember { mutableStateOf(false) }
    var dDemandExp by remember { mutableStateOf(false) }

    val basePriceUkUsedMap = mapOf(
        "Apple" to 420000.0,
        "Samsung" to 380000.0,
        "HP" to 300000.0,
        "Dell" to 320000.0,
        "Xiaomi" to 150000.0,
        "Transsion" to 95000.0
    )

    val basePrice = basePriceUkUsedMap[brand] ?: 200000.0
    val result = GadgetPricing.calculateValuation(
        ValuationInput(
            basePriceUkUsed = basePrice,
            brand = brand,
            ageInYears = age,
            storageGb = storage,
            condition = condition,
            batteryHealth = batteryHealth,
            screenCondition = screenCondition,
            marketDemand = marketDemand,
            repairRisk = repairRisk
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Depreciation Fine-Tuner",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Calculate dynamic secondary market values based on hardware conditions",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CUSTOM ENGINE VALUATION RESULT (₦)",
                    color = LightLavender,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${formatNaira(result.finalEstimatedMin)} - ${formatNaira(result.finalEstimatedMax)}",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = result.marketAdvice,
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text("Fine-Tune Hardware Conditions:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Brand Category") },
                        trailingIcon = { IconButton(onClick = { dBrandExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(expanded = dBrandExp, onDismissRequest = { dBrandExp = false }) {
                        basePriceUkUsedMap.keys.forEach { name ->
                            DropdownMenuItem(text = { Text(name) }, onClick = {
                                brand = name
                                dBrandExp = false
                            })
                        }
                    }
                }

                Text("Storage Capacity:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(64, 128, 256, 512).forEach { cap ->
                        val selected = storage == cap
                        OutlinedButton(
                            onClick = { storage = cap },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (selected) Color.White else Color.Black
                            ),
                            border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.LightGray)
                        ) {
                            Text("${cap}GB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Device Age (Years in use):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("$age Year(s)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = age.toFloat(),
                    onValueChange = { age = it.toInt() },
                    valueRange = 0f..5f,
                    steps = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Battery Health Lifespan:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("$batteryHealth%", fontWeight = FontWeight.Bold, color = if (batteryHealth >= 80) SuccessGreen else ErrorRed)
                }
                Slider(
                    value = batteryHealth.toFloat(),
                    onValueChange = { batteryHealth = it.toInt() },
                    valueRange = 50f..100f,
                    steps = 50,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Physical Condition") },
                        trailingIcon = { IconButton(onClick = { dConditionExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(expanded = dConditionExp, onDismissRequest = { dConditionExp = false }) {
                        listOf("Pristine", "Excellent", "Good", "Fair", "Broken").forEach { cond ->
                            DropdownMenuItem(text = { Text(cond) }, onClick = {
                                condition = cond
                                dConditionExp = false
                            })
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    OutlinedTextField(
                        value = screenCondition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Display Glass Condition") },
                        trailingIcon = { IconButton(onClick = { dScreenExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(expanded = dScreenExp, onDismissRequest = { dScreenExp = false }) {
                        listOf("Perfect", "Minor Scratches", "Cracked Screen", "Dead Spots").forEach { scr ->
                            DropdownMenuItem(text = { Text(scr) }, onClick = {
                                screenCondition = scr
                                dScreenExp = false
                            })
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = marketDemand,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Naira Market Request Frequency") },
                        trailingIcon = { IconButton(onClick = { dDemandExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(expanded = dDemandExp, onDismissRequest = { dDemandExp = false }) {
                        listOf("Very High", "High", "Stable", "Low").forEach { dem ->
                            DropdownMenuItem(text = { Text(dem) }, onClick = {
                                marketDemand = dem
                                dDemandExp = false
                            })
                        }
                    }
                }
            }
        }

        Text("Valuation Adjustments:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            result.adjustmentBreakdown.forEach { text ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (text.contains("+")) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (text.contains("+")) SuccessGreen else ErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun MarketplaceTab(viewModel: MainViewModel) {
    val listings by viewModel.allListings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var stateFilter by remember { mutableStateOf("All Nigeria") }
    var catFilter by remember { mutableStateOf("All Categories") }
    var brandQuery by remember { mutableStateOf("") }

    var filterExpandedState by remember { mutableStateOf(false) }
    var filterExpandedCat by remember { mutableStateOf(false) }

    var showPostFormDialog by remember { mutableStateOf(false) }
    var selectedListingDetail by remember { mutableStateOf<GadgetListingEntity?>(null) }

    var postTitle by remember { mutableStateOf("") }
    var postBrand by remember { mutableStateOf("") }
    var postModel by remember { mutableStateOf("") }
    var postCategory by remember { mutableStateOf(CATEGORIES[0]) }
    var postCondition by remember { mutableStateOf("London Used") }
    var postPrice by remember { mutableStateOf("") }
    var postState by remember { mutableStateOf(currentUser?.state ?: NIGERIAN_STATES[0]) }
    var postCity by remember { mutableStateOf(currentUser?.city ?: "") }
    var postDesc by remember { mutableStateOf("") }
    var postPhone by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var postWhatsApp by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }

    var catDropdownExp by remember { mutableStateOf(false) }
    var stateDropdownExp by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val shopsList by viewModel.allShops.collectAsState()
    val unapprovedShopNames = remember(shopsList) {
        shopsList.filter { it.status != "APPROVED" }.map { it.shopName.lowercase().trim() }
    }

    val filteredListings = listings.filter { listing ->
        val matchState = stateFilter == "All Nigeria" || listing.state.lowercase(Locale.ROOT).contains(stateFilter.lowercase(Locale.ROOT))
        val matchCat = catFilter == "All Categories" || listing.category.lowercase(Locale.ROOT).contains(catFilter.lowercase(Locale.ROOT))
        val matchBrand = brandQuery.isEmpty() || listing.brand.lowercase(Locale.ROOT).contains(brandQuery.lowercase(Locale.ROOT))
        val isApproved = listing.status == "APPROVED"
        val isFromUnapprovedShop = unapprovedShopNames.contains(listing.posterName.lowercase().trim())
        matchState && matchCat && matchBrand && isApproved && !isFromUnapprovedShop
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Naira Marketplace",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Direct buyer-meets-shop listings in Nigeria", fontSize = 11.sp, color = Color.Gray)
            }

            val satisfiesPostRole = currentUser?.role == "ADMIN" || currentUser?.role == "VENDOR" || currentUser?.role == "SHOP"
            if (satisfiesPostRole) {
                Button(
                    onClick = { showPostFormDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("post_listing_button")
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sell", fontSize = 12.sp)
                }
            }
        }

        OutlinedTextField(
            value = brandQuery,
            onValueChange = { brandQuery = it },
            placeholder = { Text("Search brand (e.g. Apple)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { filterExpandedState = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stateFilter, overflow = TextOverflow.Ellipsis, maxLines = 1, fontSize = 11.sp)
                }
                DropdownMenu(expanded = filterExpandedState, onDismissRequest = { filterExpandedState = false }) {
                    DropdownMenuItem(text = { Text("All Nigeria") }, onClick = {
                        stateFilter = "All Nigeria"
                        filterExpandedState = false
                    })
                    NIGERIAN_STATES.forEach { state ->
                        DropdownMenuItem(text = { Text(state) }, onClick = {
                            stateFilter = state
                            filterExpandedState = false
                        })
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { filterExpandedCat = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(catFilter, overflow = TextOverflow.Ellipsis, maxLines = 1, fontSize = 11.sp)
                }
                DropdownMenu(expanded = filterExpandedCat, onDismissRequest = { filterExpandedCat = false }) {
                    DropdownMenuItem(text = { Text("All Categories") }, onClick = {
                        catFilter = "All Categories"
                        filterExpandedCat = false
                    })
                    CATEGORIES.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = {
                            catFilter = cat
                            filterExpandedCat = false
                        })
                    }
                }
            }
        }

        if (filteredListings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No items posted in this region.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredListings) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedListingDetail = item },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.gadgetName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = item.condition,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalPurple,
                                    modifier = Modifier
                                        .background(RoyalPurple.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "Brand: ${item.brand} | Model: ${item.model} | Seller: ${item.posterName}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                            )

                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Listing Price", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        formatNaira(item.price),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = SuccessGreen
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.LightGray)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${item.city}, ${item.state}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPostFormDialog) {
        Dialog(onDismissRequest = { showPostFormDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Post Market Listing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showPostFormDialog = false }) { Icon(Icons.Default.Close, null) }
                    }

                    OutlinedTextField(value = postTitle, onValueChange = { postTitle = it }, label = { Text("Listing Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                    OutlinedTextField(value = postBrand, onValueChange = { postBrand = it }, label = { Text("Brand") }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                    OutlinedTextField(value = postModel, onValueChange = { postModel = it }, label = { Text("Model Spec") }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))

                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        OutlinedTextField(
                            value = postCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { IconButton(onClick = { catDropdownExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = catDropdownExp, onDismissRequest = { catDropdownExp = false }) {
                            CATEGORIES.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat) }, onClick = {
                                    postCategory = cat
                                    catDropdownExp = false
                                })
                            }
                        }
                    }

                    OutlinedTextField(value = postCondition, onValueChange = { postCondition = it }, label = { Text("Condition (e.g. London Used)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                    OutlinedTextField(value = postPrice, onValueChange = { postPrice = it }, label = { Text("Price (₦)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))

                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        OutlinedTextField(
                            value = postState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State") },
                            trailingIcon = { IconButton(onClick = { stateDropdownExp = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = stateDropdownExp, onDismissRequest = { stateDropdownExp = false }) {
                            NIGERIAN_STATES.forEach { state ->
                                DropdownMenuItem(text = { Text(state) }, onClick = {
                                    postState = state
                                    stateDropdownExp = false
                                })
                            }
                        }
                    }

                    OutlinedTextField(value = postCity, onValueChange = { postCity = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                    OutlinedTextField(value = postDesc, onValueChange = { postDesc = it }, label = { Text("Item Description") }, modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 10.dp), maxLines = 3)
                    OutlinedTextField(value = postPhone, onValueChange = { postPhone = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
                    OutlinedTextField(value = postWhatsApp, onValueChange = { postWhatsApp = it }, label = { Text("WhatsApp Number") }, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))

                    Button(
                        onClick = {
                            val priceParsed = postPrice.toDoubleOrNull() ?: 0.0
                            if (postTitle.isNotBlank() && priceParsed > 0.0) {
                                viewModel.addListing(
                                    postTitle, postBrand, postModel, postCategory, postCondition, priceParsed,
                                    postState, postCity, postDesc, postPhone, postWhatsApp
                                )
                                showPostFormDialog = false
                            } else {
                                Toast.makeText(context, "Fill name and price values correctly!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Publish Market Listing", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    selectedListingDetail?.let { listing ->
        Dialog(onDismissRequest = { selectedListingDetail = null }) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Listing Marketplace Unit",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { selectedListingDetail = null }) { Icon(Icons.Default.Close, null) }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(listing.gadgetName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RoyalPurple)
                    Text("Price Tag: ${formatNaira(listing.price)}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = SuccessGreen, modifier = Modifier.padding(vertical = 4.dp))

                    Text("Device Technical Details:", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("• Brand Name: ${listing.brand}", fontSize = 12.sp)
                            Text("• Code Model: ${listing.model}", fontSize = 12.sp)
                            Text("• Category: ${listing.category}", fontSize = 12.sp)
                            Text("• Shape/Condition: ${listing.condition}", fontSize = 12.sp)
                            Text("• Host Location: ${listing.city}, ${listing.state} State", fontSize = 12.sp)
                        }
                    }

                    Text("Vendor Description Details:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(listing.description, fontSize = 13.sp, color = Color.DarkGray, modifier = Modifier.padding(vertical = 6.dp))

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))

                    Text("Contact Trader (${listing.posterName}):", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.contactPhone}"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Contact: ${listing.contactPhone}", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                try {
                                    val url = "https://api.whatsapp.com/send?phone=${listing.contactWhatsApp}&text=Hello, saw your listing for ${listing.gadgetName} on Gadget Valuer."
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp No: ${listing.contactWhatsApp}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 13.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            viewModel.reportListing(listing.id, "Inappropriate / Suspicion of Fraud")
                            Toast.makeText(context, "Listing reported to NG Quality Admins", Toast.LENGTH_SHORT).show()
                            selectedListingDetail = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Report this Listing", color = ErrorRed, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ShopDirectoryTab(viewModel: MainViewModel) {
    val shops by viewModel.allShops.collectAsState()
    val vendors by viewModel.allVendors.collectAsState()

    var activeSubTab by remember { mutableStateOf("shops") }
    var selectedState by remember { mutableStateOf("Lagos") }

    var stateDropdownExpanded by remember { mutableStateOf(false) }
    var showRegisterShopModal by remember { mutableStateOf(false) }
    var showRegisterVendorModal by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var regName by remember { mutableStateOf("") }
    var regOwner by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regWhatsApp by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regState by remember { mutableStateOf(NIGERIAN_STATES[0]) }
    var regCity by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regCats by remember { mutableStateOf("") }

    var regStateDropdownExpanded by remember { mutableStateOf(false) }

    val filteredShops = shops.filter { shop ->
        shop.status == "APPROVED" && shop.state.lowercase(Locale.ROOT).trim().contains(selectedState.lowercase(Locale.ROOT).trim())
    }

    val filteredVendors = vendors.filter { vendor ->
        vendor.status == "APPROVED" && vendor.state.lowercase(Locale.ROOT).trim().contains(selectedState.lowercase(Locale.ROOT).trim())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Verified Hubs",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Zero tolerance directories by Nigerian State", fontSize = 11.sp, color = Color.Gray)
            }

            Button(
                onClick = {
                    if (activeSubTab == "shops") {
                        showRegisterShopModal = true
                    } else {
                        showRegisterVendorModal = true
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Register")
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            OutlinedButton(
                onClick = { activeSubTab = "shops" },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (activeSubTab == "shops") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (activeSubTab == "shops") Color.White else Color.Black
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("Official Shops")
            }

            OutlinedButton(
                onClick = { activeSubTab = "vendors" },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (activeSubTab == "vendors") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (activeSubTab == "vendors") Color.White else Color.Black
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("Independent Agents")
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            OutlinedTextField(
                value = "Active State: $selectedState",
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = { IconButton(onClick = { stateDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            DropdownMenu(expanded = stateDropdownExpanded, onDismissRequest = { stateDropdownExpanded = false }) {
                NIGERIAN_STATES.forEach { state ->
                    DropdownMenuItem(text = { Text(state) }, onClick = {
                        selectedState = state
                        stateDropdownExpanded = false
                    })
                }
            }
        }

        if (activeSubTab == "shops") {
            if (filteredShops.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No verified shops in $selectedState State yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredShops) { shop ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(shop.shopName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalPurple)
                                    Surface(
                                        color = SuccessGreen.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "VERIFIED SHOP",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    "Owner: ${shop.ownerName} | Address: ${shop.address}",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Text(
                                    "Specialties: ${shop.categoriesSold}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shop.phone}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Call: ${shop.phone}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val url = "https://api.whatsapp.com/send?phone=${shop.whatsApp}&text=Hello, saw shop '${shop.shopName}' on Gadget Valuer."
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "WhatsApp: ${shop.whatsApp}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WhatsApp", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (filteredVendors.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No approved agents in $selectedState State yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredVendors) { ven ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ven.vendorName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "LICENSED AGENT",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    "Host Node: ${ven.address}",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Text(
                                    "Deals In: ${ven.categoriesSold}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ven.phone}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Call: ${ven.phone}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Call Agent", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val url = "https://api.whatsapp.com/send?phone=${ven.whatsApp}&text=Hello, saw your profile '${ven.vendorName}' on Gadget Valuer."
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "WhatsApp: ${ven.whatsApp}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Chat Agent", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRegisterShopModal) {
        Dialog(onDismissRequest = { showRegisterShopModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Apply as Verified Shop", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showRegisterShopModal = false }) { Icon(Icons.Default.Close, null) }
                    }

                    OutlinedTextField(value = regName, onValueChange = { regName = it }, label = { Text("Shop Business Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regOwner, onValueChange = { regOwner = it }, label = { Text("Owner Full Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regPhone, onValueChange = { regPhone = it }, label = { Text("Shop Phone No") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regWhatsApp, onValueChange = { regWhatsApp = it }, label = { Text("Shop WhatsApp No") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regEmail, onValueChange = { regEmail = it }, label = { Text("Business Email") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))

                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = regState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State Boundary Location") },
                            trailingIcon = { IconButton(onClick = { regStateDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = regStateDropdownExpanded, onDismissRequest = { regStateDropdownExpanded = false }) {
                            NIGERIAN_STATES.forEach { st ->
                                DropdownMenuItem(text = { Text(st) }, onClick = {
                                    regState = st
                                    regStateDropdownExpanded = false
                                })
                            }
                        }
                    }

                    OutlinedTextField(value = regCity, onValueChange = { regCity = it }, label = { Text("City Node") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regAddress, onValueChange = { regAddress = it }, label = { Text("Physical Plot Address") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regCats, onValueChange = { regCats = it }, label = { Text("Categories sold (e.g. Phones;Laptops)") }, placeholder = { Text("Split with Semicolon") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))

                    Button(
                        onClick = {
                            if (regName.isNotBlank() && regOwner.isNotBlank() && regPhone.isNotBlank()) {
                                viewModel.registerShop(regName, regOwner, regPhone, regWhatsApp, regEmail, regState, regCity, regAddress, regCats)
                                Toast.makeText(context, "Application submitted for NG review!", Toast.LENGTH_LONG).show()
                                showRegisterShopModal = false
                                regName = ""; regOwner = ""; regPhone = ""; regWhatsApp = ""; regEmail = ""; regCity = ""; regAddress = ""; regCats = ""
                            } else {
                                Toast.makeText(context, "Please enter all required business fields!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply for Shop License", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showRegisterVendorModal) {
        Dialog(onDismissRequest = { showRegisterVendorModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Register Independent Agent", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showRegisterVendorModal = false }) { Icon(Icons.Default.Close, null) }
                    }

                    OutlinedTextField(value = regName, onValueChange = { regName = it }, label = { Text("Agent Business Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regOwner, onValueChange = { regOwner = it }, label = { Text("Owner Identification Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regPhone, onValueChange = { regPhone = it }, label = { Text("Contact Phone") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regWhatsApp, onValueChange = { regWhatsApp = it }, label = { Text("WhatsApp Line") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regEmail, onValueChange = { regEmail = it }, label = { Text("Personal Email Account") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))

                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        OutlinedTextField(
                            value = regState,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State Area") },
                            trailingIcon = { IconButton(onClick = { regStateDropdownExpanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = regStateDropdownExpanded, onDismissRequest = { regStateDropdownExpanded = false }) {
                            NIGERIAN_STATES.forEach { st ->
                                DropdownMenuItem(text = { Text(st) }, onClick = {
                                    regState = st
                                    regStateDropdownExpanded = false
                                })
                            }
                        }
                    }

                    OutlinedTextField(value = regCity, onValueChange = { regCity = it }, label = { Text("City Node") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regAddress, onValueChange = { regAddress = it }, label = { Text("Host Address (CAC/ID)") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = regCats, onValueChange = { regCats = it }, label = { Text("Deals In (e.g. Android;Playstation)") }, placeholder = { Text("Split with Semicolon") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))

                    Button(
                        onClick = {
                            if (regName.isNotBlank() && regOwner.isNotBlank() && regPhone.isNotBlank()) {
                                viewModel.registerVendor(regName, regOwner, regPhone, regWhatsApp, regEmail, regState, regCity, regAddress, regCats)
                                Toast.makeText(context, "Agent application forwarded to Admins!", Toast.LENGTH_LONG).show()
                                showRegisterVendorModal = false
                                regName = ""; regOwner = ""; regPhone = ""; regWhatsApp = ""; regEmail = ""; regCity = ""; regAddress = ""; regCats = ""
                            } else {
                                Toast.makeText(context, "Please enter all required vendor fields!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Register Agent", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboardTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    // Observe StateFlows from ViewModel
    val users by viewModel.allUsers.collectAsState()
    val shops by viewModel.allShops.collectAsState()
    val vendors by viewModel.allVendors.collectAsState()
    val listings by viewModel.allListings.collectAsState()
    val reports by viewModel.allReports.collectAsState()
    val supportMessages by viewModel.supportMessages.collectAsState()
    val adminActionLogs by viewModel.adminActionLogs.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val allScans by viewModel.allScans.collectAsState()

    // Tab state (supporting all required 9 tabs)
    var activeAdminTab by remember { mutableStateOf("overview") }

    // UNIVERSAL CONFIRMATION DIALOG STATE
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmTitle by remember { mutableStateOf("") }
    var confirmMessage by remember { mutableStateOf("") }
    var onConfirmAction by remember { mutableStateOf<() -> Unit>({}) }

    // UNIVERSAL LOCATION EDIT DIALOG STATE
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationTargetId by remember { mutableStateOf<Int?>(null) }
    var locationTargetType by remember { mutableStateOf("") } // "SHOP" or "VENDOR"
    var editStateVal by remember { mutableStateOf("") }
    var editCityVal by remember { mutableStateOf("") }

    // PAGINATION & FILTER STATES
    // 1. Users
    var userSearchQuery by remember { mutableStateOf("") }
    var userRoleFilter by remember { mutableStateOf("All") }
    var pageUsers by remember { mutableStateOf(0) }

    // 2. Shops
    var shopSearchQuery by remember { mutableStateOf("") }
    var shopStatusFilter by remember { mutableStateOf("All") }
    var pageShops by remember { mutableStateOf(0) }

    // 3. Vendors
    var vendorSearchQuery by remember { mutableStateOf("") }
    var vendorStatusFilter by remember { mutableStateOf("All") }
    var pageVendors by remember { mutableStateOf(0) }

    // 4. Listings
    var listingSearchQuery by remember { mutableStateOf("") }
    var listingStatusFilter by remember { mutableStateOf("All") }
    var pageListings by remember { mutableStateOf(0) }

    // 5. Reports
    var reportSearchQuery by remember { mutableStateOf("") }
    var reportStatusFilter by remember { mutableStateOf("All") }
    var pageReports by remember { mutableStateOf(0) }

    // 6. Support
    var supportTabSearchQuery by remember { mutableStateOf("") }
    var supportTabStatusFilter by remember { mutableStateOf("All") }
    var pageSupportMessages by remember { mutableStateOf(0) }

    // 7. Audit
    var auditSearchQuery by remember { mutableStateOf("") }
    var pageAuditLogs by remember { mutableStateOf(0) }

    val pageSize = 5

    // Helper to request confirmations easily
    val requestConfirmation = { title: String, message: String, action: () -> Unit ->
        confirmTitle = title
        confirmMessage = message
        onConfirmAction = action
        showConfirmDialog = true
    }

    // Export CSV Helper helper
    val exportCsv = { type: String ->
        try {
            val csv = viewModel.getCsvExportData(type)
            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(csv))
            Toast.makeText(context, "$type CSV exported & copied to clipboard!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Format millisecond timestamps
    val formatTimestampLocal = { timestamp: Long ->
        try {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Unknown Date"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Secure Admin Portal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Total authority directory and configuration controls", fontSize = 11.sp, color = Color.Gray)
            }
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "ROOT CONTROL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // NAVIGATION SWITCHER (All 9 authorized admin tabs in a horizontal layout)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabsList = listOf(
                "overview" to "Overview",
                "users" to "Users",
                "shops" to "Shops",
                "vendors" to "Vendors",
                "listings" to "Listings",
                "reports" to "Abuse Reports",
                "support" to "Support",
                "settings" to "Settings",
                "audit" to "Audit Logs"
            )
            items(tabsList) { (tabCode, tabTitle) ->
                val isSelected = activeAdminTab == tabCode
                Button(
                    onClick = { activeAdminTab = tabCode },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.25f),
                        contentColor = if (isSelected) Color.White else Color.Black
                    ),
                    modifier = Modifier.testTag("admin_tab_$tabCode")
                ) {
                    Text(tabTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))

        // RENDERING TABS CONTENTS
        when (activeAdminTab) {
            "overview" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Database Overview Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    val stats = listOf(
                        Triple("Registered Platform Users", users.size.toString(), Icons.Default.Groups),
                        Triple("Active System Users", users.count { it.status == "ACTIVE" }.toString(), Icons.Default.Done),
                        Triple("Suspended Platform Users", users.count { it.status == "SUSPENDED" }.toString(), Icons.Default.Block),
                        Triple("Total Marketplace Listings", listings.size.toString(), Icons.Default.Inventory),
                        Triple("Approved Listings", listings.count { it.status == "APPROVED" }.toString(), Icons.Default.Check),
                        Triple("Active Verified Shops", shops.count { it.status == "APPROVED" }.toString(), Icons.Default.Storefront),
                        Triple("Pending Shops Review", shops.count { it.status == "PENDING" }.toString(), Icons.Default.Pending),
                        Triple("Suspended Shops", shops.count { it.status == "SUSPENDED" }.toString(), Icons.Default.Block),
                        Triple("Approved Agents (Vendors)", vendors.count { it.status == "APPROVED" }.toString(), Icons.Default.SupervisorAccount),
                        Triple("Pending Agents (Vendors)", vendors.count { it.status == "PENDING" }.toString(), Icons.Default.Pending),
                        Triple("Total Gadget Scans Runs", allScans.size.toString(), Icons.Default.CameraAlt),
                        Triple("Open Abuse Reports Waiting Review", reports.count { it.status == "PENDING" }.toString(), Icons.Default.Report)
                    )

                    GridLayout(columns = 2, items = stats) { (label, count, icon) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(count, fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Export Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { exportCsv("USERS") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Users CSV", fontSize = 10.sp)
                            }
                        }
                        Button(
                            onClick = { exportCsv("SHOPS") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Shops CSV", fontSize = 10.sp)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { exportCsv("LISTINGS") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Listings CSV", fontSize = 10.sp)
                            }
                        }
                        Button(
                            onClick = { exportCsv("REPORTS") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reports CSV", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            "users" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it; pageUsers = 0 },
                            placeholder = { Text("Search by name/email/phone", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(onClick = { exportCsv("USERS") }) {
                            Icon(Icons.Default.Download, contentDescription = "Export Users", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Role filters
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val roles = listOf("All", "Admin", "Shop", "Vendor", "User")
                        items(roles) { roleOpt ->
                            val isSelected = userRoleFilter.lowercase() == roleOpt.lowercase()
                            AssistChip(
                                onClick = { userRoleFilter = roleOpt; pageUsers = 0 },
                                label = { Text(roleOpt, fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                )
                            )
                        }
                    }

                    val filteredUsers = users.filter { u ->
                        val matchesSearch = u.fullName.lowercase().contains(userSearchQuery.lowercase()) ||
                                u.email.lowercase().contains(userSearchQuery.lowercase()) ||
                                u.phoneNumber.contains(userSearchQuery)
                        val matchesRole = userRoleFilter == "All" || u.role.uppercase() == userRoleFilter.uppercase()
                        matchesSearch && matchesRole
                    }

                    val totalPages = (filteredUsers.size + pageSize - 1) / pageSize
                    val finalPage = kotlin.math.min(pageUsers, (totalPages - 1).coerceAtLeast(0))
                    val pagedUsers = filteredUsers.drop(finalPage * pageSize).take(pageSize)

                    if (pagedUsers.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No users match selected criteria.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pagedUsers) { u ->
                                val userScansCount = allScans.count { it.userId == u.id }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(u.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Surface(
                                                color = if (u.status == "ACTIVE") SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = u.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (u.status == "ACTIVE") SuccessGreen else ErrorRed,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text("Email: ${u.email} | Phone: ${u.phoneNumber}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Location: ${u.city}, ${u.state}", fontSize = 11.sp, color = Color.Gray)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("Role: ${u.role}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                            Text("Scans Run: $userScansCount", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (u.role != "SHOP" && u.role != "ADMIN") {
                                                OutlinedButton(
                                                    onClick = {
                                                        requestConfirmation("Promote to Shop", "Are you sure you want to promote ${u.fullName} to a verified shop account?") {
                                                            viewModel.adminPromoteUser(u.id, "SHOP")
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Promote Shop", fontSize = 9.sp)
                                                }
                                            }
                                            if (u.role != "VENDOR" && u.role != "ADMIN") {
                                                OutlinedButton(
                                                    onClick = {
                                                        requestConfirmation("Promote to Agent", "Promote ${u.fullName} to an authorized field agent (vendor) account?") {
                                                            viewModel.adminPromoteUser(u.id, "VENDOR")
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Promote Agent", fontSize = 9.sp)
                                                }
                                            }

                                            val toggleStatusText = if (u.status == "ACTIVE") "Suspend" else "Activate"
                                            Button(
                                                onClick = {
                                                    val nextStatus = if (u.status == "ACTIVE") "SUSPENDED" else "ACTIVE"
                                                    requestConfirmation("$toggleStatusText User", "Confirm putting ${u.fullName}'s account status to $nextStatus?") {
                                                        viewModel.adminUpdateUserStatus(u.id, nextStatus)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (u.status == "ACTIVE") WarningOrange else SuccessGreen
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(toggleStatusText, fontSize = 9.sp, color = Color.White)
                                            }

                                            IconButton(
                                                onClick = {
                                                    requestConfirmation("Delete User Account", "This action is destructive and irreversible. Delete ${u.fullName} from database permanently?") {
                                                        viewModel.adminDeleteUser(u.id)
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Users Pagination
                        PaginationControls(
                            currentPage = finalPage,
                            totalPages = totalPages,
                            totalItems = filteredUsers.size,
                            onPageChanged = { pageUsers = it }
                        )
                    }
                }
            }

            "shops" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = shopSearchQuery,
                            onValueChange = { shopSearchQuery = it; pageShops = 0 },
                            placeholder = { Text("Search shops by name/owner/state", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(onClick = { exportCsv("SHOPS") }) {
                            Icon(Icons.Default.Download, contentDescription = "Export Shops", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Status Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val statuses = listOf("All", "Pending", "Approved", "Rejected", "Suspended")
                        items(statuses) { statusOpt ->
                            val isSelected = shopStatusFilter.lowercase() == statusOpt.lowercase()
                            AssistChip(
                                onClick = { shopStatusFilter = statusOpt; pageShops = 0 },
                                label = { Text(statusOpt, fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                )
                            )
                        }
                    }

                    val filteredShops = shops.filter { s ->
                        val matchesSearch = s.shopName.lowercase().contains(shopSearchQuery.lowercase()) ||
                                s.ownerName.lowercase().contains(shopSearchQuery.lowercase()) ||
                                s.state.lowercase().contains(shopSearchQuery.lowercase())
                        val matchesStatus = shopStatusFilter == "All" || s.status.uppercase() == shopStatusFilter.uppercase()
                        matchesSearch && matchesStatus
                    }

                    val totalPages = (filteredShops.size + pageSize - 1) / pageSize
                    val finalPage = kotlin.math.min(pageShops, (totalPages - 1).coerceAtLeast(0))
                    val pagedShops = filteredShops.drop(finalPage * pageSize).take(pageSize)

                    if (pagedShops.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No shops found.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pagedShops) { s ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(s.shopName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (s.isVerified) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(Icons.Default.Verified, contentDescription = "Verified Shop", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Surface(
                                                color = when (s.status.uppercase()) {
                                                    "APPROVED" -> SuccessGreen.copy(alpha = 0.12f)
                                                    "PENDING" -> WarningOrange.copy(alpha = 0.12f)
                                                    else -> ErrorRed.copy(alpha = 0.12f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = s.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (s.status.uppercase()) {
                                                        "APPROVED" -> SuccessGreen
                                                        "PENDING" -> WarningOrange
                                                        else -> ErrorRed
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text("Owner: ${s.ownerName} | Email: ${s.email}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Contact: ${s.phone} | WhatsApp: ${s.whatsApp}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Node Address: ${s.address}, ${s.city}, ${s.state}", fontSize = 11.sp, color = Color.Gray)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (s.status != "APPROVED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Approve Shop", "Are you sure you want to approve & authorize ${s.shopName}?") {
                                                            viewModel.adminApproveShop(s.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Approve", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            if (s.status == "PENDING") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Reject Shop", "Decline the store application of ${s.shopName}?") {
                                                            viewModel.adminRejectShop(s.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reject", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            if (s.status == "APPROVED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Suspend Shop", "Suspend all activity and marketplace privileges of ${s.shopName}?") {
                                                            viewModel.adminSuspendShop(s.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Suspend", fontSize = 9.sp, color = Color.White)
                                                }
                                            } else if (s.status == "SUSPENDED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Reactivate Shop", "Lift suspension and restore full privileges for ${s.shopName}?") {
                                                            viewModel.adminReactivateShop(s.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reactivate", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    // Toggle verified state
                                                    val nextVer = !s.isVerified
                                                    val actionTxt = if (nextVer) "Verify" else "Unverify"
                                                    requestConfirmation("$actionTxt Shop", "Toggle store badge verification state for ${s.shopName}?") {
                                                        viewModel.adminVerifyShop(s.id, nextVer)
                                                    }
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1.2f)
                                            ) {
                                                Text(if (s.isVerified) "Unverify" else "Verify", fontSize = 9.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    locationTargetId = s.id
                                                    locationTargetType = "SHOP"
                                                    editStateVal = s.state
                                                    editCityVal = s.city
                                                    showLocationDialog = true
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Edit Loc", fontSize = 9.sp)
                                            }

                                            IconButton(
                                                onClick = {
                                                    requestConfirmation("Delete Shop Record", "Remove ${s.shopName}'s platform merchant listing entirely?") {
                                                        viewModel.adminDeleteShop(s.id)
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        PaginationControls(
                            currentPage = finalPage,
                            totalPages = totalPages,
                            totalItems = filteredShops.size,
                            onPageChanged = { pageShops = it }
                        )
                    }
                }
            }

            "vendors" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = vendorSearchQuery,
                            onValueChange = { vendorSearchQuery = it; pageVendors = 0 },
                            placeholder = { Text("Search by business or owner", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(onClick = { exportCsv("VENDORS") }) {
                            Icon(Icons.Default.Download, contentDescription = "Export Vendors", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Status Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val statuses = listOf("All", "Pending", "Approved", "Rejected", "Suspended")
                        items(statuses) { statusOpt ->
                            val isSelected = vendorStatusFilter.lowercase() == statusOpt.lowercase()
                            AssistChip(
                                onClick = { vendorStatusFilter = statusOpt; pageVendors = 0 },
                                label = { Text(statusOpt, fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                )
                            )
                        }
                    }

                    val filteredVendors = vendors.filter { v ->
                        val matchesSearch = v.vendorName.lowercase().contains(vendorSearchQuery.lowercase()) ||
                                v.ownerName.lowercase().contains(vendorSearchQuery.lowercase())
                        val matchesStatus = vendorStatusFilter == "All" || v.status.uppercase() == vendorStatusFilter.uppercase()
                        matchesSearch && matchesStatus
                    }

                    val totalPages = (filteredVendors.size + pageSize - 1) / pageSize
                    val finalPage = kotlin.math.min(pageVendors, (totalPages - 1).coerceAtLeast(0))
                    val pagedVendors = filteredVendors.drop(finalPage * pageSize).take(pageSize)

                    if (pagedVendors.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No field agents found.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pagedVendors) { v ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(v.vendorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Surface(
                                                color = when (v.status.uppercase()) {
                                                    "APPROVED" -> SuccessGreen.copy(alpha = 0.12f)
                                                    "PENDING" -> WarningOrange.copy(alpha = 0.12f)
                                                    else -> ErrorRed.copy(alpha = 0.12f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = v.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (v.status.uppercase()) {
                                                        "APPROVED" -> SuccessGreen
                                                        "PENDING" -> WarningOrange
                                                        else -> ErrorRed
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text("Owner Agent: ${v.ownerName} | Email: ${v.email}", fontSize = 11.sp, color = Color.Gray)
                                        Text("WhatsApp: ${v.whatsApp} | States Server: ${v.state}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Commodities: ${v.categoriesSold}", fontSize = 11.sp, color = Color.Gray)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (v.status != "APPROVED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Approve Agent", "Confirm approving & licensing ${v.vendorName} on active directory?") {
                                                            viewModel.adminApproveVendor(v.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Approve", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            if (v.status == "PENDING") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Reject Agent", "Reject applications for ${v.vendorName}?") {
                                                            viewModel.adminRejectVendor(v.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reject", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            if (v.status == "APPROVED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Suspend Agent", "Temporarily suspend operations of ${v.vendorName}?") {
                                                            viewModel.adminSuspendVendor(v.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Suspend", fontSize = 9.sp, color = Color.White)
                                                }
                                            } else if (v.status == "SUSPENDED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Reactivate Agent", "Lift suspension for agent ${v.vendorName}?") {
                                                            viewModel.adminReactivateVendor(v.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reactivate", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    locationTargetId = v.id
                                                    locationTargetType = "VENDOR"
                                                    editStateVal = v.state
                                                    editCityVal = v.city
                                                    showLocationDialog = true
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Edit State", fontSize = 9.sp)
                                            }

                                            IconButton(
                                                onClick = {
                                                    requestConfirmation("Delete Agent Record", "Delete vendor registration file for ${v.vendorName} permanently?") {
                                                        viewModel.adminDeleteVendor(v.id)
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        PaginationControls(
                            currentPage = finalPage,
                            totalPages = totalPages,
                            totalItems = filteredVendors.size,
                            onPageChanged = { pageVendors = it }
                        )
                    }
                }
            }

            "listings" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = listingSearchQuery,
                            onValueChange = { listingSearchQuery = it; pageListings = 0 },
                            placeholder = { Text("Search listings by device name/brand", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(onClick = { exportCsv("LISTINGS") }) {
                            Icon(Icons.Default.Download, contentDescription = "Export Listings", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Status Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val statuses = listOf("All", "Pending", "Approved", "Rejected", "Suspended")
                        items(statuses) { statusOpt ->
                            val isSelected = listingStatusFilter.lowercase() == statusOpt.lowercase()
                            AssistChip(
                                onClick = { listingStatusFilter = statusOpt; pageListings = 0 },
                                label = { Text(statusOpt, fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                )
                            )
                        }
                    }

                    val filteredListings = listings.filter { l ->
                        val matchesSearch = l.gadgetName.lowercase().contains(listingSearchQuery.lowercase()) ||
                                l.brand.lowercase().contains(listingSearchQuery.lowercase()) ||
                                l.model.lowercase().contains(listingSearchQuery.lowercase())
                        val matchesStatus = listingStatusFilter == "All" || l.status.uppercase() == listingStatusFilter.uppercase()
                        matchesSearch && matchesStatus
                    }

                    val totalPages = (filteredListings.size + pageSize - 1) / pageSize
                    val finalPage = kotlin.math.min(pageListings, (totalPages - 1).coerceAtLeast(0))
                    val pagedListings = filteredListings.drop(finalPage * pageSize).take(pageSize)

                    if (pagedListings.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No marketplace items found.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pagedListings) { l ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(l.gadgetName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (l.isFeatured) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(Icons.Default.Star, contentDescription = "Featured", tint = WarningOrange, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (l.isSold) {
                                                    Surface(
                                                        color = Color.LightGray.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.padding(end = 4.dp)
                                                    ) {
                                                        Text("SOLD", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                    }
                                                }
                                                Surface(
                                                    color = when (l.status.uppercase()) {
                                                        "APPROVED" -> SuccessGreen.copy(alpha = 0.12f)
                                                        "PENDING" -> WarningOrange.copy(alpha = 0.12f)
                                                        else -> ErrorRed.copy(alpha = 0.12f)
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = l.status,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (l.status.uppercase()) {
                                                            "APPROVED" -> SuccessGreen
                                                            "PENDING" -> WarningOrange
                                                            else -> ErrorRed
                                                        },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text("Brand: ${l.brand} | Model: ${l.model} | Category: ${l.category}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Price Tag: ${formatNaira(l.price)} | Poster: ${l.posterName}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Geographics: ${l.city}, ${l.state} | Condition: ${l.condition}", fontSize = 11.sp, color = Color.Gray)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (l.status != "APPROVED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Approve Listing", "Approve & enable ${l.gadgetName} public visibility?") {
                                                            viewModel.adminApproveListing(l.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Approve", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            if (l.status == "PENDING") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Reject Listing", "Decline & archive listing of ${l.gadgetName}?") {
                                                            viewModel.adminRejectListing(l.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reject", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            if (l.status == "APPROVED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Suspend Listing", "Suspend marketplace display of ${l.gadgetName}?") {
                                                            viewModel.adminSuspendListing(l.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Suspend", fontSize = 9.sp, color = Color.White)
                                                }
                                            } else if (l.status == "SUSPENDED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Reactivate Listing", "Reactivate visibility for ${l.gadgetName}?") {
                                                            viewModel.adminReactivateListing(l.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reactivate", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    val nextSold = !l.isSold
                                                    viewModel.adminMarkListingSold(l.id, nextSold)
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(if (l.isSold) "Make Avail" else "Mark Sold", fontSize = 9.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    val nextFeat = !l.isFeatured
                                                    viewModel.adminFeatureListing(l.id, nextFeat)
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1.1f)
                                            ) {
                                                Text(if (l.isFeatured) "Unfeature" else "Feature", fontSize = 9.sp)
                                            }

                                            IconButton(
                                                onClick = {
                                                    requestConfirmation("De-list Item", "Delete item listing of ${l.gadgetName}?") {
                                                        viewModel.adminDeleteListing(l.id)
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        PaginationControls(
                            currentPage = finalPage,
                            totalPages = totalPages,
                            totalItems = filteredListings.size,
                            onPageChanged = { pageListings = it }
                        )
                    }
                }
            }

            "reports" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = reportSearchQuery,
                            onValueChange = { reportSearchQuery = it; pageReports = 0 },
                            placeholder = { Text("Search by reporter / reasons / target", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(onClick = { exportCsv("REPORTS") }) {
                            Icon(Icons.Default.Download, contentDescription = "Export Abuse Reports", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Status Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val statuses = listOf("All", "Pending", "Reviewed", "Resolved", "Dismissed")
                        items(statuses) { statusOpt ->
                            val isSelected = reportStatusFilter.lowercase() == statusOpt.lowercase()
                            AssistChip(
                                onClick = { reportStatusFilter = statusOpt; pageReports = 0 },
                                label = { Text(statusOpt, fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                )
                            )
                        }
                    }

                    val filteredReports = reports.filter { r ->
                        val matchesSearch = r.reporterName.lowercase().contains(reportSearchQuery.lowercase()) ||
                                r.reason.lowercase().contains(reportSearchQuery.lowercase()) ||
                                r.targetName.lowercase().contains(reportSearchQuery.lowercase())
                        val matchesStatus = reportStatusFilter == "All" || r.status.uppercase() == reportStatusFilter.uppercase()
                        matchesSearch && matchesStatus
                    }

                    val totalPages = (filteredReports.size + pageSize - 1) / pageSize
                    val finalPage = kotlin.math.min(pageReports, (totalPages - 1).coerceAtLeast(0))
                    val pagedReports = filteredReports.drop(finalPage * pageSize).take(pageSize)

                    if (pagedReports.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No abuse/complaints reports logged.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pagedReports) { r ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Report ID: #${r.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Surface(
                                                color = when (r.status.uppercase()) {
                                                    "PENDING" -> WarningOrange.copy(alpha = 0.12f)
                                                    "RESOLVED" -> SuccessGreen.copy(alpha = 0.12f)
                                                    else -> Color.LightGray.copy(alpha = 0.35f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = r.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (r.status.uppercase()) {
                                                        "PENDING" -> WarningOrange
                                                        "RESOLVED" -> SuccessGreen
                                                        else -> Color.DarkGray
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text("Reporter: ${r.reporterName} (${r.reporterEmail})", fontSize = 11.sp, color = Color.Gray)
                                        Text("Target Subject: ${r.targetType} [ID: ${r.targetId}] - ${r.targetName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        
                                        Surface(
                                            color = Color.LightGray.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("Violation Type: ${r.reason}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Observation: ${r.details}", fontSize = 11.sp, color = Color.DarkGray)
                                            }
                                        }

                                        Text("Received on: ${formatTimestampLocal(r.timestamp)}", fontSize = 10.sp, color = Color.Gray)
                                        
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (r.status != "PENDING") {
                                                OutlinedButton(
                                                    onClick = { viewModel.adminUpdateReportState(r.id, "PENDING") },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Set Pending", fontSize = 9.sp)
                                                }
                                            }

                                            if (r.status != "REVIEWED") {
                                                OutlinedButton(
                                                    onClick = { viewModel.adminUpdateReportState(r.id, "REVIEWED") },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Set Reviewed", fontSize = 9.sp)
                                                }
                                            }

                                            if (r.status != "RESOLVED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Resolve Report", "Mark report #${r.id} as resolved and fully closed?") {
                                                            viewModel.adminResolveReport(r.id)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Resolve", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            if (r.status != "DISMISSED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Dismiss Report", "Dismiss reported complaint #${r.id} as false or invalid?") {
                                                            viewModel.adminUpdateReportState(r.id, "DISMISSED")
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Dismiss", fontSize = 9.sp, color = Color.Black)
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    requestConfirmation("Force Delete Report", "Purge report audit incident record #${r.id} permanently?") {
                                                        viewModel.adminDeleteReport(r.id)
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        PaginationControls(
                            currentPage = finalPage,
                            totalPages = totalPages,
                            totalItems = filteredReports.size,
                            onPageChanged = { pageReports = it }
                        )
                    }
                }
            }

            "support" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = supportTabSearchQuery,
                        onValueChange = { supportTabSearchQuery = it; pageSupportMessages = 0 },
                        placeholder = { Text("Search messages by sender/subject/message", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Status Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val statuses = listOf("All", "Open", "In progress", "Resolved")
                        items(statuses) { statusOpt ->
                            val isSelected = supportTabStatusFilter.lowercase() == statusOpt.lowercase()
                            AssistChip(
                                onClick = { supportTabStatusFilter = statusOpt; pageSupportMessages = 0 },
                                label = { Text(statusOpt, fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                )
                            )
                        }
                    }

                    val filteredSupport = supportMessages.filter { s ->
                        val matchesSearch = s.subject.lowercase().contains(supportTabSearchQuery.lowercase()) ||
                                s.senderName.lowercase().contains(supportTabSearchQuery.lowercase()) ||
                                s.senderEmail.lowercase().contains(supportTabSearchQuery.lowercase()) ||
                                s.message.lowercase().contains(supportTabSearchQuery.lowercase())
                        val matchesStatus = supportTabStatusFilter == "All" || s.status.uppercase() == supportTabStatusFilter.uppercase()
                        matchesSearch && matchesStatus
                    }

                    val totalPages = (filteredSupport.size + pageSize - 1) / pageSize
                    val finalPage = kotlin.math.min(pageSupportMessages, (totalPages - 1).coerceAtLeast(0))
                    val pagedSupport = filteredSupport.drop(finalPage * pageSize).take(pageSize)

                    if (pagedSupport.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No support tickets generated.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pagedSupport) { s ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(s.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                            Surface(
                                                color = when (s.status.uppercase()) {
                                                    "OPEN" -> ErrorRed.copy(alpha = 0.12f)
                                                    "IN_PROGRESS" -> WarningOrange.copy(alpha = 0.12f)
                                                    else -> SuccessGreen.copy(alpha = 0.12f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = s.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (s.status.uppercase()) {
                                                        "OPEN" -> ErrorRed
                                                        "IN_PROGRESS" -> WarningOrange
                                                        else -> SuccessGreen
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text("Sender: ${s.senderName} (${s.senderEmail})", fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(s.message, fontSize = 11.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Created on: ${formatTimestampLocal(s.timestamp)}", fontSize = 10.sp, color = Color.Gray)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (s.status != "OPEN") {
                                                OutlinedButton(
                                                    onClick = { viewModel.adminUpdateSupportStatus(s.id, "OPEN") },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Set Open", fontSize = 9.sp)
                                                }
                                            }
                                            if (s.status != "IN_PROGRESS") {
                                                OutlinedButton(
                                                    onClick = { viewModel.adminUpdateSupportStatus(s.id, "IN_PROGRESS") },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("In Progress", fontSize = 9.sp)
                                                }
                                            }
                                            if (s.status != "RESOLVED") {
                                                Button(
                                                    onClick = {
                                                        requestConfirmation("Resolve Ticket", "Are you sure you want to mark this support request as resolved?") {
                                                            viewModel.adminUpdateSupportStatus(s.id, "RESOLVED")
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Resolve", fontSize = 9.sp, color = Color.White)
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    requestConfirmation("Delete Support Message", "Delete support log #${s.id} permanently?") {
                                                        viewModel.adminDeleteSupport(s.id)
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        PaginationControls(
                            currentPage = finalPage,
                            totalPages = totalPages,
                            totalItems = filteredSupport.size,
                            onPageChanged = { pageSupportMessages = it }
                        )
                    }
                }
            }

            "settings" -> {
                // Settings Form (Fully loaded from Entity and synchronizes natively)
                var priceDisclaimerState by remember { mutableStateOf("") }
                var supportedStatesState by remember { mutableStateOf("") }
                var supportedCategoriesState by remember { mutableStateOf("") }
                var marketplaceRulesState by remember { mutableStateOf("") }
                var shopApprovalMessageState by remember { mutableStateOf("") }
                var vendorApprovalMessageState by remember { mutableStateOf("") }
                var contactEmailState by remember { mutableStateOf("") }
                var whatsAppNumberState by remember { mutableStateOf("") }

                LaunchedEffect(appSettings) {
                    appSettings?.let { s ->
                        priceDisclaimerState = s.priceDisclaimer
                        supportedStatesState = s.supportedStates
                        supportedCategoriesState = s.supportedCategories
                        marketplaceRulesState = s.marketplaceRules
                        shopApprovalMessageState = s.shopApprovalMessage
                        vendorApprovalMessageState = s.vendorApprovalMessage
                        contactEmailState = s.contactEmail
                        whatsAppNumberState = s.whatsAppNumber
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Global System Configurations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = priceDisclaimerState,
                        onValueChange = { priceDisclaimerState = it },
                        label = { Text("Default Price Disclaimer Text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = supportedStatesState,
                        onValueChange = { supportedStatesState = it },
                        label = { Text("Supported Geographics States (Semicolon separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = supportedCategoriesState,
                        onValueChange = { supportedCategoriesState = it },
                        label = { Text("Supported Device Categories (Semicolon separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = marketplaceRulesState,
                        onValueChange = { marketplaceRulesState = it },
                        label = { Text("Marketplace Terms & Rules Guidance") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = shopApprovalMessageState,
                        onValueChange = { shopApprovalMessageState = it },
                        label = { Text("Official Shop Approval Welcome Message") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = vendorApprovalMessageState,
                        onValueChange = { vendorApprovalMessageState = it },
                        label = { Text("Field Agent / Vendor Approval Message") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = contactEmailState,
                            onValueChange = { contactEmailState = it },
                            label = { Text("Contact Email Support") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = whatsAppNumberState,
                            onValueChange = { whatsAppNumberState = it },
                            label = { Text("WhatsApp Hotline") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.adminSaveSettings(
                                priceDisclaimerState,
                                supportedStatesState,
                                supportedCategoriesState,
                                marketplaceRulesState,
                                shopApprovalMessageState,
                                vendorApprovalMessageState,
                                contactEmailState,
                                whatsAppNumberState
                            )
                            Toast.makeText(context, "System configuration saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save All Settings Configuration", fontWeight = FontWeight.Bold)
                    }
                }
            }

            "audit" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = auditSearchQuery,
                        onValueChange = { auditSearchQuery = it; pageAuditLogs = 0 },
                        placeholder = { Text("Search logs by email, type or target", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    val filteredAudit = adminActionLogs.filter { log ->
                        log.adminEmail.lowercase().contains(auditSearchQuery.lowercase()) ||
                                log.actionType.lowercase().contains(auditSearchQuery.lowercase()) ||
                                log.targetType.lowercase().contains(auditSearchQuery.lowercase())
                    }.sortedByDescending { it.timestamp }

                    val totalPages = (filteredAudit.size + pageSize - 1) / pageSize
                    val finalPage = kotlin.math.min(pageAuditLogs, (totalPages - 1).coerceAtLeast(0))
                    val pagedAudit = filteredAudit.drop(finalPage * pageSize).take(pageSize)

                    if (pagedAudit.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No audit incidents listed.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(pagedAudit) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = when (log.actionType) {
                                                    "APPROVE" -> SuccessGreen.copy(alpha = 0.15f)
                                                    "REJECT", "SUSPEND", "DELETE" -> ErrorRed.copy(alpha = 0.15f)
                                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = log.actionType,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (log.actionType) {
                                                        "APPROVE" -> SuccessGreen
                                                        "REJECT", "SUSPEND", "DELETE" -> ErrorRed
                                                        else -> MaterialTheme.colorScheme.primary
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = formatTimestampLocal(log.timestamp),
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("User / Admin: ${log.adminEmail}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("Modified Component: ${log.targetType} [ID: ${log.targetId}]", fontSize = 11.sp, color = Color.Gray)
                                        
                                        if (log.oldValue.isNotBlank() || log.newValue.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text("Change description: ${log.oldValue} -> ${log.newValue}", fontSize = 10.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }

                        PaginationControls(
                            currentPage = finalPage,
                            totalPages = totalPages,
                            totalItems = filteredAudit.size,
                            onPageChanged = { pageAuditLogs = it }
                        )
                    }
                }
            }
        }
    }

    // CONFIRMATION DIALOG OVERLAY
    AdminConfirmDialog(
        isOpen = showConfirmDialog,
        title = confirmTitle,
        message = confirmMessage,
        onConfirm = onConfirmAction,
        onDismiss = { showConfirmDialog = false }
    )

    // LOCATION EDIT DIALOG OVERLAY
    if (showLocationDialog) {
        Dialog(onDismissRequest = { showLocationDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Change Location Nodes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    var selectStateDropdownExp by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editStateVal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State Geographic Node") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { selectStateDropdownExp = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = selectStateDropdownExp,
                            onDismissRequest = { selectStateDropdownExp = false }
                        ) {
                            NIGERIAN_STATES.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        editStateVal = state
                                        selectStateDropdownExp = false
                                    }
                                )
                            }
                        }
                    }

                    if (locationTargetType == "SHOP") {
                        OutlinedTextField(
                            value = editCityVal,
                            onValueChange = { editCityVal = it },
                            label = { Text("City Node") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showLocationDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val tid = locationTargetId
                                if (tid != null) {
                                    if (locationTargetType == "SHOP") {
                                        viewModel.adminEditShopLocation(tid, editStateVal, editCityVal)
                                    } else {
                                        viewModel.adminEditVendorState(tid, editStateVal)
                                    }
                                    Toast.makeText(context, "Location updated successfully!", Toast.LENGTH_SHORT).show()
                                }
                                showLocationDialog = false
                            }
                        ) {
                            Text("Save Location")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridLayout(
    columns: Int,
    items: List<Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>>,
    content: @Composable (Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val rows = (items.size + columns - 1) / columns
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (c in 0 until columns) {
                    val index = r * columns + c
                    if (index < items.size) {
                        Box(modifier = Modifier.weight(1f)) {
                            content(items[index])
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    onPageChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Page ${currentPage + 1} of ${totalPages.coerceAtLeast(1)} ($totalItems items)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = { if (currentPage > 0) onPageChanged(currentPage - 1) },
                enabled = currentPage > 0
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Previous", fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = { if (currentPage < totalPages - 1) onPageChanged(currentPage + 1) },
                enabled = currentPage < totalPages - 1
            ) {
                Text("Next", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun AdminConfirmDialog(
    isOpen: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text(message, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Confirm action", color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
fun ProfileTab(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val savedScans by viewModel.userScans.collectAsState()

    var showTestingVIPSuite by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "User Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            text = "Your identity session and configurations",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = currentUser?.fullName ?: "Non-identified User",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        ) {
            Text(
                text = "ROLE: ${currentUser?.role ?: "USER"}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val items = listOf(
                    "Email Contact" to (currentUser?.email ?: ""),
                    "Mobile Contact" to (currentUser?.phoneNumber ?: ""),
                    "Operating State" to (currentUser?.state ?: ""),
                    "Operating City" to (currentUser?.city ?: ""),
                    "Scans Registered" to savedScans.size.toString()
                )

                items.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.15f))
                }
            }
        }

        // Help & Contact Support Message Card
        var supportSubject by remember { mutableStateOf("") }
        var supportMsg by remember { mutableStateOf("") }
        var msgSubmitted by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Help & Contact Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Send a message directly to admins regarding price valuations, verification status, or account concerns.", fontSize = 11.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(14.dp))
                
                if (msgSubmitted) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Muted Success!",
                                color = SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Your inquiry was logged. Operators will review shortly.",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = { msgSubmitted = false }) {
                        Text("Send Another Message", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    OutlinedTextField(
                        value = supportSubject,
                        onValueChange = { supportSubject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = supportMsg,
                        onValueChange = { supportMsg = it },
                        label = { Text("Details of your issue") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (supportSubject.isNotBlank() && supportMsg.isNotBlank()) {
                                viewModel.submitSupport(
                                    currentUser?.fullName ?: "Chidi Okafor",
                                    currentUser?.email ?: "chidi@gmail.com",
                                    supportSubject,
                                    supportMsg
                                )
                                supportSubject = ""
                                supportMsg = ""
                                msgSubmitted = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Submit Request")
                    }
                }
            }
        }

        Button(
            onClick = { showTestingVIPSuite = !showTestingVIPSuite },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.SettingsInputComponent, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Launch VIP Developer Role-Switcher")
        }

        AnimatedVisibility(visible = showTestingVIPSuite) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = LightLavender),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Developer Session Simulator (Role-Switcher)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Instantly swap session roles to test permissions & dashboard tabs with a single tap:", fontSize = 10.sp, color = Color.DarkGray, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ADMIN", "SHOP", "VENDOR", "USER").forEach { role ->
                            OutlinedButton(
                                onClick = { viewModel.switchRoleForTesting(role) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (currentUser?.role == role) MaterialTheme.colorScheme.primary else Color.White,
                                    contentColor = if (currentUser?.role == role) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(role, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Secure Log Out", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
