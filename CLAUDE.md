# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

### Prerequisites
- **Java 11 or newer** required (Android Gradle Plugin 8.12.3 dependency)
- **IMPORTANT**: System may default to Java 8 - use `quick_build.bat` or set `JAVA_HOME=C:\Program Files\Android\Android Studio\jbr`
- Android SDK API 24+ (project targets API 34)
- Emulator or physical device with Google Play Services

### Standard Build Process

#### Windows (Command Prompt/PowerShell)
```cmd
# RECOMMENDED: Use the automated build script (sets JAVA_HOME correctly)
quick_build.bat

# Manual build (requires JAVA_HOME setup)
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
.\gradlew.bat clean assembleDebug

# Quick build without clean
.\gradlew.bat assembleDebug

# Run unit tests
.\gradlew.bat test

# Run instrumentation tests
.\gradlew.bat connectedAndroidTest

# Stop hanging Gradle daemons if builds freeze
.\gradlew.bat --stop
```

#### WSL/Linux (Note: gradlew script missing, use Windows commands)
```bash
# Use PowerShell from WSL to run Windows batch file
powershell.exe -Command "& './gradlew.bat' assembleDebug"

# Or run directly on Windows Command Prompt/PowerShell
```

### Common Build Issues
1. **Java Version Error**: "Dependency requires at least JVM runtime version 11"
   - **Solution**: Use Android Studio's bundled JDK (Java 21)
   - **Command**: `$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; & ".\gradlew.bat" assembleDebug`
   - Alternative: Install Java 11+ and set JAVA_HOME
   - Check with: `java -version`

2. **Missing gradlew script**: Only `gradlew.bat` exists
   - Solution: Use Windows Command Prompt or PowerShell instead of WSL/Git Bash

### ✅ **VERIFIED WORKING BUILD COMMAND** (September 2025)
```powershell
# Windows PowerShell - Uses Android Studio's bundled JDK (TESTED & VERIFIED)
cd "C:/Users/na8di/Desktop/PROJECTS/Maintainer"
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
& ".\gradlew.bat" assembleDebug
```
**Result**: BUILD SUCCESSFUL in 5s - APK created at `app/build/outputs/apk/debug/app-debug.apk`

### ✅ **ALTERNATIVE WORKING BUILD COMMAND** (From WSL/Bash)
```bash
# From WSL or Git Bash - Uses PowerShell to execute Windows build
cd "C:/Users/na8di/Desktop/PROJECTS/Maintainer"
powershell.exe -Command '$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; & ".\gradlew.bat" assembleDebug'
```
**Result**: BUILD SUCCESSFUL - Works from any terminal environment

### Development Scripts
```bash
# Custom quick build script (shows instructions only)
./quick_build.bat

# Project verification
python verify_project.py
```

### Android Studio Workflow
```bash
# Import project in Android Studio
# File > Open > Select project root directory
# Wait for Gradle sync (2-3 minutes first time)
# Click Run (▶️) or Shift+F10 to build and deploy

# Command line install to emulator
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.maintainer.app/.MainActivity
```

### Development Environment Setup
- **Java 11+ required** (not Java 8)
- Android SDK API 24+ required (project targets API 34)
- Emulator or physical device with Google Play Services for ML Kit functionality
- Requires `app/google-services.json` for Google Drive integration (often missing)

### Troubleshooting Build Issues

#### Recently Fixed Compilation Errors (September 2025)
1. **BuildConfig import error**: Resolved by removing BuildConfig dependency
2. **Variable declaration order**: Fixed cameraLauncher forward reference in AddEditMaintenanceScreen
3. **Duplicate imports**: Removed duplicate ContextCompat import in VinScannerActivity
4. **Java version compatibility**: Documented Java 11+ requirement
5. **Missing icon imports**: Fixed Tire icon reference in MaintenanceListScreen
6. **Non-existent enum values**: Updated maintenance type mappings to use correct enum values
7. **Missing shape imports**: Added CircleShape import in VinScannerScreen
8. **Deprecation warnings**: Updated to AutoMirrored ArrowBack icons

#### Current Known Issues
- Missing Unix `gradlew` script (only `gradlew.bat` for Windows)
- Google Drive backup service disabled (stub implementation)
- Some ML Kit features may fail without proper `google-services.json`

## Architecture Overview

### MVVM + Repository Pattern
The app follows strict MVVM architecture with clear separation of concerns:

```
UI Layer (Compose) → ViewModel (Hilt) → Repository → DAO/API → Database/Network
```

**Key architectural components:**
- `MaintainerApplication.kt` - Hilt entry point
- `MainActivity.kt` - Single activity with Compose navigation
- `MaintainerNavHost.kt` - Centralized navigation routing with typed arguments
- `MaintainerDatabase.kt` - Room database singleton with destructive migration fallback

### Database Schema (Room)
Three main entities with proper foreign key relationships:

**Vehicle Entity** (`data/database/entity/Vehicle.kt`):
- Primary fields: id, vin, make, model, year, currentMileage
- Metadata: createdAt, updatedAt, isActive
- Optional: color, licensePlate, nickname, engineSize, fuelType, transmission

**MaintenanceRecord Entity** (`data/database/entity/MaintenanceRecord.kt`):
- Links to Vehicle via foreign key with CASCADE delete
- Core fields: type (enum), description, cost, mileage, serviceDate
- OCR support: receiptImagePath, ocrText
- Maintenance types: OIL_CHANGE, FILTER_CHANGE, BRAKE_SERVICE, etc.

