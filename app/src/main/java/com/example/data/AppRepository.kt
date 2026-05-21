package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val db: AppDatabase) {
    val userDao = db.userDao()
    val shopDao = db.shopDao()
    val vendorDao = db.vendorDao()
    val gadgetScanDao = db.gadgetScanDao()
    val gadgetListingDao = db.gadgetListingDao()
    val savedGadgetDao = db.savedGadgetDao()
    val reportDao = db.reportDao()
    val supportMessageDao = db.supportMessageDao()
    val appSettingDao = db.appSettingDao()
    val adminActionLogDao = db.adminActionLogDao()

    // Users
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
    suspend fun getUserById(id: Int): UserEntity? = userDao.getUserById(id)
    suspend fun updateUserStatus(id: Int, status: String) = userDao.updateUserStatus(id, status)
    suspend fun updateUserRole(id: Int, role: String) = userDao.updateUserRole(id, role)
    suspend fun deleteUser(id: Int) = userDao.deleteUser(id)

    // Shops
    val allShops: Flow<List<ShopEntity>> = shopDao.getAllShops()
    val approvedShops: Flow<List<ShopEntity>> = shopDao.getApprovedShops()
    suspend fun insertShop(shop: ShopEntity): Long = shopDao.insertShop(shop)
    suspend fun updateShopStatus(id: Int, status: String) = shopDao.updateShopStatus(id, status)
    suspend fun updateShopState(id: Int, state: String) = shopDao.updateShopState(id, state)
    suspend fun updateShopCity(id: Int, city: String) = shopDao.updateShopCity(id, city)
    suspend fun updateShopVerification(id: Int, isVerified: Boolean) = shopDao.updateShopVerification(id, isVerified)
    suspend fun deleteShop(id: Int) = shopDao.deleteShop(id)

    // Vendors
    val allVendors: Flow<List<VendorEntity>> = vendorDao.getAllVendors()
    val approvedVendors: Flow<List<VendorEntity>> = vendorDao.getApprovedVendors()
    suspend fun insertVendor(vendor: VendorEntity): Long = vendorDao.insertVendor(vendor)
    suspend fun updateVendorStatus(id: Int, status: String) = vendorDao.updateVendorStatus(id, status)
    suspend fun updateVendorState(id: Int, state: String) = vendorDao.updateVendorState(id, state)
    suspend fun deleteVendor(id: Int) = vendorDao.deleteVendor(id)

    // Scans
    val allScans: Flow<List<GadgetScanEntity>> = gadgetScanDao.getAllScans()
    suspend fun insertScan(scan: GadgetScanEntity): Long = gadgetScanDao.insertScan(scan)
    suspend fun getScanById(id: Int): GadgetScanEntity? = gadgetScanDao.getScanById(id)
    fun getScansForUser(userId: Int): Flow<List<GadgetScanEntity>> = gadgetScanDao.getScansForUser(userId)

    // Listings
    val allListings: Flow<List<GadgetListingEntity>> = gadgetListingDao.getAllListings()
    val approvedListings: Flow<List<GadgetListingEntity>> = gadgetListingDao.getApprovedListings()
    suspend fun insertListing(listing: GadgetListingEntity): Long = gadgetListingDao.insertListing(listing)
    suspend fun updateListingStatus(id: Int, status: String) = gadgetListingDao.updateListingStatus(id, status)
    suspend fun updateListingFeatured(id: Int, isFeatured: Boolean) = gadgetListingDao.updateListingFeatured(id, isFeatured)
    suspend fun updateListingSold(id: Int, isSold: Boolean) = gadgetListingDao.updateListingSold(id, isSold)
    suspend fun deleteListing(id: Int) = gadgetListingDao.deleteListing(id)

    // Saved/Bookmarked
    suspend fun insertSaved(saved: SavedGadgetEntity): Long = savedGadgetDao.insertSaved(saved)
    suspend fun deleteSaved(userId: Int, scanId: Int) = savedGadgetDao.deleteSaved(userId, scanId)
    fun getSavedForUser(userId: Int): Flow<List<SavedGadgetEntity>> = savedGadgetDao.getSavedForUser(userId)

    // Reports
    val allReports: Flow<List<ReportEntity>> = reportDao.getAllReports()
    suspend fun insertReport(report: ReportEntity): Long = reportDao.insertReport(report)
    suspend fun updateReportStatus(id: Int, status: String) = reportDao.updateReportStatus(id, status)
    suspend fun deleteReport(id: Int) = reportDao.deleteReport(id)

    // Support Messages
    val allMessages: Flow<List<SupportMessageEntity>> = supportMessageDao.getAllMessages()
    suspend fun insertMessage(msg: SupportMessageEntity): Long = supportMessageDao.insertMessage(msg)
    suspend fun updateMessageStatus(id: Int, status: String) = supportMessageDao.updateMessageStatus(id, status)
    suspend fun deleteMessage(id: Int) = supportMessageDao.deleteMessage(id)

    // Settings
    suspend fun insertSettings(settings: AppSettingEntity) = appSettingDao.insertSettings(settings)
    suspend fun getSettings(): AppSettingEntity? = appSettingDao.getSettings()
    fun getSettingsFlow(): Flow<AppSettingEntity?> = appSettingDao.getSettingsFlow()

    // Logs
    val allLogs: Flow<List<AdminActionLogEntity>> = adminActionLogDao.getAllLogs()
    suspend fun insertLog(log: AdminActionLogEntity): Long = adminActionLogDao.insertLog(log)

    // Database Seeder
    suspend fun seedIfNeeded() {
        val existingUsers = userDao.getAllUsers().firstOrNull() ?: emptyList()
        if (existingUsers.isEmpty()) {
            // Seed Admin User
            userDao.insertUser(
                UserEntity(
                    fullName = "NG Admin",
                    email = "kheenganaz@gmail.com",
                    phoneNumber = "+2348012345678",
                    password = "adminpassword123",
                    state = "FCT Abuja",
                    city = "Garki",
                    role = "ADMIN",
                    status = "ACTIVE"
                )
            )

            // Seed normal User
            val uid = userDao.insertUser(
                UserEntity(
                    fullName = "Chidi Okafor",
                    email = "chidi@gmail.com",
                    phoneNumber = "+2348123456789",
                    password = "userpassword123",
                    state = "Lagos",
                    city = "Ikeja",
                    role = "USER",
                    status = "ACTIVE"
                )
            ).toInt()

            // Seed approved Shops
            shopDao.insertShop(
                ShopEntity(
                    shopName = "Niger Tech Hub",
                    ownerName = "Musa Ibrahim",
                    phone = "+2347012345678",
                    whatsApp = "+2347012345678",
                    email = "nigertech@gmail.com",
                    state = "Niger",
                    city = "Minna",
                    address = "Plot 12, Bosso Road, Minna",
                    categoriesSold = "Phones;Laptops;Accessories",
                    shopLogo = "default_logo",
                    verificationDocument = "CAC_992831.pdf",
                    status = "APPROVED",
                    isVerified = true
                )
            )

            shopDao.insertShop(
                ShopEntity(
                    shopName = "Lagos Gadget Hub",
                    ownerName = "Tunde Johnson",
                    phone = "+2348022345678",
                    whatsApp = "+2348022345678",
                    email = "lagoshub@gmail.com",
                    state = "Lagos",
                    city = "Ikeja",
                    address = "Computer Village, Ikeja",
                    categoriesSold = "Phones;Tablets;Macbooks",
                    shopLogo = "default_logo",
                    verificationDocument = "CAC_102938.pdf",
                    status = "APPROVED",
                    isVerified = true
                )
            )

            // Seed raw/pending shop
            shopDao.insertShop(
                ShopEntity(
                    shopName = "Sokoto Gadget Kings",
                    ownerName = "Usman Danfodio",
                    phone = "+2348043229988",
                    whatsApp = "+2348043229988",
                    email = "sokoto@gmail.com",
                    state = "Sokoto",
                    city = "Sokoto",
                    address = "Ahmadu Bello Way",
                    categoriesSold = "Phones;Accessories",
                    shopLogo = "default_logo",
                    verificationDocument = "DOC_SOS_111.pdf",
                    status = "PENDING",
                    isVerified = false
                )
            )

            // Seed approved Vendors
            vendorDao.insertVendor(
                VendorEntity(
                    vendorName = "Emeka iPhone Plug",
                    ownerName = "Emeka Nnamani",
                    phone = "+2348055667788",
                    whatsApp = "+2348055667788",
                    email = "emeka.plug@gmail.com",
                    state = "Lagos",
                    city = "Surulere",
                    address = "45 Adeniran Ogunsanya, Surulere",
                    categoriesSold = "Phones;Apple Devices",
                    vendorLogo = "default_logo",
                    verificationDocument = "ID_CARD_EMEKA.pdf",
                    status = "APPROVED"
                )
            )

            // Seed pending vendor
            vendorDao.insertVendor(
                VendorEntity(
                    vendorName = "Chinedu Laptop Guru",
                    ownerName = "Chinedu Obi",
                    phone = "+2348077551234",
                    whatsApp = "+2348077551234",
                    email = "chinedu@gmail.com",
                    state = "Anambra",
                    city = "Onitsha",
                    address = "Main Market Onitsha",
                    categoriesSold = "Laptops",
                    vendorLogo = "default_logo",
                    verificationDocument = "CAC_CHINEDU.pdf",
                    status = "PENDING"
                )
            )

            // Seed Approved Listings
            val list1 = gadgetListingDao.insertListing(
                GadgetListingEntity(
                    userId = uid,
                    posterName = "Lagos Gadget Hub",
                    gadgetName = "iPhone 13 Pro Max 256GB",
                    brand = "Apple",
                    model = "iPhone 13 Pro Max",
                    category = "Phones",
                    condition = "London Used",
                    price = 540000.0,
                    state = "Lagos",
                    city = "Ikeja",
                    imageUri = null,
                    description = "London Used iPhone 13 Pro Max, Sierra Blue, 88% battery health. 100% functional with warranty.",
                    contactPhone = "+2348022345678",
                    contactWhatsApp = "+2348022345678",
                    status = "APPROVED",
                    isFeatured = true
                )
            ).toInt()

            val list2 = gadgetListingDao.insertListing(
                GadgetListingEntity(
                    userId = uid,
                    posterName = "Niger Tech Hub",
                    gadgetName = "HP Pavilion 15 Core i5",
                    brand = "HP",
                    model = "Pavilion 15",
                    category = "Laptops",
                    condition = "London Used",
                    price = 320000.0,
                    state = "Niger",
                    city = "Minna",
                    imageUri = null,
                    description = "HP Pavilion 15, Intel Core i5 11th Gen, 16GB RAM, 512GB SSD. Pristine condition.",
                    contactPhone = "+2347012345678",
                    contactWhatsApp = "+2347012345678",
                    status = "APPROVED",
                    isFeatured = false
                )
            ).toInt()

            // Seed pending Listing
            val pendingList = gadgetListingDao.insertListing(
                GadgetListingEntity(
                    userId = uid,
                    posterName = "Chidi Okafor",
                    gadgetName = "Sony PlayStation 5 Slim",
                    brand = "Sony",
                    model = "PlayStation 5",
                    category = "Gaming Consoles",
                    condition = "Brand New",
                    price = 680000.0,
                    state = "Lagos",
                    city = "Ikeja",
                    imageUri = null,
                    description = "Splendid sealed brand new PS5 Slim. Includes 1 controller and 1 digital game code.",
                    contactPhone = "+2348123456789",
                    contactWhatsApp = "+2348123456789",
                    status = "PENDING"
                )
            ).toInt()

            // Seed reports
            reportDao.insertReport(
                ReportEntity(
                    userId = uid,
                    reporterName = "Chidi Okafor",
                    reporterEmail = "chidi@gmail.com",
                    targetType = "LISTING",
                    targetId = list2,
                    targetName = "HP Pavilion 15 Core i5",
                    reason = "Wrong gadget price",
                    details = "This price is too low for an 11th Gen i5 laptop. It's normally at least 400k Naira.",
                    status = "PENDING"
                )
            )

            reportDao.insertReport(
                ReportEntity(
                    userId = uid,
                    reporterName = "Chidi Okafor",
                    reporterEmail = "chidi@gmail.com",
                    targetType = "PRICE",
                    targetId = 0,
                    targetName = "iPhone 15 Pro Max valuation",
                    reason = "Wrong gadget identification",
                    details = "AI scanning identified my phone as a base iPhone 15 instead of iPhone 15 Pro Max.",
                    status = "PENDING"
                )
            )

            // Seed support messages
            supportMessageDao.insertMessage(
                SupportMessageEntity(
                    senderName = "Musa Ibrahim",
                    senderEmail = "nigertech@gmail.com",
                    subject = "Verification document updates",
                    message = "Hello Admin, let me know if my CAC tax certificate copy is needed for my shop verification. Thanks.",
                    status = "OPEN"
                )
            )

            supportMessageDao.insertMessage(
                SupportMessageEntity(
                    senderName = "Bisi Alabi",
                    senderEmail = "bisi@gmail.com",
                    subject = "Premium advertising packages",
                    message = "Can we pay to promote a featured gadget card to the top banner row across Nigeria?",
                    status = "IN_PROGRESS"
                )
            )

            // Seed default settings
            appSettingDao.insertSettings(
                AppSettingEntity(
                    id = 1,
                    priceDisclaimer = "Prices are estimates based on market patterns and may vary by condition, location, exchange rate and seller.",
                    supportedStates = "Abuja (FCT);Lagos;Niger;Rivers;Oyo;Kano;Kaduna;Delta;Enugu;Anambra;Edo;Kwara;Ogun;Ondo;Sokoto",
                    supportedCategories = "Phones;Laptops;Tablets;Gaming Consoles;Accessories",
                    marketplaceRules = "1. Only list authentic gadgets.\n2. Provide clear images.\n3. Include a valid voice call or WhatsApp number.\n4. Defrauding or scamming buyers will lead to legal action and permanent ban.",
                    shopApprovalMessage = "Congratulations! Your shop has been verified and approved on Gadget Valuer NG.",
                    vendorApprovalMessage = "Dear Partner, your application for verified vendor has been successfully approved.",
                    contactEmail = "support@gadgetvaluer.ng",
                    whatsAppNumber = "+2348000000000"
                )
            )
        }
    }
}
