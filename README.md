# HandyHub 🛠️

**HandyHub** is a mobile platform that connects customers with skilled service providers in Sri Lanka. Whether you need a plumber, electrician, carpenter, or any other handyman service, HandyHub makes it easy to find, book, and pay verified professionals.

## 📱 Overview

HandyHub serves as a two-way marketplace:
- **Customers** can browse, search, and book verified service providers
- **Workers** can create professional profiles, receive job requests, and get paid

## ✨ Features

### For Customers
- **Search & Discover** - Find workers by name, job title, or service category
- **Worker Profiles** - View ratings, experience, and pricing information
- **Easy Booking** - Book services with date, time, and location selection
- **Google Maps Integration** - Select your location using an interactive map
- **Secure Payments** - Pay workers through PayHere payment gateway
- **Track Hirings** - View and manage all your active bookings

### For Workers
- **Work Profile Creation** - Set up your professional profile with title, category, experience, and pricing
- **NIC Verification** - Submit identity documents for verification
- **Job Management** - Accept, complete, or cancel incoming job requests
- **Set Charges** - Set your pricing after job completion
- **Ratings & Reviews** - Build your reputation through customer ratings

### Security & Authentication
- **Mobile Number Verification** - OTP-based authentication
- **Password Encryption** - Secure password hashing using BCrypt
- **Worker Verification** - Admin verification of worker identity documents

## 🛠️ Technology Stack

| Technology | Purpose |
|------------|---------|
| **Java** | Primary programming language |
| **Android SDK** | Native Android development (API 24 - 35) |
| **Firebase Authentication** | User authentication |
| **Firebase Firestore** | Cloud database |
| **Firebase Storage** | File/image storage |
| **Google Maps API** | Location services and maps |
| **Google Places API** | Location search autocomplete |
| **PayHere SDK** | Payment processing |
| **Glide & Picasso** | Image loading |
| **View Binding** | Type-safe view access |

## 📂 Project Structure

```
app/
├── src/
│   └── main/
│       ├── java/lk/handyhub/handyhub/
│       │   ├── MainActivity.java          # Authentication screens
│       │   ├── HomeActivity.java           # Main app container
│       │   ├── SplashScreenActivity.java   # App launch screen
│       │   ├── models/                     # Data models & adapters
│       │   │   ├── User.java
│       │   │   ├── Worker.java
│       │   │   ├── Booking.java
│       │   │   ├── WorkerAdapter.java
│       │   │   ├── BookingAdapter.java
│       │   │   └── HireWorkerAdapter.java
│       │   └── ui/                         # UI fragments
│       │       ├── home/                   # Home & search screens
│       │       ├── profile/                # User & work profiles
│       │       ├── myhirings/              # Customer bookings
│       │       └── myjobs/                 # Worker job requests
│       └── res/                            # Resources (layouts, drawables, etc.)
├── build.gradle.kts                        # App-level dependencies
└── google-services.json                    # Firebase configuration
```

## 📋 Prerequisites

- **Android Studio** Arctic Fox or newer
- **JDK 11** or higher
- **Android SDK** API 24+
- **Firebase Project** with Firestore, Authentication, and Storage enabled
- **Google Cloud Platform** project with Maps and Places APIs enabled
- **PayHere Merchant Account** for payment integration

## 🚀 Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/tharidul/handyhub.git
   cd handyhub
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned repository

3. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app with package name `lk.handyhub.handyhub`
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Authentication (Phone), Firestore, and Storage in Firebase Console

4. **Configure Google Maps API**
   - Enable Maps SDK for Android and Places API in [Google Cloud Console](https://console.cloud.google.com/)
   - Add your API key in `app/src/main/res/values/strings.xml`:
     ```xml
     <string name="google_maps_key">YOUR_API_KEY_HERE</string>
     ```
   - Also update the API key in `AndroidManifest.xml`

5. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or use Android Studio's Run button

## ⚙️ Configuration

### Firebase Collections

The app uses the following Firestore collections:

| Collection | Purpose |
|------------|---------|
| `users` | User profiles and worker information |
| `bookings` | Service bookings between customers and workers |
| `work_categories` | Available service categories |

### Permissions

The app requires the following permissions:
- `INTERNET` - Network access
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` - Location services
- `CALL_PHONE` - Direct calling of workers
- `READ_EXTERNAL_STORAGE` - Image selection for NIC upload (API < 33, uses scoped storage for newer versions)

## 🔧 Build Configuration

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35
- **Java Version**: 11

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

HandyHub - [handyhub.lk](https://handyhub.lk)

---

<p align="center">Made with ❤️ in Sri Lanka 🇱🇰</p>
