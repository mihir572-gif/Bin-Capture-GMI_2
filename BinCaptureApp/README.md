
# BinCapture (Android)

An Android app for capturing **Bin Location** via barcode scan, **Item** via UPC scan or search (from a CSV item master), **Quantity**, and **Expiration Date**. Stores data **locally** (offline) and exports to **CSV**.

## Features
- Scan **Bin Location** and **UPC** (ZXing embedded)
- Search/select items from your **CSV item master** (columns: `Item Code`, `Barcode`, `Description`, `External ID`, `Brand`)
- Enter **Qty** and **Expiration Date** (calendar)
- **Offline** storage using Room (SQLite)
- **CSV Export** via Android's Storage Access Framework
- Simple **multi-user** (user label per device)

## How to Build *without Android Studio*
Use **GitHub Actions** to produce the APK automatically.

### Steps
1. Create a new **GitHub repository** and upload the contents of this project.
2. Make sure the directory structure stays intact (the `app/` folder, `build.gradle`, etc.).
3. Commit and push.
4. GitHub Actions workflow (already included under `.github/workflows/android.yml`) will build `app-debug.apk` and attach it as a build artifact.

### Download the APK
- Go to your GitHub repo → **Actions** tab → open the latest workflow run → **Artifacts** → download `BinCaptureApp-debug-apk`.
- Transfer the APK to your Android device and install.

### First Run
- Tap **Import Items** → select your CSV file (from device storage or cloud).
- Tap **Scan Bin** to capture the bin location.
- Scan UPC or search to pick an item.
- Enter **Qty** and pick **Expiration Date**.
- Tap **Save**.
- Tap **Export CSV** to generate and share a CSV file.

## CSV Columns (Header names supported)
- `Item Code` or `ItemCode`
- `Barcode` or `UPC`
- `Description`
- `External ID` or `ExternalID`
- `Brand` or `Bran`

## Notes
- No Internet required for usage; works offline. Multiple users can install and use on their own devices.
- Export uses the system **Share** flow via Storage Access Framework; no special permissions required.

## Build Locally (optional, without Android Studio)
If you have a machine with **Java 17** installed, you can build from the command line:

```bash
# Install Gradle (once), then inside repo root
gradle wrapper          # generates ./gradlew
./gradlew assembleDebug # builds APK at app/build/outputs/apk/debug/app-debug.apk
```

You can install Java via SDKMAN or your OS package manager.

## License
MIT
