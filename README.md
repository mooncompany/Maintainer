# Maintainer - Vehicle Maintenance Tracking App

A comprehensive Android application for tracking vehicle maintenance with local SQLite storage and cloud backup capabilities.

## Features

### Core Functionality
- **Vehicle Management**: Add, edit, and manage multiple vehicles
- **VIN Scanning**: OCR-powered VIN scanning with NHTSA API integration for auto-population
- **Maintenance Logging**: Track maintenance services with photo receipt capture
- **OCR Receipt Processing**: Extract text from receipts using ML Kit
- **Cloud Backup**: Automatic backup to Google Drive
- **Maintenance Scheduling**: Basic maintenance scheduling with customizable intervals

### Technical Features
- **Modern Android Architecture**: MVVM pattern with Repository
- **Local Database**: Room database for offline functionality
- **Material Design 3**: Clean, modern UI with dark mode support
- **Dependency Injection**: Hilt for clean architecture
- **Camera Integration**: CameraX for photo capture
- **ML Kit Integration**: Text recognition and barcode scanning

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite wrapper)
- **DI**: Hilt
- **UI**: Jetpack Compose with Material Design 3
- **Camera**: CameraX
- **OCR**: ML Kit Text Recognition
- **VIN Scanning**: ML Kit Barcode Scanning
- **Cloud Storage**: Google Drive API
- **Networking**: Retrofit with OkHttp
- **Image Loading**: Coil
- **Background Tasks**: WorkManager

## Project Structure

```
app/src/main/java/com/maintainer/app/
├── data/
│   ├── database/
│   │   ├── entity/          # Room entities
│   │   ├── dao/             # Data Access Objects
│   │   └── converter/       # Type converters
│   ├── repository/          # Repository implementations
│   ├── remote/              # API services
│   └── backup/              # Cloud backup services
├── ui/
│   ├── vehicles/            # Vehicle management screens
│   ├── maintenance/         # Maintenance logging screens
│   ├── home/                # Home screen
│   ├── navigation/          # Navigation setup
│   └── theme/               # Material Design 3 theming
├── utils/                   # Utility classes
├── di/                      # Dependency injection modules
└── MaintainerApplication.kt # Application class
```

## Database Schema

### Vehicles Table
- Vehicle information (VIN, make, model, year, etc.)
- Current mileage tracking
- Custom vehicle nicknames

### Maintenance Records Table
- Service details and descriptions
- Cost tracking
- Photo receipt storage
- OCR text extraction

### Maintenance Schedules Table
- Customizable maintenance intervals
- Mileage and time-based scheduling
- Service due date calculations

## API Integrations

### NHTSA VIN Decoder API
- **Endpoint**: `https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVin/{vin}?format=json`
- **Purpose**: Auto-populate vehicle specifications from VIN
- **Free**: Government API with no rate limits

### Google Drive API
- **Purpose**: Cloud backup and restore functionality
- **Scopes**: `DRIVE_FILE`, `DRIVE_APPDATA`
- **Authentication**: Google Sign-In with OAuth2

## Setup Instructions

### Prerequisites
1. Android Studio Arctic Fox or newer
2. Android SDK API 24+ (Android 7.0+)
3. Google Play Services for ML Kit

### Configuration

1. **Clone the repository**
   ```bash
   git clone [repository-url]
   cd Maintainer
   ```

2. **Google Services Setup**
   - Create a project in [Google Cloud Console](https://console.cloud.google.com/)
   - Enable Google Drive API
   - Create OAuth 2.0 credentials for Android
   - Download `google-services.json` and place in `app/` directory

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

### Required Permissions
- `CAMERA`: For VIN scanning and receipt capture
- `INTERNET`: For NHTSA API and Google Drive
- `ACCESS_NETWORK_STATE`: Network connectivity checks
- `GET_ACCOUNTS`: Google Drive authentication
- `POST_NOTIFICATIONS`: Maintenance reminders

## Usage

### Adding a Vehicle
1. Tap the "+" button on the home screen
2. Either manually enter vehicle details or scan VIN
3. VIN scanning auto-populates make, model, year from NHTSA database
4. Save vehicle to local database

### Logging Maintenance
1. Select a vehicle from the home screen
2. Tap "+" to add new maintenance record
3. Select maintenance type and enter details
4. Optionally capture receipt photo for OCR processing
5. Save record with automatic schedule updates

### Cloud Backup
- Automatic backup to Google Drive after each change
- Manual backup/restore options in settings
- Encrypted local storage with cloud sync

## Development

### Architecture Patterns
- **MVVM**: Separation of concerns with ViewModels
- **Repository Pattern**: Data layer abstraction
- **Dependency Injection**: Hilt for testability
- **Reactive Programming**: Flow for data streams

### Testing
```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest
```

### Code Style
- Kotlin coding conventions
- Material Design 3 guidelines
- Clean Architecture principles

## Future Enhancements

### Planned Features
- **Advanced Analytics**: Cost trends and predictions
- **Maintenance Reminders**: Push notifications
- **Multiple Cloud Providers**: Dropbox, OneDrive support
- **Export Options**: PDF reports, CSV exports
- **Vehicle History**: Integration with service records
- **Parts Inventory**: Track replacement parts

### Technical Improvements
- **Offline Sync**: Robust offline-first architecture
- **Image Compression**: Optimize photo storage
- **Enhanced OCR**: Better receipt text extraction
- **Batch Operations**: Bulk maintenance entry
- **Data Validation**: Improved input validation

## Contributing

1. Fork the repository
2. Create a feature branch
3. Follow coding standards
4. Add tests for new features
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and feature requests, please use the GitHub issue tracker.

## Privacy

- All data stored locally using Room database
- Cloud backup optional and user-controlled
- No analytics or tracking without consent
- Secure authentication with Google OAuth2