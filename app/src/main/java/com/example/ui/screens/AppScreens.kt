package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.GadgetAnalysisResult
import com.example.data.SavedComparison
import com.example.data.ValuationHistory
import com.example.ui.MainViewModel
import com.example.ui.UiState
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavigation(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            if (currentScreen != "scanner_photo" && currentScreen != "valuation") {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                "dashboard" -> DashboardScreen(viewModel)
                "scanner_photo" -> SimulatedCameraView(viewModel)
                "scanner_confirm" -> PhotoConfirmScreen(viewModel)
                "valuation" -> ValuationScreen(viewModel)
                "compare" -> CompareScreen(viewModel)
                "vendors" -> VendorsScreen(viewModel)
                "tips" -> TipsScreen(viewModel)
                "history" -> HistoryScreen(viewModel)
            }
        }
    }
}

// --- Bottom Navigation ---
@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            selected = currentScreen == "dashboard" || currentScreen == "scanner_confirm",
            onClick = { onNavigate("dashboard") },
            modifier = Modifier.testTag("nav_home")
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.CompareArrows, contentDescription = "Compare") },
            label = { Text("Compare", fontSize = 11.sp) },
            selected = currentScreen == "compare",
            onClick = { onNavigate("compare") },
            modifier = Modifier.testTag("nav_compare")
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Storefront, contentDescription = "Vendors") },
            label = { Text("Shops", fontSize = 11.sp) },
            selected = currentScreen == "vendors",
            onClick = { onNavigate("vendors") },
            modifier = Modifier.testTag("nav_vendors")
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History", fontSize = 11.sp) },
            selected = currentScreen == "history",
            onClick = { onNavigate("history") },
            modifier = Modifier.testTag("nav_history")
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Security") },
            label = { Text("Safety", fontSize = 11.sp) },
            selected = currentScreen == "tips",
            onClick = { onNavigate("tips") },
            modifier = Modifier.testTag("nav_tips")
        )
    }
}

// --- Dashboard Screen ---
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val history by viewModel.valuationsHistory.collectAsStateWithLifecycle()
    var textInput by remember { mutableStateOf("") }

    val totalValue = history.sumOf { (it.calculatedValueMin + it.calculatedValueMax) / 2 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Card
        item {
            HeaderSection(totalItems = history.size, averageEstimatedWorth = totalValue)
        }

        // Quick Search Action Block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Real-time Valuation Desk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter gadget model parameters or take a diagnostics photo",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("e.g. iPhone 13 Pro Max", fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    viewModel.submitValuation(textInput, null)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_value_text"),
                            enabled = textInput.isNotBlank(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fast Price")
                        }

                        FilledTonalButton(
                            onClick = { viewModel.navigateTo("scanner_photo") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_value_camera"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Photo Scan")
                        }
                    }
                }
            }
        }

        // Quick Category Showcase
        item {
            Text(
                text = "Nigerian Market Hotspots",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                QuickBrandChip("Apple iPhone", "Phone") { textInput = "Apple iPhone "; viewModel.submitValuation("Apple iPhone 14 Pro", null) }
                QuickBrandChip("Samsung Galaxy", "Phone") { textInput = "Samsung Galaxy "; viewModel.submitValuation("Samsung Galaxy S23 Ultra", null) }
                QuickBrandChip("MacBook Pro/Air", "Laptop") { textInput = "MacBook Pro "; viewModel.submitValuation("Apple MacBook Air M2", null) }
                QuickBrandChip("HP EliteBook", "Laptop") { textInput = "HP EliteBook "; viewModel.submitValuation("HP EliteBook 840 G8", null) }
                QuickBrandChip("Dell Latitude", "Laptop") { textInput = "Dell Latitude "; viewModel.submitValuation("Dell Latitude 7420", null) }
            }
        }

        // Computer Village Essential Safe Trade Alert Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo("tips") }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Computer Village Trading Guide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Avoid getting scammed at slots. Explore full escrow procedures & diagnostic lists.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Quick History Preview Card
        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Appraisals",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    TextButton(onClick = { viewModel.navigateTo("history") }) {
                        Text("View all")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    history.take(3).forEach { hist ->
                        HistoryRowItem(hist) { viewModel.navigateTo("history") }
                    }
                }
            }
        }
    }
}

