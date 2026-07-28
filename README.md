<div align="center">

# CivicSense Companion

> *Kotlin Multiplatform mobile companion for the CivicSense Edge AI perception pipeline.*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-purple.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Android-Compose-brightgreen)]()
[![Platform](https://img.shields.io/badge/iOS-SwiftUI-blue)]()
[![gRPC](https://img.shields.io/badge/gRPC-v1.71-red)]()

</div>

---

## What This Is

A **Kotlin Multiplatform** mobile app that displays real-time detection alerts from the [CivicSense](https://github.com/arpanpathak/driving-civicsense-vision-model) Rust perception pipeline. It connects via **gRPC** to receive intersection violations, lane camping warnings, stop sign alerts, and road hazard notifications — right on your phone.

### Architecture

```
┌─────────────────────┐     gRPC      ┌──────────────────────────────┐
│  Rust Pipeline      │◄────────────►│  KMP Companion App (this)    │
│  (edge device)      │   :50051     │                              │
│                     │              │  ┌────────────────────────┐  │
│  YOLOv8/v11 ONNX    │  Subscribe   │  │ shared/ (KMP module)   │  │
│  Deep SORT Tracker  │─────────────►│  │  Domain Models         │  │
│  Alert Engine       │   stream     │  │  Service Interface     │  │
│  gRPC Server (tonic)│              │  │  CivicSenseViewModel   │  │
└─────────────────────┘              │  └──────┬─────────┬───────┘  │
                                      │         │         │         │
                                      │  ┌──────┘         └──────┐  │
                                      │  ▼                      ▼  │
                                      │  Android (Compose)    iOS  │
                                      │  Dashboard + Alerts   Swift│
                                      └──────────────────────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Shared logic | Kotlin Multiplatform (Kotlin 2.1.20) |
| Android UI | Jetpack Compose + Material 3 |
| iOS UI | SwiftUI |
| Transport (Android) | gRPC + OkHttp |
| Transport (iOS) | Ktor HTTP client |
| Concurrency | Kotlin Coroutines + StateFlow |
| Build | Gradle 8.12 + Version Catalog |

## Project Structure

```
frontend/
├── proto/
│   └── civicsense.proto           # gRPC contract (Rust ↔ app)
├── shared/                        # KMP shared module
│   ├── src/commonMain/            # Models, service interface, ViewModel
│   ├── src/androidMain/           # gRPC Android implementation
│   └── src/iosMain/               # Ktor iOS implementation
├── androidApp/                    # Jetpack Compose app
│   └── src/main/kotlin/
│       ├── MainActivity.kt
│       └── ui/
│           ├── DashboardScreen.kt
│           └── AlertListScreen.kt
├── iosApp/iosApp/                 # SwiftUI app
│   ├── CivicSenseApp.swift
│   ├── ContentView.swift
│   ├── DashboardView.swift
│   └── AlertListView.swift
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```

## Getting Started

### Prerequisites

- **Android**: Android Studio Ladybug+, JDK 17
- **iOS**: Xcode 16+, CocoaPods or SPM (for gRPC-Swift)
- **Rust pipeline**: Running gRPC server from `driving-civicsense-vision-model`

### Build & Run (Android)

```bash
./gradlew :androidApp:assembleDebug
# Install APK on device / emulator
```

### Build & Run (iOS)

```bash
# 1. Build the shared KMP framework
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# 2. Open iosApp in Xcode, link CivicSenseShared.framework, and run
#    (Xcode project setup must include the framework in search paths)
```

### gRPC Service Contract

The app expects the Rust pipeline to expose a gRPC server implementing:

```protobuf
service CivicSenseAlertService {
  // Server-streaming alerts
  rpc SubscribeAlerts(AlertSubscription) returns (stream AlertEvent);

  // Current state snapshot
  rpc GetCurrentState(Empty) returns (CivicSenseState);

  // Health check
  rpc Ping(Empty) returns (Pong);
}
```

Full definition in [`proto/civicsense.proto`](proto/civicsense.proto).

## Screens

| Dashboard | Alert Log |
|-----------|-----------|
| Pipeline connection status | Scrollable alert history |
| FPS / uptime / state | Severity-colored icons |
| Active alerts summary | Distance & speed context |
| Critical / Warning / Info badges | Resolved status indicator |

## License

**GNU AGPL v3** — same as the parent CivicSense project.

---

<div align="center"><i>Your windshield's co-pilot, now in your pocket.</i></div>
