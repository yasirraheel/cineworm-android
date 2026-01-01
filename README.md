# 🎬 Cineworm Android

<div align="center">

![Cineworm](https://img.shields.io/badge/Platform-Android-green.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)

A modern Android video streaming application for movies, TV shows, sports, and live TV content.

</div>

## ✨ Features

### 🎥 Content
- **Movies** - Browse and stream a vast collection of movies
- **TV Shows** - Watch your favorite series with episode tracking
- **Live Sports** - Stream live sporting events
- **Live TV** - Access live television channels

### 👤 User Features
- User authentication (Email, Google, Facebook)
- Personalized watchlist
- Continue watching functionality
- User profiles and account management
- Subscription plans and payment integration

### 📱 Player Features
- ExoPlayer integration for smooth playback
- Chromecast support
- Multiple subtitle options
- Quality settings
- Full-screen mode
- Picture-in-Picture support

### 💳 Payment Integration
- Multiple payment gateways:
  - PayPal
  - Stripe
  - Razorpay
  - PayTM
  - PayU
  - Cashfree
  - Flutterwave
  - Paystack
  - Instamojo
  - SSLCommerz
  - Mollie
  - CoinGate
  - Bank Transfer

### 🎨 UI/UX
- Modern Material Design
- Dark theme support
- Smooth animations
- Responsive layouts for tablets
- RTL language support

## 🛠️ Tech Stack

- **Language**: Java
- **Minimum SDK**: 21 (Android 5.0 Lollipop)
- **Architecture**: MVC
- **Video Player**: ExoPlayer
- **Networking**: Retrofit, OkHttp
- **Image Loading**: Glide
- **Authentication**: Firebase Auth
- **Push Notifications**: OneSignal
- **Analytics**: Google Analytics
- **Chromecast**: Google Cast SDK

## 📋 Prerequisites

- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK API 21+
- Google Services JSON file
- OneSignal App ID

## 🚀 Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/yasirraheel/cineworm-android.git
cd cineworm-android
```

### 2. Configure Google Services
- Add your `google-services.json` file to the `app/` directory
- Update Firebase configuration as needed

### 3. Configure API Keys
Create a `local.properties` file in the root directory and add:
```properties
# JFrog credentials (for Braintree SDK)
jfrog.username=your_username
jfrog.password=your_password

# Other API keys
onesignal.app_id=your_onesignal_app_id
```

### 4. Update API Endpoints
- Open `app/src/main/java/com/cineworm/util/API.java`
- Update the base URL to point to your backend API

### 5. Build and Run
```bash
./gradlew clean build
```

## 📁 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/cineworm/
│   │   │   ├── adapter/          # RecyclerView adapters
│   │   │   ├── cast/              # Chromecast integration
│   │   │   ├── fragment/          # App fragments
│   │   │   ├── item/              # Data models
│   │   │   ├── util/              # Utility classes
│   │   │   └── videostreamingapp/ # Activities
│   │   ├── res/                   # Resources (layouts, drawables, etc.)
│   │   └── AndroidManifest.xml
│   └── androidTest/               # Instrumented tests
└── build.gradle
```

## 🎨 Screenshots

| Home | Movie Details | Player |
|------|---------------|--------|
| Add screenshot | Add screenshot | Add screenshot |

| TV Shows | Live TV | Profile |
|----------|---------|---------|
| Add screenshot | Add screenshot | Add screenshot |

## 🔐 Security

- Never commit sensitive API keys or credentials
- Use environment variables or `local.properties` for secrets
- Keep `google-services.json` secure and never expose API keys publicly

## 📄 Configuration Files

### Important Files to Configure
1. **google-services.json** - Firebase configuration
2. **local.properties** - Local API keys and credentials
3. **API.java** - Backend API endpoints
4. **Constant.java** - App constants and configuration

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- ExoPlayer for video playback
- Google Cast SDK for Chromecast support
- All payment gateway providers
- Firebase for authentication and services
- OneSignal for push notifications

## 📧 Contact

For any queries or support, please reach out:
- GitHub: [@yasirraheel](https://github.com/yasirraheel)

## 🔄 Version History

- **1.0.0** - Initial release

---

<div align="center">
Made with ❤️ by the Cineworm Team
</div>
