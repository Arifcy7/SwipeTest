# ai-scribe-copilot

A production-grade Flutter app for real-time medical transcription, built for the Attack Capital Mobile Engineering Challenge.

## Features
- Real-time audio streaming with chunked upload
- Survives phone calls, app switching, network loss, and device restarts
- Native microphone, camera, notifications, and share sheet
- Android foreground service & iOS background audio
- Local queue for offline chunk storage and retry
- Bonus: On-device speech recognition, Material You, accessibility

## Quick Start

### 1. Flutter App
```sh
flutter pub get
flutter run
```

#### Android APK (Release)
```sh
flutter build apk --release
```
APK will be available in `build/app/outputs/flutter-apk/app-release.apk`.

#### iOS
```sh
flutter build ios --simulator
```

### 2. Mock Backend
```sh
cd backend
docker-compose up
```
Backend runs at http://localhost:8080

## Demo
- 📱 **Android APK:** [GitHub Releases](#)
- 🎥 **iOS Loom Video:** [Loom Link](#)
- 📚 [API Documentation](https://docs.google.com/document/d/1hzfry0fg7qQQb39cswEychYMtBiBKDAqIg6LamAKENI/edit?usp=sharing)
- 🔧 [Postman Collection](https://drive.google.com/file/d/1rnEjRzH64ESlIi5VQekG525Dsf8IQZTP/view?usp=sharing)
- 🔗 **Backend Deployment:** http://localhost:8080

## Build Requirements
- Flutter 3.19+
- Node.js 20+ (for backend)
- Docker (for backend, optional)

## Pass/Fail Scenarios
- [x] Lock phone during recording: no data loss
- [x] Phone call interruption: auto-pause/resume
- [x] Airplane mode: chunks queue, upload on reconnect
- [x] Open camera: recording continues
- [x] Kill app: session recovers

## License
MIT
# doctor_manage

A new Flutter project.

## Getting Started

This project is a starting point for a Flutter application.

A few resources to get you started if this is your first Flutter project:

- [Lab: Write your first Flutter app](https://docs.flutter.dev/get-started/codelab)
- [Cookbook: Useful Flutter samples](https://docs.flutter.dev/cookbook)

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev/), which offers tutorials,
samples, guidance on mobile development, and a full API reference.
