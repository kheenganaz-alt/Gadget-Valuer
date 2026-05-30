package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.data.VendorShop
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
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp) },
            selected = currentScreen == "tips",
            onClick = { onNavigate("tips") },
            modifier = Modifier.testTag("nav_profile")
        )
    }
}

// --- Dashboard Screen ---
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val history by viewModel.valuationsHistory.collectAsStateWithLifecycle()
    var textInput by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf("Phone") }
    var activeBrand by remember { mutableStateOf("Apple") }

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
                        text = "Interactive Valuation Desk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Appraise gadgets instantly. Select options below or enter manually.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // 1. Select Category Row
                    Text("1. SELECT CATEGORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        val categories = listOf(
                            "Phone" to "📱 Phones", 
                            "Laptop" to "💻 Laptops", 
                            "Tablet" to "📁 Tablets", 
                            "Smartwatch" to "⌚ Watches"
                        )
                        categories.forEach { (catId, catLabel) ->
                            val isSelected = activeCategory == catId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                                    .clickable { 
                                        activeCategory = catId
                                        activeBrand = "Apple"
                                        textInput = ""
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = catLabel, 
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 2. Select Brand Row
                    Text("2. SELECT BRAND", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    val brandsForCategory = when (activeCategory) {
                        "Phone" -> listOf("Apple", "Samsung", "Google", "Xiaomi", "Tecno", "Infinix")
                        "Laptop" -> listOf("Apple", "HP", "Dell", "Lenovo", "Asus", "Acer")
                        "Tablet" -> listOf("Apple", "Samsung", "Lenovo")
                        else -> listOf("Apple", "Samsung") // Smartwatch
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        brandsForCategory.forEach { b ->
                            val isSelected = activeBrand == b
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                                    .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { 
                                        activeBrand = b
                                        textInput = ""
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = b, 
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface, 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 3. Select Target Model
                    val popularModels = when (activeCategory) {
                        "Phone" -> when (activeBrand) {
                            "Apple" -> listOf("iPhone 15 Pro Max", "iPhone 15 Pro", "iPhone 14 Pro Max", "iPhone 13 Pro", "iPhone 12", "iPhone 11")
                            "Samsung" -> listOf("Galaxy S24 Ultra", "Galaxy S23 Ultra", "Galaxy S22 Ultra", "Galaxy Z Fold 5", "Galaxy A54")
                            "Google" -> listOf("Pixel 8 Pro", "Pixel 7 Pro", "Pixel 6a", "Pixel 7")
                            "Xiaomi" -> listOf("Redmi Note 12", "Xiaomi 13 Ultra", "Poco F5")
                            "Tecno" -> listOf("Camon 20 Pro", "Phantom X2 Pro", "Spark 10 Pro")
                            else -> listOf("Hot 30i", "Note 30 Pro", "Zero Ultra") // Infinix
                        }
                        "Laptop" -> when (activeBrand) {
                            "Apple" -> listOf("MacBook Pro M3", "MacBook Air M2", "MacBook Pro M1")
                            "HP" -> listOf("EliteBook 840 G8", "ProBook 450 G9", "Spectre x360")
                            "Dell" -> listOf("Latitude 7420", "XPS 13 9310", "Inspiron 15")
                            "Lenovo" -> listOf("ThinkPad T14", "Yoga Slim 7", "IdeaPad 3")
                            else -> listOf("ROG Zephyrus G14", "ZenBook 13", "Aspire 5") // Asus/Acer
                        }
                        "Tablet" -> when (activeBrand) {
                            "Apple" -> listOf("iPad Pro M2", "iPad Air 5", "iPad 10th Gen", "iPad Mini 6")
                            "Samsung" -> listOf("Galaxy Tab S9", "Galaxy Tab S8 Ultra", "Galaxy Tab A8")
                            else -> listOf("Tab P11 Pro", "Yoga Tab 11") // Lenovo
                        }
                        else -> { // Smartwatch
                            if (activeBrand == "Apple") listOf("Apple Watch Ultra 2", "Apple Watch Series 9", "Apple Watch SE")
                            else listOf("Galaxy Watch 6 Pro", "Galaxy Watch 5", "Galaxy Watch Active 2")
                        }
                    }

                    Text("3. SELECT POPULAR TARGET MODEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        popularModels.forEach { model ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .clickable { 
                                        textInput = model
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = model, 
                                    color = MaterialTheme.colorScheme.onSurface, 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Manual Input field Override
                    Text("OR ENTER CUSTOM MODEL MANUALLY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
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
                text = "Instant Value Appraisal",
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

        // Safe Market Essential Safe Trade Alert Banner
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
                            text = "Safe Market Trading Guide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Avoid getting scammed. Explore full escrow procedures & diagnostic lists.",
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
                        text = "Diagnostic & Fair Value Index",
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

// Robust and safe downsampled bitmap decoder that protects against OutOfMemoryError and native crashes
fun decodeUriToBitmap(context: android.content.Context, uri: android.net.Uri, maxDim: Int = 1024): android.graphics.Bitmap {
    val contentResolver = context.contentResolver
    
    // Attempt modern ImageDecoder path if API >= 28
    if (android.os.Build.VERSION.SDK_INT >= 28) {
        try {
            val source = android.graphics.ImageDecoder.createSource(contentResolver, uri)
            return android.graphics.ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                val w = info.size.width
                val h = info.size.height
                if (w > maxDim || h > maxDim) {
                    val ratio = w.toFloat() / h
                    val targetW = if (w > h) maxDim else (maxDim * ratio).toInt()
                    val targetH = if (w > h) (maxDim / ratio).toInt() else maxDim
                    decoder.setTargetSize(targetW, targetH)
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            // Fallthrough to standard BitmapFactory if ImageDecoder fails
        }
    }
    
    // Safe downsampled BitmapFactory path
    try {
        // Step 1: Decode dimensions only
        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        var pfd = contentResolver.openFileDescriptor(uri, "r")
        pfd?.use { fd ->
            android.graphics.BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, options)
        }
        
        val w = options.outWidth
        val h = options.outHeight
        
        // Calculate sample size (power of 2)
        var inSampleSize = 1
        if (w > maxDim || h > maxDim) {
            val halfW = w / 2
            val halfH = h / 2
            while ((halfW / inSampleSize) >= maxDim && (halfH / inSampleSize) >= maxDim) {
                inSampleSize *= 2
            }
        }
        
        // Step 2: Decode bitmap with sample size
        val decodeOptions = android.graphics.BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
        
        pfd = contentResolver.openFileDescriptor(uri, "r")
        val decoded = pfd?.use { fd ->
            android.graphics.BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, decodeOptions)
        }
        
        if (decoded != null) {
            // Precise scaling if it's still larger than maxDim after sampling
            if (decoded.width > maxDim || decoded.height > maxDim) {
                val ratio = decoded.width.toFloat() / decoded.height
                val targetW = if (decoded.width > decoded.height) maxDim else (maxDim * ratio).toInt()
                val targetH = if (decoded.width > decoded.height) (maxDim / ratio).toInt() else maxDim
                val scaled = android.graphics.Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
                if (scaled != decoded) {
                    decoded.recycle()
                }
                return scaled
            }
            return decoded
        }
    } catch (t: Throwable) {
        t.printStackTrace()
    }
    
    // Fallback simple decode (last resort)
    try {
        contentResolver.openInputStream(uri)?.use { stream ->
            val decoded = android.graphics.BitmapFactory.decodeStream(stream)
            if (decoded != null) return decoded
        }
    } catch (t: Throwable) {
        t.printStackTrace()
    }
    
    throw Exception("Secure photo parsing exhausted all options.")
}

// --- Native Camera & Diagnostics View ---
@Composable
fun SimulatedCameraView(viewModel: MainViewModel) {
    val context = LocalContext.current
    
    val detectedDevice = remember {
        val model = android.os.Build.MODEL
        val manufacturer = android.os.Build.MANUFACTURER
        if (model.contains("gphone", ignoreCase = true) || model.contains("emulator", ignoreCase = true) || model.contains("sdk", ignoreCase = true) || manufacturer.contains("Genymotion", ignoreCase = true)) {
            "Google Pixel 8"
        } else {
            val manufacturerCapitalized = manufacturer.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() }
            "$manufacturerCapitalized $model"
        }
    }

    var brandName by remember { mutableStateOf(detectedDevice) }
    var selectedCat by remember { mutableStateOf("Phone") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val finalName = brandName.ifBlank { if (selectedCat == "Phone") detectedDevice else "MacBook Air" }
            viewModel.analyzerQueryText.value = finalName
            viewModel.setCapturedBitmap(bitmap)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Could not open camera: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        } else {
            android.widget.Toast.makeText(context, "Camera permission is required to photograph your device for appraisal.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = decodeUriToBitmap(context, uri)
                val finalName = brandName.ifBlank { if (selectedCat == "Phone") detectedDevice else "MacBook Air" }
                viewModel.analyzerQueryText.value = finalName
                viewModel.setCapturedBitmap(bitmap)
            } catch (e: Throwable) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Error decoding photo securely", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
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
            Text("DEVICE DIAGNOSTICS SCANNER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Physical Gadget Appraisal",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Photograph your device to evaluate body condition, detect scratches, and value its worth securely.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // 1. Category Selection Row
                Text(
                    text = "SELECT DEVICE CATEGORY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    listOf("Phone", "Laptop", "Tablet", "Smartwatch").forEach { cat ->
                        val isSelected = selectedCat == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF334155))
                                .clickable { 
                                    selectedCat = cat 
                                    // Update default model if they haven't typed an explicit brand custom name!
                                    if (brandName.isBlank() || brandName == detectedDevice || brandName == "MacBook Air" || brandName == "iPad Air 5" || brandName == "Apple Watch Series 8") {
                                        brandName = when (cat) {
                                            "Phone" -> detectedDevice
                                            "Laptop" -> "MacBook Air"
                                            "Tablet" -> "iPad Air 5"
                                            "Smartwatch" -> "Apple Watch Series 8"
                                            else -> "iPhone 13 Pro"
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. User specifies model input string
                Text("Verify / Enter Device Model Name To Appraise:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    placeholder = { Text("e.g., iPhone 15 Pro Max", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diag_name_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF334155),
                        unfocusedContainerColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.LightGray,
                        unfocusedPlaceholderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Popular Quick Choose Targets Row (Scrollable horizontally)
                Text(
                    text = "QUICK MATCH POPULAR DEVICES (TAP TO SEED)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                // Set of targets based on category
                val popularTargets = when (selectedCat) {
                    "Laptop" -> listOf("MacBook Pro M3", "MacBook Air M2", "HP EliteBook 840", "Dell Latitude 5420", "Lenovo ThinkPad X1")
                    "Tablet" -> listOf("iPad Pro M2", "iPad Air 5", "Samsung Galaxy Tab S9", "Surface Pro 9")
                    "Smartwatch" -> listOf("Apple Watch Ultra 2", "Apple Watch Series 9", "Galaxy Watch 6")
                    else -> listOf(
                        "✨ My Detected ($detectedDevice)",
                        "iPhone 15 Pro Max", 
                        "iPhone 14 Pro Max", 
                        "iPhone 13 Pro", 
                        "Samsung Galaxy S24 Ultra", 
                        "Infinix Hot 40 Pro", 
                        "Tecno Spark 20 Pro"
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    popularTargets.forEach { target ->
                        val cleanTarget = if (target.startsWith("✨")) detectedDevice else target
                        val isMatched = brandName.equals(cleanTarget, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, if (isMatched) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .background(if (isMatched) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFF1E293B))
                                .clickable { 
                                    brandName = cleanTarget
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = target,
                                color = if (isMatched) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Functional Trigger Deck
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Choose Photo Appraisal Input Method:", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Real Camera Button
                    Button(
                        onClick = {
                            val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            )
                            if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                try {
                                    cameraLauncher.launch(null)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Could not open camera: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            } else {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("camera_shutter"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Native Camera")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Camera", fontSize = 12.sp)
                    }

                    // Gallery Selection Button
                    FilledTonalButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gallery_picker"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = "Gallery Picker")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Gallery", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Simulator Appraisal for Emulator testing
                Button(
                    onClick = {
                        val mockName = brandName.ifBlank { if (selectedCat == "Phone") detectedDevice else "MacBook Air" }
                        val mockBitmap = createMockDeviceBitmap(mockName, selectedCat)
                        viewModel.analyzerQueryText.value = mockName
                        viewModel.setCapturedBitmap(mockBitmap)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_emulator_sim"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AutoMode, contentDescription = "Simulate Scan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Interactive Scanner Appraisal")
                }
            }
        }
    }
}

// --- Preview / Confirm Captured Photo screen ---
@Composable
fun PhotoConfirmScreen(viewModel: MainViewModel) {
    val bitmapState by viewModel.capturedImage.collectAsStateWithLifecycle()
    val query by viewModel.analyzerQueryText.collectAsStateWithLifecycle()
    val bitmap = bitmapState

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
                        text = "Running Gemini AI diagnostics & checking secondary market trade catalogs...",
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
                "UK Used (Mint)" -> Pair(res.valueMinGradeA, res.valueMaxGradeA)
                "UK Used (Good)" -> Pair(res.valueMinGradeB, res.valueMaxGradeB)
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
                                 listOf("UK Used (Mint)", "UK Used (Good)", "Locally Used (Fair)").forEach { item ->
                                     Row(
                                         verticalAlignment = Alignment.CenterVertically,
                                         modifier = Modifier.clickable { viewModel.selectedCondition.value = item }
                                     ) {
                                         RadioButton(
                                             selected = condition == item,
                                             onClick = { viewModel.selectedCondition.value = item }
                                         )
                                         Text(item, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

                // AI Expert Advice specifically for secure secondary market trades and transactions
                item {
                    Text("Expert Appraisal Advice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

                // Save Appraisal or Redirect to Shops
                item {
                    var appraisalSaved by remember { mutableStateOf(false) }

                    Column {
                        Button(
                            onClick = {
                                viewModel.saveCustomizedAppraisal(
                                    result = res,
                                    condition = condition,
                                    hasCharger = chargerPresent,
                                    hasReceipt = receiptPresent,
                                    screenPerfect = screenIntactState,
                                    batteryPerfect = batteryNormalState,
                                    finalMin = finalMin,
                                    finalMax = finalMax
                                )
                                appraisalSaved = true
                            },
                            enabled = !appraisalSaved,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (appraisalSaved) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_save_appraisal")
                        ) {
                            Icon(
                                imageVector = if (appraisalSaved) Icons.Default.Check else Icons.Default.Save,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (appraisalSaved) "Appraisal Saved to Portfolio" else "Save Adjusted Appraisal")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.navigateTo("vendors") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_redirect_shops"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Locate Verified Partner Shops")
                        }
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

@Composable
fun VendorsScreen(viewModel: MainViewModel) {
    val allVendors by viewModel.vendorsList.collectAsStateWithLifecycle()
    var selectedState by remember { mutableStateOf("Lagos") }
    
    val filteredVendors = allVendors.filter { 
        it.status == "approved" && it.state.equals(selectedState, ignoreCase = true) 
    }

    var showApplyDialog by remember { mutableStateOf(false) }
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var operatingState by remember { mutableStateOf("Lagos") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    
    // Check categories selection
    val categoriesOptions = listOf("Phones", "Laptops", "Accessories", "Repairs", "UK Used", "Fairly Used", "Brand new")
    var selectedCategoryChips by remember { mutableStateOf(setOf<String>()) }
    
    var logoAttached by remember { mutableStateOf(false) }
    var docAttached by remember { mutableStateOf(false) }
    var submissionSuccess by remember { mutableStateOf(false) }
    
    // Dialog details viewer
    var selectedViewShop by remember { mutableStateOf<VendorShop?>(null) }

    val nigerianStatesList = listOf(
        "Lagos", "Abuja (FCT)", "Rivers", "Niger", "Kano", "Oyo", "Anambra", "Delta", "Edo", "Enugu", "Abia", "Ogun", "Kaduna", "Kogi"
    )

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Descriptive Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Registered Gadget Shops", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Locate verified physical dealers in your State for secure buy-and-sell, fast trade-ins, and escrow transactions.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }

        // Horizontal State Selector Picker
        item {
            Column {
                Text(
                    text = "FILTER SHOPS BY NIGERIAN STATE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    nigerianStatesList.take(8).forEach { stateItem ->
                        val isSelected = selectedState.equals(stateItem, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { selectedState = stateItem }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = stateItem,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Advanced State drop-down choice
                var showStatePickerMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedButton(
                        onClick = { showStatePickerMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_state_picker_dropdown")
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Show All States Picker ($selectedState)")
                    }
                    
                    DropdownMenu(
                        expanded = showStatePickerMenu,
                        onDismissRequest = { showStatePickerMenu = false }
                    ) {
                        nigerianStatesList.forEach { sName ->
                            DropdownMenuItem(
                                text = { Text(sName) },
                                onClick = {
                                    selectedState = sName
                                    showStatePickerMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Apply Card Banner for new vendors
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Own an Active Gadget Plaza?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Apply to list your shop and highlight your retail & trade-in channels. Safe, checked physical verification standards are held.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showApplyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_apply_vendor")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Register Shop Listing", fontSize = 12.sp)
                    }
                }
            }
        }

        // Filter result display
        if (filteredVendors.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No registered shops available in this state yet. Registered sellers can apply to be listed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("txt_empty_state_shops")
                        )
                    }
                }
            }
        } else {
            items(filteredVendors) { vendor ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = vendor.name, 
                                        fontWeight = FontWeight.Bold, 
                                        fontSize = 17.sp, 
                                        maxLines = 1, 
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (vendor.isVerified) {
                                        Icon(
                                            Icons.Default.Verified, 
                                            contentDescription = "Verified Badged Dealer", 
                                            tint = Color(0xFF10B981), 
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("${vendor.city}, ${vendor.state}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFBBF24).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${vendor.ratings}", fontWeight = FontWeight.Bold, color = Color(0xFFD97706), fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(vendor.details.ifBlank { "High grade electronic sales, trade-ins & instant appraisals at verified mall locations." }, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
                            Text("Categories: ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = vendor.categories, 
                                fontSize = 11.sp, 
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val formattedNum = vendor.phone.replace("+", "").replace(" ", "").trim()
                                        val url = "https://api.whatsapp.com/send?phone=$formattedNum"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp", color = Color.White, fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = { selectedViewShop = vendor },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Shop", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dynamic Viewer Dialog for View Shop ---
    selectedViewShop?.let { shopDetail ->
        AlertDialog(
            onDismissRequest = { selectedViewShop = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(shopDetail.name, fontWeight = FontWeight.Black)
                    if (shopDetail.isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    if (shopDetail.isVerified) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.12f))
                                .padding(8.dp)
                        ) {
                            Text("🛡️ Officially approved & verified physical trading hub.", color = Color(0xFF047857), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                        ) {
                            Text("⏳ Pending physical location review by administration.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }

                    Text("📍 PHYSICAL MALL ADDRESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(shopDetail.address.ifBlank { shopDetail.center }, style = MaterialTheme.typography.bodyMedium)

                    Text("👤 PROPRIETOR / REPRESENTATIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(shopDetail.ownerName.ifBlank { "Official Slot Administrator" }, style = MaterialTheme.typography.bodyMedium)

                    Text("📞 DIRECT PHONE NUMBERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(shopDetail.phone.ifBlank { shopDetail.telephone }, style = MaterialTheme.typography.bodyMedium)

                    if (shopDetail.email.isNotBlank()) {
                        Text("✉️ REGISTERED EMAIL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(shopDetail.email, style = MaterialTheme.typography.bodyMedium)
                    }

                    Text("⚙️ TRADE CATEGORIES APPROVED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.wrapContentWidth()) {
                        shopDetail.categories.split(",").forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp, bottom = 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(cat.trim(), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("💡 TRADE AND DIAGNOSTIC DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(shopDetail.details.ifBlank { "Provides certified UK Used device appraisals, trade-ins, and secure transactions." }, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = { selectedViewShop = null }) {
                    Text("Close")
                }
            }
        )
    }

    // --- Dynamic Dialog: Registration Form ---
    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { 
                showApplyDialog = false
                submissionSuccess = false
            },
            title = { Text(if (submissionSuccess) "Shop Registration Filed" else "Apply for Partner Placement") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (submissionSuccess) {
                        Text(
                            "Thank you! Your shop registration has been successfully captured under PENDING status.\n\nOnce physical address verification checks complete by Administrator (Kheenganaz@gmail.com) in your State ($operatingState), your shop will appear on the dynamic state picker directory list.\n\nYou can view your application status in the Admin review page.",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text("Fill out mandatory details. Applications will undergo strict address review.", fontSize = 12.sp)

                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("Shop or Brand Name *") },
                            modifier = Modifier.fillMaxWidth().testTag("apply_shop_name")
                        )

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner or Director Name *") },
                            modifier = Modifier.fillMaxWidth().testTag("apply_owner_name")
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Office Phone Number *") },
                            placeholder = { Text("e.g. +2348030001111") },
                            modifier = Modifier.fillMaxWidth().testTag("apply_phone")
                        )

                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp Hotline *") },
                            placeholder = { Text("e.g. +2348030001111") },
                            modifier = Modifier.fillMaxWidth().testTag("apply_whatsapp")
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Business Email (Optional)") },
                            modifier = Modifier.fillMaxWidth().testTag("apply_email")
                        )

                        // State picker choice
                        var operatingStateExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = operatingState,
                                onValueChange = {},
                                label = { Text("Operating Nigerian State *") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { operatingStateExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { operatingStateExpanded = true }
                            )
                            DropdownMenu(
                                expanded = operatingStateExpanded,
                                onDismissRequest = { operatingStateExpanded = false }
                            ) {
                                nigerianStatesList.forEach { sName ->
                                    DropdownMenuItem(
                                        text = { Text(sName) },
                                        onClick = {
                                            operatingState = sName
                                            operatingStateExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City or Local Area *") },
                            placeholder = { Text("e.g. Ikeja, Wuse II, Gbagada") },
                            modifier = Modifier.fillMaxWidth().testTag("apply_city")
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Physical Shop Address / Plaza Suite *") },
                            modifier = Modifier.fillMaxWidth().testTag("apply_address")
                        )

                        // Categories checkbox chips selection
                        Text("SELECT SALES & SERVICE CATEGORIES *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Column {
                            categoriesOptions.forEach { option ->
                                val isChecked = selectedCategoryChips.contains(option)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategoryChips = if (isChecked) {
                                                selectedCategoryChips - option
                                            } else {
                                                selectedCategoryChips + option
                                            }
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            selectedCategoryChips = if (isChecked) {
                                                selectedCategoryChips - option
                                            } else {
                                                selectedCategoryChips + option
                                            }
                                        }
                                    )
                                    Text(option, fontSize = 13.sp)
                                }
                            }
                        }

                        // Attach logo optional logic
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text("Shop Logo Icon *", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            OutlinedButton(
                                onClick = { logoAttached = !logoAttached },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (logoAttached) Color(0xFF10B981) else MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(if (logoAttached) Icons.Default.Check else Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (logoAttached) "Attached" else "Upload Logo", fontSize = 11.sp)
                            }
                        }

                        // Attach business verification document optional
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Text("CAC / CAC Registration Doc *", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            OutlinedButton(
                                onClick = { docAttached = !docAttached },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (docAttached) Color(0xFF10B981) else MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(if (docAttached) Icons.Default.Check else Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (docAttached) "Registered" else "Attach CAC", fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (submissionSuccess) {
                    Button(onClick = { 
                        showApplyDialog = false
                        submissionSuccess = false
                        shopName = ""
                        ownerName = ""
                        phone = ""
                        whatsapp = ""
                        email = ""
                        city = ""
                        address = ""
                        selectedCategoryChips = setOf()
                        logoAttached = false
                        docAttached = false
                    }) {
                        Text("OK")
                    }
                } else {
                    val isValid = shopName.isNotBlank() && ownerName.isNotBlank() && phone.isNotBlank() && whatsapp.isNotBlank() && city.isNotBlank() && address.isNotBlank() && selectedCategoryChips.isNotEmpty()
                    Button(
                        onClick = {
                            if (isValid) {
                                val concatCategories = selectedCategoryChips.joinToString(", ")
                                viewModel.submitVendorApplication(
                                    name = shopName,
                                    ownerName = ownerName,
                                    phone = phone,
                                    whatsapp = whatsapp,
                                    email = email,
                                    state = operatingState,
                                    city = city,
                                    address = address,
                                    categories = concatCategories,
                                    logoUrl = if (logoAttached) "attached" else null,
                                    verificationDocUrl = if (docAttached) "attached" else null
                                )
                                submissionSuccess = true
                            }
                        },
                        enabled = isValid,
                        modifier = Modifier.testTag("apply_submit_btn")
                    ) {
                        Text("Submit Shop details")
                    }
                }
            },
            dismissButton = {
                if (!submissionSuccess) {
                    TextButton(onClick = { showApplyDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

// --- Escrow & Safe Trading Screen ---
@Composable
fun TipsScreen(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val userEmail by viewModel.loggedInUserEmail.collectAsStateWithLifecycle()
    val userName by viewModel.loggedInUserName.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isUserAdmin.collectAsStateWithLifecycle()
    val allVendors by viewModel.vendorsList.collectAsStateWithLifecycle()
    val history by viewModel.valuationsHistory.collectAsStateWithLifecycle()

    val pendingVendors = allVendors.filter { it.status == "pending" }

    var loginEmail by remember { mutableStateOf("") }
    var loginName by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf(0) } // 0 = Profile & Stats, 1 = Safety Index

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Text("User Profile Portal", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black)
            Text("Access verified trader credentials, application stats, and safe market transaction rules.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }

        // Tab Selector for Organization
        item {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("My Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }}
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Safe Trade Rules", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }}
                )
            }
        }

        if (activeTab == 0) {
            // PROFILE & STATS TAB
            
            // Profile Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (!isLoggedIn) {
                            Text("Activate User Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Log in to verify listings, submit shops, or activate admin privileges.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))
                            
                            OutlinedTextField(
                                value = loginName,
                                onValueChange = { loginName = it },
                                label = { Text("Your Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("auth_name")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it },
                                label = { Text("Your Email Address") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("auth_email"),
                                placeholder = { Text("e.g. Kheenganaz@gmail.com") }
                            )
                            
                            // Help hint for administrator login
                            Text(
                                "🔑 Admin Hint: Log in with \"Kheenganaz@gmail.com\" to manage pending vendor applications.", 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                            )

                            Button(
                                onClick = {
                                    if (loginEmail.isNotBlank() && loginName.isNotBlank()) {
                                        viewModel.loginUser(loginEmail, loginName)
                                    }
                                },
                                enabled = loginEmail.isNotBlank() && loginName.isNotBlank(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("auth_submit_btn")
                            ) {
                                Text("Log In / Sign Up")
                            }
                        } else {
                            // User is Logged In
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(if (isAdmin) Color(0xFFFFECEF) else MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userName.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAdmin) Color(0xFFE11D48) else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(userEmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                                
                                IconButton(
                                    onClick = { viewModel.logoutUser() },
                                    modifier = Modifier.testTag("auth_logout_btn")
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.error)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAdmin) Color(0xFFFFE4E6) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isAdmin) Icons.Default.Security else Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = if (isAdmin) Color(0xFFE11D48) else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isAdmin) "🛡️ Administrator Privileges Active" else "🛡️ Registered Trader Account Active",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAdmin) Color(0xFF9F1239) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                             
                            // Personal Gemini Key override config card
                            val savedKey by viewModel.customGeminiApiKey.collectAsStateWithLifecycle()
                            var apiKeyValue by remember { mutableStateOf(savedKey) }
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Connect Your Own Gemini API Key", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Override the default server-side API key with your own custom key to direct requests through your personal Google AI Studio console.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = apiKeyValue,
                                        onValueChange = { 
                                            apiKeyValue = it 
                                            viewModel.updateCustomGeminiApiKey(it)
                                        },
                                        placeholder = { Text("AIzaSy...", fontSize = 12.sp) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("custom_key_input"),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                        trailingIcon = {
                                            if (apiKeyValue.isNotBlank()) {
                                                IconButton(onClick = { 
                                                    apiKeyValue = ""
                                                    viewModel.updateCustomGeminiApiKey("")
                                                }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                                }
                                            }
                                        }
                                    )
                                    if (apiKeyValue.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("⚡ Overridden Key Active: Real-time Gemini 3.5 live prices will trigger.", fontSize = 10.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Real Statistics Workspace Card
            if (isLoggedIn) {
                item {
                    Text("Your Diagnostic Statistics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Total Evaluated", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("${history.size} Devices", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Reputed Grade", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("Silver Member", fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Administrative Workshop Block
            if (isLoggedIn && isAdmin) {
                item {
                    Text("Admin Review Workstation", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                    Text("Manage status of pending partner shop requests for My Gadget Valuer.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }

                if (pendingVendors.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("🎉 All systems audited. There are no pending vendor applications!", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                } else {
                    items(pendingVendors) { application ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(2.dp, Color(0xFFE11D48).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(application.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("👤 Proprietor: ${application.ownerName.ifBlank { "N/A" }}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("📞 Phone: ${application.phone.ifBlank { application.telephone }}", fontSize = 12.sp)
                                Text("💬 WhatsApp Hot-line: ${application.whatsapp}", fontSize = 12.sp)
                                if (application.email.isNotBlank()) {
                                    Text("✉️ Email Address: ${application.email}", fontSize = 12.sp)
                                }
                                Text("📍 Operating Nigerian State: ${application.state}", fontSize = 12.sp)
                                Text("🗺️ City/Area: ${application.city}", fontSize = 12.sp)
                                Text("🏢 Plaza Mall Address: ${application.address.ifBlank { application.center }}", fontSize = 12.sp)
                                Text("🏷️ Approved Trade Categories: ${application.categories}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (application.logoUrl != null) {
                                    Text("🖼️ Shop Logo Icon: Uploaded & Associated", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                                if (application.verificationDocUrl != null) {
                                    Text("📃 CAC Doc / Business Proof: Attached & Signed", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.approveVendor(application.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("admin_approve_${application.id}")
                                    ) {
                                        Text("Approve & List", fontSize = 12.sp)
                                    }
                                    
                                    OutlinedButton(
                                        onClick = { viewModel.deleteVendor(application.id) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("admin_reject_${application.id}")
                                    ) {
                                        Text("Reject / Delete", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // SAFE TRADE RULES TAB
            item {
                Text("Trade Safeguards & Escrows", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Text("Follow strict safety guidelines inside gadget market malls and plazas.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }

            item {
                SafeManualCard(
                    title = "1. Always Meet in Secure Public Spaces",
                    desc = "Prefer indoor bank spaces or secure restaurant hubs inside public hubs or plazas. Avoid remote dark corridors under stairs or corners.",
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
                    title = "3. Refuse iCloud/Google Account Locks",
                    desc = "Perform a complete system factory reset in the presence of the vendor, and initialize the device as new. If an MDM configuration screen or remote login pops up, walk away immediately.",
                    Icons.Default.Lock
                )
            }

            item {
                SafeManualCard(
                    title = "4. Paper Receipts with Matching IMEI",
                    desc = "Ensure the shop distributes a formal sales receipt printed with their physical address, matching telephone, and correct IMEI/Serial parameters mapped.",
                    Icons.Default.ReceiptLong
                )
            }

            item {
                SafeManualCard(
                    title = "5. Escrow and Secured Payment Protocols",
                    desc = "If doing a mail-order or remote purchase, utilize a trusted, physical escrow partner in the plaza to hold assets until diagnostic results compile cleanly.",
                    Icons.Default.Verified
                )
            }
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
                Text("Appraisal History Logs", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black)
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

// Procedural high-fidelity device mock bitmap generator for smooth demo appraisals
fun createMockDeviceBitmap(name: String, category: String): Bitmap {
    val size = 512
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint()
    
    // Draw background Space Slate
    paint.color = android.graphics.Color.parseColor("#1e293b")
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
    
    // Draw device outer casing frame
    paint.color = android.graphics.Color.parseColor("#0ea5e9")
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 14f
    val rSide = 120f
    val rTop = 50f
    canvas.drawRoundRect(rSide, rTop, size.toFloat() - rSide, size.toFloat() - rTop, 34f, 34f, paint)
    
    // Draw screen container background
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#334155")
    canvas.drawRoundRect(rSide + 12f, rTop + 12f, size.toFloat() - rSide - 12f, size.toFloat() - rTop - 12f, 24f, 24f, paint)
    
    // Draw camera notch or sensor island
    paint.color = android.graphics.Color.BLACK
    canvas.drawRoundRect(size.toFloat() / 2f - 40f, rTop + 24f, size.toFloat() / 2f + 40f, rTop + 44f, 10f, 10f, paint)
    
    // Draw dynamic green appraisal scan-line overlay
    paint.color = android.graphics.Color.parseColor("#10b981")
    paint.alpha = 90
    canvas.drawRect(0f, (size / 2 - 12).toFloat(), size.toFloat(), (size / 2 + 12).toFloat(), paint)
    
    // Text labels overlay on mockup image
    paint.color = android.graphics.Color.WHITE
    paint.style = android.graphics.Paint.Style.FILL
    paint.alpha = 255
    paint.textSize = 28f
    paint.textAlign = android.graphics.Paint.Align.CENTER
    canvas.drawText(name.uppercase(), (size / 2).toFloat(), (size - 105).toFloat(), paint)
    
    paint.textSize = 18f
    paint.color = android.graphics.Color.parseColor("#38bdf8")
    canvas.drawText("Appraisal Diagnostics: $category", (size / 2).toFloat(), (size - 145).toFloat(), paint)
    
    return bitmap
}