// --- Composable Subcomponents ---

@Composable
fun HeaderSection(totalItems: Int, averageEstimatedWorth: Long) {
    val nairaFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    nairaFormat.maximumFractionDigits = 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Gadget Valuer NG",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Nigeria's Diagnostic & Price Index",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                // Country Code badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("🇳🇬 NGN", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("YOUR INVENTORY ESTIMATE", fontSize = 10.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    Text(
                        text = nairaFormat.format(averageEstimatedWorth),
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("APPRAISED", fontSize = 10.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    Text(
                        text = "$totalItems devices",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun QuickBrandChip(name: String, cat: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (cat == "Phone") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun HistoryRowItem(history: ValuationHistory, onRowClick: () -> Unit = {}) {
    val nairaFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    nairaFormat.maximumFractionDigits = 0

    Card(
        onClick = onRowClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (history.category) {
                        "Phone" -> Icons.Default.PhoneAndroid
                        "Laptop" -> Icons.Default.Laptop
                        "Tablet" -> Icons.Default.TabletAndroid
                        "Smartwatch" -> Icons.Default.Watch
                        else -> Icons.Default.DevicesOther
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(history.gadgetName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(history.specs, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = nairaFormat.format((history.calculatedValueMin + history.calculatedValueMax) / 2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Valued Average", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

// --- Simulated Camera View for Diagnostics ---
@Composable
fun SimulatedCameraView(viewModel: MainViewModel) {
    var brandName by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("Phone") }
    var mockImageGenerated by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("AI DIAGNOSTICK WORKSTATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            IconButton(onClick = {}) {
                Icon(Icons.Default.FlashOn, contentDescription = "Flash", tint = Color.White)
            }
        }

        // Camera viewfinder overlay / instructions
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .background(Color(0xFF1E293B))
                .border(2.dp, Color.Green.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.Green, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Simulate photo appraisal of your device.",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    "This triggers Gemini multi-modal detection of your model, body condition, and visual damages.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // User specifies model hints
                Text("Specify model name so the scanner knows what to identify:", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    placeholder = { Text("e.g., iPhone 13 Pro (128GB)", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diag_name_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedCat == "Phone", onClick = { selectedCat = "Phone" })
                        Text("Phone", color = Color.White, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedCat == "Laptop", onClick = { selectedCat = "Laptop" })
                        Text("Laptop", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }

        // Shutter Button Panel
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Simulate Snapshot Capture", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, CircleShape)
                    .border(5.dp, Color.Gray, CircleShape)
                    .clickable {
                        // Generate a simple mock bitmap for the preview
                        val width = 300
                        val height = 300
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        val paint = android.graphics.Paint()
                        paint.color = android.graphics.Color.BLUE
                        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                        paint.color = android.graphics.Color.WHITE
                        paint.textSize = 24f
                        canvas.drawText("DEVICE MODEL: ${brandName.ifEmpty { "Generic" }}", 20f, 150f, paint)

                        viewModel.analyzerQueryText.value = brandName.ifBlank { "iPhone 13 Pro" }
                        viewModel.setCapturedBitmap(bitmap)
                    }
                    .testTag("camera_shutter"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

// --- Preview / Confirm Captured Photo screen ---
@Composable
fun PhotoConfirmScreen(viewModel: MainViewModel) {
    val bitmap = viewModel.capturedImage.value
    val query = viewModel.analyzerQueryText.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Verify Diagnostic Photo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Make sure the screen and boundaries are clear for the Gemini model",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Device Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Selected device target: $query", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.navigateTo("scanner_photo") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_retake"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Retake")
            }

            Button(
                onClick = { viewModel.submitValuation(query, bitmap) },
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("btn_submit_appraisal"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Appraise Target")
            }
        }
    }
}

// --- Dynamic Valuation Results Screen ---
@Composable
fun ValuationScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val condition by viewModel.selectedCondition.collectAsStateWithLifecycle()
    
    // Custom appraisal adjustment variables
    val chargerPresent by viewModel.hasOriginalCharger.collectAsStateWithLifecycle()
    val receiptPresent by viewModel.hasReceipt.collectAsStateWithLifecycle()
    val screenIntactState by viewModel.screenIntact.collectAsStateWithLifecycle()
    val batteryNormalState by viewModel.batteryNormal.collectAsStateWithLifecycle()

    when (uiState) {
        is UiState.Idle -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Initiate valuation via Home or Photo appraise.")
            }
        }
        is UiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Scrubbing Price Indexes...",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Running Gemini AI diagnostics & checking Computer Village historical trade catalogs...",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Appraisal Interrupted", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text((uiState as UiState.Error).message, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.navigateTo("dashboard") }) {
                        Text("Return Home")
                    }
                }
            }
        }
        is UiState.Success -> {
            val res = (uiState as UiState.Success).result

            // Reactive adjustments based on fine-tuning variables!
            var adjustmentDeduction = 0L
            if (!chargerPresent) adjustmentDeduction += 15000L
            if (!receiptPresent) adjustmentDeduction += 20000L
            if (!screenIntactState) adjustmentDeduction += 35000L
            if (!batteryNormalState) adjustmentDeduction += 18000L

            val (baseMin, baseMax) = when (condition) {
                "Grade A" -> Pair(res.valueMinGradeA, res.valueMaxGradeA)
                "Grade B" -> Pair(res.valueMinGradeB, res.valueMaxGradeB)
                else -> Pair(res.valueMinGradeC, res.valueMaxGradeC)
            }

            val finalMin = (baseMin - adjustmentDeduction).coerceAtLeast(30000L)
            val finalMax = (baseMax - adjustmentDeduction).coerceAtLeast(45000L)

            val nairaFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
            nairaFormat.maximumFractionDigits = 0

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top control bar
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.navigateTo("dashboard") },
                            modifier = Modifier.testTag("btn_back_home")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fast Exit")
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(res.category, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                        }
                    }
                }

                // Title details card
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = res.brand,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = res.model,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = res.estimatedSpecs,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Dynamic Appraised valuation range dashboard
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("ESTIMATED MARKET EXCHAGE RANGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = nairaFormat.format(finalMin),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(" - ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = nairaFormat.format(finalMax),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Green, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Liquidity rating: VERY HIGH RESELLABLE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Custom Appraisal Station toggles (Fine Tuning!)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Fine-Tune Physical Condition Index",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Condition Radio selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("Grade A", "Grade B", "Grade C").forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { viewModel.selectedCondition.value = item }
                                    ) {
                                        RadioButton(
                                            selected = condition == item,
                                            onClick = { viewModel.selectedCondition.value = item }
                                        )
                                        Text(item, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)

                            // Toggles
                            GridAdjustmentToggle("Has Original Follow-Come Charger", chargerPresent) { viewModel.hasOriginalCharger.value = it }
                            Spacer(modifier = Modifier.height(8.dp))
                            GridAdjustmentToggle("Has Device Verified Receipt", receiptPresent) { viewModel.hasReceipt.value = it }
                            Spacer(modifier = Modifier.height(8.dp))
                            GridAdjustmentToggle("Screen is free from scratches", screenIntactState) { viewModel.screenIntact.value = it }
                            Spacer(modifier = Modifier.height(8.dp))
                            GridAdjustmentToggle("Battery has normal cycles", batteryNormalState) { viewModel.batteryNormal.value = it }
                        }
                    }
                }

                // AI Expert Advice specifically for Computer Village swaps
                item {
                    Text("Computer Village Appraisal Advice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Recommend, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Local Resale Analysis", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = res.localMarketAnalysis,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Critical Hardware diagnostics verification sheets
                item {
                    Text("Essential Physical Self Check Guidelines", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DiagnosticChecklistCard("OLED/LCD Display Verification", res.screenVerificationTips, Icons.Default.Screenshot)
                        DiagnosticChecklistCard("Power & Ampere Cycles Test", res.batteryInspectionTips, Icons.Default.BatteryChargingFull)
                        DiagnosticChecklistCard("Lock Status & Anti-theft Check", res.lockVerificationTips, Icons.Default.LockReset)
                        DiagnosticChecklistCard("Vulnerability Repairement Hazard", res.standardRepairsWarning, Icons.Default.ReportProblem, true)
                    }
                }

                // Fast Direct verified shops redirection
                item {
                    Button(
                        onClick = { viewModel.navigateTo("vendors") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .testTag("btn_redirect_shops"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Locate Verified Nigeria Shops")
                    }
                }
            }
        }
    }
}

@Composable
fun GridAdjustmentToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun DiagnosticChecklistCard(
    title: String,
    tipsText: String,
    icon: ImageVector,
    isWarning: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(tipsText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }
}

// --- Side-by-Side compare Station ---
@Composable
fun CompareScreen(viewModel: MainViewModel) {
    var deviceA by remember { mutableStateOf("") }
    var deviceB by remember { mutableStateOf("") }
    val isLoading by viewModel.comparisonLoading.collectAsStateWithLifecycle()
    val activeComp by viewModel.activeComparison.collectAsStateWithLifecycle()
    val comparisonsHistory by viewModel.comparisonsHistory.collectAsStateWithLifecycle()

    val nairaFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    nairaFormat.maximumFractionDigits = 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Price Comparison Station", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black)
            Text("Appraise and structure side-by-side values instantly.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }

        // Compare input form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Enter specifications to compare", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = deviceA,
                        onValueChange = { deviceA = it },
                        placeholder = { Text("Base Device (e.g., iPhone 12 Pro)", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("compare_device_a"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = deviceB,
                        onValueChange = { deviceB = it },
                        placeholder = { Text("Alternative Device (e.g., Galaxy S21 Ultra)", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("compare_device_b"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.runDeviceComparison(deviceA, deviceB) },
                        enabled = deviceA.isNotBlank() && deviceB.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_run_compare")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.CompareArrows, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Appraise Comparison")
                        }
                    }
                }
            }
        }

        // Active Comparison Cards
        if (activeComp != null) {
            item {
                Text("Appraisal Comparison Case", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(activeComp!!.gadgetAName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(nairaFormat.format(activeComp!!.gadgetAValueMin), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text("Est. Minimum", fontSize = 10.sp)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(activeComp!!.gadgetBName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(nairaFormat.format(activeComp!!.gadgetBValueMin), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text("Est. Minimum", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI Comparative Breakdown", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(activeComp!!.specComparison, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Historic Comparisons list
        if (comparisonsHistory.isNotEmpty()) {
            item {
                Text("Comparison Audit Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    comparisonsHistory.forEach { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${log.gadgetAName} vs ${log.gadgetBName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    IconButton(onClick = { viewModel.deleteComparison(log.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Text(log.specComparison, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Verified Hub Directory Screen (Vendors) ---
data class NigeriaVendor(
    val name: String,
    val center: String,
    val details: String,
    val ratings: Double,
    val specialization: String,
    val telephone: String
)

@Composable
fun VendorsScreen(viewModel: MainViewModel) {
    val vendorsList = remember {
        listOf(
            NigeriaVendor("Slot Systems Hub", "Ikeja Computer Village, Lagos", "Plot 14, Medical Road. Official retail brand in secondary swapping pipelines.", 4.7, "Apple & Samsung Diagnostics", "+2348030000000"),
            NigeriaVendor("MicroStation Village", "Ikeja Computer Village, Lagos", "Otigba Street. Specializes in direct Grade A refurb imports.", 4.5, "HP/Dell Laptops, MacBooks", "+2348020000001"),
            NigeriaVendor("Abuja Gadget Palace", "Wuse Zone 3 plaza, Abuja", "Suite C4. Premium trusted node in FCT region for secure swap deals.", 4.8, "Apple iPhones & Tablets", "+2348090000002"),
            NigeriaVendor("Garden City Tech Haven", "Plaza Port Harcourt", "D-Line retail avenue. Verified hardware swaps with official safe escrow certificates.", 4.6, "Infinix, Tecno, Xiaomi", "+2348060000003")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Verified Swapping Hubs", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black)
            Text("Physically verified partner shops offering accurate payouts & official escrow safety agreements.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }

        items(vendorsList) { vendor ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(vendor.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(vendor.center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Green.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("${vendor.ratings}", fontWeight = FontWeight.Bold, color = Color(0xFF047857), fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(vendor.details, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Speciality: ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text(vendor.specialization, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { /* simulated direct WhatsApp api dispatch */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp green
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", color = Color.White, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { /* simulated telephone action */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Partner", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- Escrow & Safe Trading Screen ---
@Composable
fun TipsScreen(viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Computer Village Escrow Manual", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black)
            Text("Maintain standard safety routines to evade gadget trade fraud in Nigeria.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }

        item {
            SafeManualCard(
                title = "1. Always Meet in Secure Public Spaces",
                desc = "Prefer indoor banks or secure restaurant hubs inside medical roads/plazas. Avoid remote dark corridors under stairs or corners outside Otigba street.",
                Icons.Default.LocalActivity
            )
        }

        item {
            SafeManualCard(
                title = "2. Enforce SIM, WiFi & GSM Signal Check",
                desc = "Never buy a phone without inserting your SIM card. Some refurbs look clean but have broken baseband chips (invalid IMEI, No service error) due to motherboard splits.",
                Icons.Default.SignalCellularAlt
            )
        }

        item {
            SafeManualCard(
                title = "3. Refuse iCloud/Google Account locks",
                desc = "Perform a complete system factory reset inside the presence of the vendor, and initialize the device as new. If an MDM configuration screen or remote login pops up, walk away immediately.",
                Icons.Default.Lock
            )
        }

        item {
            SafeManualCard(
                title = "4. Paper Receipts with matching IMEI",
                desc = "Ensure the shop distributes a formal sales receipt printed with their physical address, matching telephone, and correct IMEI/Serial parameters mapped.",
                Icons.Default.ReceiptLong
            )
        }

        item {
            SafeManualCard(
                title = "5. Escrow Swapping Protocols",
                desc = "If doing a mail-order or remote swap, utilize a trusted, physical escrow partner in the plaza to hold assets until diagnostic results compile cleanly.",
                Icons.Default.Verified
            )
        }
    }
}

@Composable
fun SafeManualCard(title: String, desc: String, icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

// --- Saved history Audit Screen ---
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val list by viewModel.valuationsHistory.collectAsStateWithLifecycle()
    val nairaFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    nairaFormat.maximumFractionDigits = 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Price Swapping Audit Logs", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black)
                Text("Past diagnostic appraisals.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            if (list.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearAllValuations() },
                    modifier = Modifier.testTag("btn_clear_history")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (list.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HistoryToggleOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Appraisals Yet", fontWeight = FontWeight.Bold)
                    Text("Perform a gadget check or photo scan to build your archive.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(list) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_card_${item.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (item.category) {
                                            "Phone" -> Icons.Default.PhoneAndroid
                                            "Laptop" -> Icons.Default.Laptop
                                            "Tablet" -> Icons.Default.TabletAndroid
                                            "Smartwatch" -> Icons.Default.Watch
                                            else -> Icons.Default.DevicesOther
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(item.brand, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(
                                    onClick = { viewModel.deleteValuation(item.id) },
                                    modifier = Modifier.testTag("btn_delete_history_${item.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(item.model, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(item.specs, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("APPRAISED VALUE ARV", fontSize = 10.sp, letterSpacing = 0.5.sp)
                                    Text(
                                        text = "${nairaFormat.format(item.calculatedValueMin)} - ${nairaFormat.format(item.calculatedValueMax)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(item.physicalCondition, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
