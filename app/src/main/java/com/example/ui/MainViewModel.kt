package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GadgetAnalyzer
import com.example.ai.AnalyzerResult
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository: AppRepository
    private val analyzer = GadgetAnalyzer()

    // Authentication session state
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _adminSuccess = MutableStateFlow<String?>(null)
    val adminSuccess: StateFlow<String?> = _adminSuccess.asStateFlow()

    // DB flows
    val allShops: StateFlow<List<ShopEntity>>
    val allVendors: StateFlow<List<VendorEntity>>
    val allListings: StateFlow<List<GadgetListingEntity>>
    val allUsers: StateFlow<List<UserEntity>>
    val allReports: StateFlow<List<ReportEntity>>
    val supportMessages: StateFlow<List<SupportMessageEntity>>
    val adminActionLogs: StateFlow<List<AdminActionLogEntity>>
    val appSettings: StateFlow<AppSettingEntity?>
    val allScans: StateFlow<List<GadgetScanEntity>>

    // Scan flows
    private val _userScans = MutableStateFlow<List<GadgetScanEntity>>(emptyList())
    val userScans: StateFlow<List<GadgetScanEntity>> = _userScans.asStateFlow()

    // AI active scanning indicator
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<AnalyzerResult?>(null)
    val scanResult: StateFlow<AnalyzerResult?> = _scanResult.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database)

        allShops = repository.allShops.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allVendors = repository.allVendors.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allListings = repository.allListings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allReports = repository.allReports.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        supportMessages = repository.allMessages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        adminActionLogs = repository.allLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        appSettings = repository.getSettingsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        allScans = repository.allScans.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            repository.seedIfNeeded()
            // Default load seeded user "Chidi" for fast evaluation, but admin is also ready in the DB.
            val chidi = repository.getUserByEmail("chidi@gmail.com")
            if (chidi != null) {
                _currentUser.value = chidi
                loadScansForUser(chidi.id)
            }
        }
    }

    fun loadScansForUser(userId: Int) {
        viewModelScope.launch {
            repository.getScansForUser(userId).collect {
                _userScans.value = it
            }
        }
    }

    // User authentication flows
    fun registerUser(
        fullName: String,
        email: String,
        phone: String,
        pass: String,
        state: String,
        city: String,
        requestedRole: String
    ) {
        viewModelScope.launch {
            _authError.value = null
            if (fullName.isBlank() || email.isBlank() || phone.isBlank() || pass.isBlank()) {
                _authError.value = "All fields are required"
                return@launch
            }

            // Case-insensitive admin matching
            val isEmailAdmin = email.lowercase().trim() == "kheenganaz@gmail.com"
            val finalRole = if (isEmailAdmin) "ADMIN" else requestedRole

            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _authError.value = "An account with this email already exists"
                return@launch
            }

            val newUser = UserEntity(
                fullName = fullName,
                email = email.lowercase().trim(),
                phoneNumber = phone,
                password = pass,
                state = state,
                city = city,
                role = finalRole,
                status = "ACTIVE"
            )

            val insertedId = repository.insertUser(newUser)
            val registeredUser = newUser.copy(id = insertedId.toInt())
            _currentUser.value = registeredUser
            loadScansForUser(registeredUser.id)
        }
    }

    fun loginUser(email: String, pass: String) {
        viewModelScope.launch {
            _authError.value = null
            val trimEmail = email.lowercase().trim()
            val user = repository.getUserByEmail(trimEmail)
            if (user == null) {
                _authError.value = "User not found"
                return@launch
            }

            if (user.status == "SUSPENDED") {
                _authError.value = "This account has been suspended by the administrator."
                return@launch
            }

            if (user.password != pass) {
                _authError.value = "Invalid password"
                return@launch
            }

            _currentUser.value = user
            loadScansForUser(user.id)
        }
    }

    fun switchRoleForTesting(role: String) {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            // Support status syncing
            val updated = current.copy(role = role)
            _currentUser.value = updated
        }
    }

    fun logout() {
        _currentUser.value = null
        _userScans.value = emptyList()
        _scanResult.value = null
    }

    // AI Scanner trigger
    fun scanGadget(bitmap: Bitmap?, manualQuery: String?) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = null
            try {
                val query = manualQuery ?: "iPhone 13"
                val result = analyzer.analyzeGadget(bitmap, query)
                _scanResult.value = result

                // Automatically save scan results to user history
                val userId = _currentUser.value?.id ?: 1
                repository.insertScan(
                    GadgetScanEntity(
                        userId = userId,
                        imageUri = null,
                        gadgetName = result.gadgetName,
                        brand = result.brand,
                        model = result.model,
                        category = result.category,
                        storage = result.storage,
                        color = result.color,
                        estimatedCondition = result.estimatedCondition,
                        confidenceScore = result.confidenceScore,
                        ukUsedMin = result.ukUsedMin,
                        ukUsedMax = result.ukUsedMax,
                        fairlyUsedMin = result.fairlyUsedMin,
                        fairlyUsedMax = result.fairlyUsedMax,
                        brandNewMin = result.brandNewMin,
                        brandNewMax = result.brandNewMax,
                        marketTrend = result.marketTrend,
                        commonIssues = result.commonIssues,
                        conditionFactors = result.conditionFactors,
                        bestResaleAdvice = result.bestResaleAdvice,
                        screenCracksScore = result.screenCracksScore,
                        bezelDamageScore = result.bezelDamageScore,
                        portWearScore = result.portWearScore,
                        cosmeticScratchesScore = result.cosmeticScratchesScore,
                        conditionConfidenceScore = result.conditionConfidenceScore
                    )
                )
            } catch (e: Exception) {
                _authError.value = "Scan failed: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun clearResult() {
        _scanResult.value = null
    }

    // Add Listings (Defaults to PENDING status now for proper moderation cycle)
    fun addListing(
        title: String,
        brand: String,
        model: String,
        category: String,
        condition: String,
        price: Double,
        state: String,
        city: String,
        desc: String,
        phone: String,
        whatsApp: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            // Auto approve admin's listings for convenient testing, otherwise state is PENDING
            val initStatus = if (user.role == "ADMIN") "APPROVED" else "PENDING"
            repository.insertListing(
                GadgetListingEntity(
                    userId = user.id,
                    posterName = user.fullName,
                    gadgetName = title,
                    brand = brand,
                    model = model,
                    category = category,
                    condition = condition,
                    price = price,
                    state = state,
                    city = city,
                    imageUri = null,
                    description = desc,
                    contactPhone = phone,
                    contactWhatsApp = whatsApp,
                    status = initStatus
                )
            )
        }
    }

    // Request Registration For Shops & Vendors
    fun registerShop(
        shopName: String,
        ownerName: String,
        phone: String,
        whatsApp: String,
        email: String,
        state: String,
        city: String,
        address: String,
        categoriesSold: String
    ) {
        viewModelScope.launch {
            repository.insertShop(
                ShopEntity(
                    shopName = shopName,
                    ownerName = ownerName,
                    phone = phone,
                    whatsApp = whatsApp,
                    email = email,
                    state = state,
                    city = city,
                    address = address,
                    categoriesSold = categoriesSold,
                    shopLogo = "default_logo",
                    verificationDocument = "Uploaded_CAC_Doc.pdf",
                    status = "PENDING"
                )
            )
        }
    }

    fun registerVendor(
        vendorName: String,
        ownerName: String,
        phone: String,
        whatsApp: String,
        email: String,
        state: String,
        city: String,
        address: String,
        categoriesSold: String
    ) {
        viewModelScope.launch {
            repository.insertVendor(
                VendorEntity(
                    vendorName = vendorName,
                    ownerName = ownerName,
                    phone = phone,
                    whatsApp = whatsApp,
                    email = email,
                    state = state,
                    city = city,
                    address = address,
                    categoriesSold = categoriesSold,
                    vendorLogo = "default_logo",
                    verificationDocument = "Uploaded_National_ID.pdf",
                    status = "PENDING"
                )
            )
        }
    }

    // Reports submittals
    fun submitReport(
        targetType: String,
        targetId: Int,
        targetName: String,
        reason: String,
        details: String
    ) {
        val user = _currentUser.value
        viewModelScope.launch {
            repository.insertReport(
                ReportEntity(
                    userId = user?.id ?: 0,
                    reporterName = user?.fullName ?: "Anonymous Guest",
                    reporterEmail = user?.email ?: "guest@gadgetvaluer.ng",
                    targetType = targetType,
                    targetId = targetId,
                    targetName = targetName,
                    reason = reason,
                    details = details,
                    status = "PENDING"
                )
            )
        }
    }

    // Contact Support Message Form
    fun submitSupport(name: String, email: String, subject: String, message: String) {
        if (name.isBlank() || email.isBlank() || message.isBlank()) return
        viewModelScope.launch {
            repository.insertMessage(
                SupportMessageEntity(
                    senderName = name,
                    senderEmail = email,
                    subject = subject,
                    message = message,
                    status = "OPEN"
                )
            )
        }
    }

    fun clearAdminSuccess() {
        _adminSuccess.value = null
    }

    // =========================================================================
    // SECURITY CONSTRAINT: Only ADMIN role is authorized to update database state
    // =========================================================================
    private fun verifyAdminOrThrow() {
        val user = _currentUser.value
        if (user == null || user.role != "ADMIN" || user.status == "SUSPENDED") {
            throw SecurityException("Unauthorized Access attempt. Only verified administrators can execute this command.")
        }
    }

    private suspend fun logAction(action: String, targetType: String, targetId: Int, oldVal: String = "", newVal: String = "") {
        val admin = _currentUser.value ?: return
        repository.insertLog(
            AdminActionLogEntity(
                adminUserId = admin.id,
                adminEmail = admin.email,
                actionType = action,
                targetType = targetType,
                targetId = targetId,
                oldValue = oldVal,
                newValue = newVal
            )
        )
    }

    // USER ADMINISTRATION ACTS
    fun adminUpdateUserStatus(userId: Int, status: String) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateUserStatus(userId, status)
            logAction("CHANGE_STATUS", "USER", userId, "UNKNOWN", status)
            _adminSuccess.value = "User account has been marked $status successfully"
        }
    }

    fun adminPromoteUser(userId: Int, newRole: String) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateUserRole(userId, newRole)
            logAction("PROMOTE", "USER", userId, "USER/SHOP/VENDOR", newRole)
            _adminSuccess.value = "User promoted to $newRole status"
        }
    }

    fun adminDeleteUser(userId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.deleteUser(userId)
            logAction("DELETE", "USER", userId)
            _adminSuccess.value = "User has been deleted from records"
        }
    }

    // SHOP MODERATION ACTS
    fun adminApproveShop(shopId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateShopStatus(shopId, "APPROVED")
            logAction("APPROVE", "SHOP", shopId, "PENDING", "APPROVED")
            _adminSuccess.value = "Shop has been approved and published to public catalog"
        }
    }

    fun adminRejectShop(shopId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateShopStatus(shopId, "REJECTED")
            logAction("REJECT", "SHOP", shopId, "PENDING", "REJECTED")
            _adminSuccess.value = "Shop application was rejected"
        }
    }

    fun adminSuspendShop(shopId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateShopStatus(shopId, "SUSPENDED")
            logAction("SUSPEND", "SHOP", shopId, "APPROVED", "SUSPENDED")
            _adminSuccess.value = "Shop listing suspended indefinitely"
        }
    }

    fun adminReactivateShop(shopId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateShopStatus(shopId, "APPROVED")
            logAction("REACTIVATE", "SHOP", shopId, "SUSPENDED", "APPROVED")
            _adminSuccess.value = "Shop reinstated to active index"
        }
    }

    fun adminEditShopLocation(shopId: Int, state: String, city: String) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateShopState(shopId, state)
            repository.updateShopCity(shopId, city)
            logAction("EDIT_LOCATION", "SHOP", shopId, "PREVIOUS", "$state - $city")
            _adminSuccess.value = "Shop location updated to $city, $state"
        }
    }

    fun adminVerifyShop(shopId: Int, isVerified: Boolean) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateShopVerification(shopId, isVerified)
            logAction(if (isVerified) "VERIFY" else "UNVERIFY", "SHOP", shopId, (!isVerified).toString(), isVerified.toString())
            _adminSuccess.value = if (isVerified) "Shop marked as verified (Badge active)" else "Verification Badge removed"
        }
    }

    fun adminDeleteShop(shopId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.deleteShop(shopId)
            logAction("DELETE", "SHOP", shopId)
            _adminSuccess.value = "Shop deleted from the repository"
        }
    }

    // VENDOR ACTIONS
    fun adminApproveVendor(vendorId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateVendorStatus(vendorId, "APPROVED")
            logAction("APPROVE", "VENDOR", vendorId, "PENDING", "APPROVED")
            _adminSuccess.value = "Vendor partner approved"
        }
    }

    fun adminRejectVendor(vendorId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateVendorStatus(vendorId, "REJECTED")
            logAction("REJECT", "VENDOR", vendorId, "PENDING", "REJECTED")
            _adminSuccess.value = "Vendor partner application rejected"
        }
    }

    fun adminSuspendVendor(vendorId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateVendorStatus(vendorId, "SUSPENDED")
            logAction("SUSPEND", "VENDOR", vendorId, "APPROVED", "SUSPENDED")
            _adminSuccess.value = "Vendor partner suspended"
        }
    }

    fun adminReactivateVendor(vendorId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateVendorStatus(vendorId, "APPROVED")
            logAction("REACTIVATE", "VENDOR", vendorId, "SUSPENDED", "APPROVED")
            _adminSuccess.value = "Vendor partner reinstated"
        }
    }

    fun adminEditVendorState(vendorId: Int, state: String) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateVendorState(vendorId, state)
            logAction("EDIT_LOCATION", "VENDOR", vendorId, "PREVIOUS", state)
            _adminSuccess.value = "Vendor directory updated"
        }
    }

    fun adminDeleteVendor(vendorId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.deleteVendor(vendorId)
            logAction("DELETE", "VENDOR", vendorId)
            _adminSuccess.value = "Vendor deleted"
        }
    }

    // PRODUCT/LISTING MODERATION
    fun adminApproveListing(listingId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateListingStatus(listingId, "APPROVED")
            logAction("APPROVE", "LISTING", listingId, "PENDING", "APPROVED")
            _adminSuccess.value = "Listing published safely to marketplace"
        }
    }

    fun adminRejectListing(listingId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateListingStatus(listingId, "REJECTED")
            logAction("REJECT", "LISTING", listingId, "PENDING", "REJECTED")
            _adminSuccess.value = "Listing submission rejected"
        }
    }

    fun adminSuspendListing(listingId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateListingStatus(listingId, "SUSPENDED")
            logAction("SUSPEND", "LISTING", listingId, "APPROVED", "SUSPENDED")
            _adminSuccess.value = "Listing soft-hidden from feed"
        }
    }

    fun adminReactivateListing(listingId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateListingStatus(listingId, "APPROVED")
            logAction("REACTIVATE", "LISTING", listingId, "SUSPENDED", "APPROVED")
            _adminSuccess.value = "Listing reinstated to feed"
        }
    }

    fun adminDeleteListing(listingId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.deleteListing(listingId)
            logAction("DELETE", "LISTING", listingId)
            _adminSuccess.value = "Listing dropped from system"
        }
    }

    fun adminMarkListingSold(listingId: Int, isSold: Boolean) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateListingSold(listingId, isSold)
            logAction("MARK_SOLD", "LISTING", listingId, (!isSold).toString(), isSold.toString())
            _adminSuccess.value = if (isSold) "Listing marked as SOLD" else "Listing marked as AVAILABLE"
        }
    }

    fun adminFeatureListing(listingId: Int, isFeatured: Boolean) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateListingFeatured(listingId, isFeatured)
            logAction(if (isFeatured) "FEATURE" else "UNFEATURE", "LISTING", listingId, (!isFeatured).toString(), isFeatured.toString())
            _adminSuccess.value = if (isFeatured) "Selected listing pins on premium highlights" else "Highlight tag cleared"
        }
    }

    // USER REPORTS ACTION
    fun adminUpdateReportState(reportId: Int, status: String) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateReportStatus(reportId, status)
            logAction("CHANGE_STATUS", "REPORT", reportId, "UNKNOWN", status)
            _adminSuccess.value = "Report marked as $status"
        }
    }

    fun adminDeleteReport(reportId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.deleteReport(reportId)
            logAction("DELETE", "REPORT", reportId)
            _adminSuccess.value = "Report removed"
        }
    }

    // SUPPORT TICKETS
    fun adminUpdateSupportStatus(messageId: Int, status: String) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.updateMessageStatus(messageId, status)
            logAction("CHANGE_STATUS", "SUPPORT", messageId, "UNKNOWN", status)
            _adminSuccess.value = "Inquiry ticket marked $status"
        }
    }

    fun adminDeleteSupport(messageId: Int) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            repository.deleteMessage(messageId)
            logAction("DELETE", "SUPPORT", messageId)
            _adminSuccess.value = "Ticket deleted"
        }
    }

    // APP SETTINGS ADMINISTRATION
    fun adminSaveSettings(
        priceDisclaimer: String,
        supportedStates: String,
        supportedCategories: String,
        marketplaceRules: String,
        shopApprovalMessage: String,
        vendorApprovalMessage: String,
        contactEmail: String,
        whatsAppNumber: String
    ) {
        viewModelScope.launch {
            verifyAdminOrThrow()
            val entity = AppSettingEntity(
                id = 1,
                priceDisclaimer = priceDisclaimer,
                supportedStates = supportedStates,
                supportedCategories = supportedCategories,
                marketplaceRules = marketplaceRules,
                shopApprovalMessage = shopApprovalMessage,
                vendorApprovalMessage = vendorApprovalMessage,
                contactEmail = contactEmail,
                whatsAppNumber = whatsAppNumber
            )
            repository.insertSettings(entity)
            logAction("UPDATE_SETTINGS", "SETTINGS", 1, "PREVIOUS", "NEW_SETTINGS_RECORD")
            _adminSuccess.value = "Global configuration parameters updated"
        }
    }

    // =========================================================================
    // EXPORT UTILITIES: Extends administrative abilities with raw CSV exports
    // =========================================================================
    fun getCsvExportData(targetType: String): String {
        verifyAdminOrThrow()
        val sb = StringBuilder()
        when (targetType.uppercase()) {
            "USERS" -> {
                sb.append("ID,FullName,Email,Phone,State,City,Role,Status\n")
                allUsers.value.forEach { u ->
                    sb.append("${u.id},\"${u.fullName.replace("\"", "\"\"")}\",\"${u.email}\",\"${u.phoneNumber}\",\"${u.state}\",\"${u.city}\",\"${u.role}\",\"${u.status}\"\n")
                }
            }
            "SHOPS" -> {
                sb.append("ID,ShopName,Owner,Phone,WhatsApp,Email,State,City,Address,Status,Verified\n")
                allShops.value.forEach { s ->
                    sb.append("${s.id},\"${s.shopName.replace("\"", "\"\"")}\",\"${s.ownerName.replace("\"", "\"\"")}\",\"${s.phone}\",\"${s.whatsApp}\",\"${s.email}\",\"${s.state}\",\"${s.city}\",\"${s.address.replace("\"", "\"\"")}\",\"${s.status}\",${s.isVerified}\n")
                }
            }
            "VENDORS" -> {
                sb.append("ID,VendorName,Owner,Phone,WhatsApp,Email,State,City,Address,Status\n")
                allVendors.value.forEach { v ->
                    sb.append("${v.id},\"${v.vendorName.replace("\"", "\"\"")}\",\"${v.ownerName.replace("\"", "\"\"")}\",\"${v.phone}\",\"${v.whatsApp}\",\"${v.email}\",\"${v.state}\",\"${v.city}\",\"${v.address.replace("\"", "\"\"")}\",\"${v.status}\"\n")
                }
            }
            "LISTINGS" -> {
                sb.append("ID,Poster,GadgetName,Brand,Model,Category,Condition,Price,State,City,Status,Featured,Sold\n")
                allListings.value.forEach { l ->
                    sb.append("${l.id},\"${l.posterName.replace("\"", "\"\"")}\",\"${l.gadgetName.replace("\"", "\"\"")}\",\"${l.brand}\",\"${l.model}\",\"${l.category}\",\"${l.condition}\",${l.price},\"${l.state}\",\"${l.city}\",\"${l.status}\",${l.isFeatured},${l.isSold}\n")
                }
            }
            "REPORTS" -> {
                sb.append("ID,Reporter,TargetType,TargetId,TargetName,Reason,Details,Status,Timestamp\n")
                allReports.value.forEach { r ->
                    sb.append("${r.id},\"${r.reporterName.replace("\"", "\"\"")}\",\"${r.targetType}\",${r.targetId},\"${r.targetName.replace("\"", "\"\"")}\",\"${r.reason}\",\"${r.details.replace("\"", "\"\"")}\",\"${r.status}\",${r.timestamp}\n")
                }
            }
        }
        return sb.toString()
    }

    fun reportListing(listingId: Int, reason: String) {
        val user = _currentUser.value
        viewModelScope.launch {
            repository.insertReport(
                ReportEntity(
                    userId = user?.id ?: 0,
                    listingId = listingId,
                    reporterName = user?.fullName ?: "Anonymous Guest",
                    reporterEmail = user?.email ?: "guest@gadgetvaluer.ng",
                    targetType = "LISTING",
                    targetId = listingId,
                    targetName = "Listing #$listingId",
                    reason = reason,
                    details = "Reported from listing screen.",
                    status = "PENDING"
                )
            )
        }
    }

    fun adminEditShopState(shopId: Int, state: String) {
        adminEditShopLocation(shopId, state, "Unknown")
    }

    fun adminResolveReport(reportId: Int) {
        adminUpdateReportState(reportId, "RESOLVED")
    }
}