**MaintenanceSchedule Entity**:
- Handles maintenance intervals and due date calculations

### Dependency Injection (Hilt)
All components use Hilt for dependency injection:

**DatabaseModule.kt**: Provides Room database and DAOs
**NetworkModule.kt**: Provides Retrofit services and API clients

Pattern for ViewModels:
```kotlin
@HiltViewModel
class SomeViewModel @Inject constructor(
    private val repository: SomeRepository
) : ViewModel()
```

### Navigation Structure
Navigation uses type-safe arguments via `MaintainerNavHost.kt`:

- `home` - Dashboard overview
- `vehicles` - Vehicle list
- `vehicles/add` - Add new vehicle
- `vehicles/edit/{vehicleId}` - Edit existing vehicle
- `maintenance/{vehicleId}` - Maintenance history for vehicle
- `maintenance/{vehicleId}/add` - Add maintenance record
- `maintenance/{vehicleId}/edit/{recordId}` - Edit maintenance record

## Key Feature Implementations

### VIN Scanning & Auto-Population
- Uses ML Kit barcode scanning for VIN capture
- Integrates with NHTSA API (`https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVin/{vin}?format=json`)
- Auto-populates vehicle make, model, year from government database
- No API key required for NHTSA service

### OCR Receipt Processing
- CameraX integration for receipt photo capture
- ML Kit Text Recognition for extracting text from receipts
- Stores both image path and extracted text in MaintenanceRecord
- Requires camera permissions at runtime

### Cloud Backup (Google Drive)
- `GoogleDriveBackupService.kt` handles backup/restore operations
- Uses Google Drive API with DRIVE_FILE and DRIVE_APPDATA scopes
- Requires OAuth 2.0 setup via Google Cloud Console
- Backs up database content as JSON format

## Code Patterns & Conventions

### Repository Pattern
All data access goes through repositories that combine local (Room) and remote (API) data sources:

```kotlin
class SomeRepository @Inject constructor(
    private val dao: SomeDao,
    private val apiService: SomeApiService
) {
    fun getData(): Flow<List<SomeEntity>> = dao.getAll()
    suspend fun syncData() { /* API + local DB logic */ }
}
```

### Compose Screen Pattern
UI screens follow consistent patterns with Hilt ViewModels:

```kotlin
@Composable
fun SomeScreen(
    viewModel: SomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    // Compose UI implementation
}
```

### Database Operations
- All DAO methods are `suspend` functions returning `Flow` for reactive UI
- Uses `fallbackToDestructiveMigration()` - no proper migration strategy implemented
- Foreign key constraints enforce data integrity (CASCADE deletes)

## Testing Context

### Current Test Coverage (Minimal)
- `ExampleUnitTest.kt` - Basic unit test template
- `VehicleTest.kt` - Vehicle entity validation tests
- Instrumentation test structure exists but mostly empty

### Testing Opportunities
The codebase lacks comprehensive testing. Priority areas:
- ViewModel business logic testing
- Repository integration tests
- Database migration testing (when implemented)
- API integration tests (NHTSA, Google Drive)
- Compose UI testing

## Common Development Tasks

### Adding New Maintenance Types
1. Update `MaintenanceType` enum in `MaintenanceRecord.kt`
2. Update UI dropdowns in maintenance screens
3. Add any type-specific validation logic

### Extending Vehicle Properties
1. Add fields to `Vehicle` entity
2. Create Room migration (currently using destructive migration)
3. Update vehicle forms and validation
4. Update repository and DAO methods

### Adding New Screens
1. Create Compose screen in appropriate `ui/` subdirectory
2. Add ViewModel with Hilt injection
3. Update `MaintainerNavHost.kt` with new route
4. Add navigation calls from existing screens

## External Dependencies & APIs

### ML Kit (On-device processing)
- Text Recognition: `com.google.mlkit:text-recognition:16.0.0`
- Barcode Scanning: `com.google.mlkit:barcode-scanning:17.2.0`
- Requires Google Play Services on device/emulator
- No API keys needed - all processing is on-device

### Google Services Integration
- Requires `app/google-services.json` configuration file
- Google Drive API needs OAuth 2.0 setup in Google Cloud Console
- SHA-1 fingerprints must be configured for authentication

### NHTSA VIN Decoder
- Free government API with no rate limits
- Returns vehicle specifications in JSON format
- Endpoint: `https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVin/{vin}?format=json`

## Performance Considerations

### Database Optimization
- Consider adding indexes for frequently queried columns (vehicleId, serviceDate)
- Implement pagination for large maintenance history lists
- Use Flow for reactive data binding to avoid memory leaks

### Image Handling
- Receipt images stored as file paths, not in database
- Consider implementing image compression for storage efficiency
- Clean up temporary camera files to prevent storage bloat

### Background Processing
- Uses WorkManager for reliable background sync operations
- Implement exponential backoff for API failures
- Handle network connectivity changes gracefully

## Known Issues & Limitations

### Configuration Issues
1. **Missing google-services.json** - ML Kit and Google services require proper configuration
2. **Google Drive Backup Disabled** - Backup service requires Google Drive API setup
3. **Database Migration Strategy** - Currently uses destructive migration (data loss on updates)

### Development Opportunities
- Enhanced OCR parsing for receipt data extraction
- Maintenance scheduling intelligence and reminders
- Comprehensive automated test coverage