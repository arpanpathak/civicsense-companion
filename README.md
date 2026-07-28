<div align="center">

# CivicSense Companion

> *Kotlin Multiplatform mobile companion for the CivicSense Edge AI perception pipeline.*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-purple.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Android-Compose-brightgreen)]()
[![Platform](https://img.shields.io/badge/iOS-SwiftUI-blue)]()
[![gRPC](https://img.shields.io/badge/gRPC-v1.71-red)]()
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

</div>

---

## What This Is

A **Kotlin Multiplatform** mobile app that displays real-time detection alerts from the [CivicSense](https://github.com/arpanpathak/driving-civicsense-vision-model) Rust perception pipeline. It connects via **gRPC** to receive intersection violations, lane camping warnings, stop sign alerts, and road hazard notifications — right on your phone.

### Architecture

The Rust pipeline runs on the edge device and exposes a gRPC server. The companion app connects over the local network to receive real-time alerts.

| Side | Component | Description |
|------|-----------|-------------|
| **Rust Pipeline** | Perception Engine | YOLOv8/v11 ONNX + Deep SORT tracking |
| | Alert Engine | Lane / intersection / hazard detection |
| | gRPC Server | Exposes `CivicSenseAlertService` on `:50051` |
| **Companion App** | `shared/` KMP module | Domain models, service interface, ViewModel |
| | Android UI | Jetpack Compose + Material 3 |
| | iOS UI | SwiftUI |

**Data flow:** Pipeline detects events -> gRPC stream (SubscribeAlerts) -> shared ViewModel -> platform UI updates

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

| Path | Purpose |
|------|---------|
| `proto/civicsense.proto` | gRPC contract between Rust pipeline and app |
| `shared/src/commonMain/` | Cross-platform models, service interface, ViewModel |
| `shared/src/androidMain/` | gRPC Android transport (OkHttp) |
| `shared/src/iosMain/` | Ktor HTTP transport (iOS) |
| `androidApp/` | Jetpack Compose UI (MainActivity, Dashboard, Alert Log) |
| `iosApp/iosApp/` | SwiftUI app (ContentView, DashboardView, AlertListView) |
| `build.gradle.kts` | Root Gradle build config |
| `settings.gradle.kts` | Gradle settings with module includes |
| `gradle/libs.versions.toml` | Version catalog for dependencies |

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

**Apache 2.0** — permissive license, patent protection.  
The parent Rust pipeline ([driving-civicsense-vision-model](https://github.com/arpanpathak/driving-civicsense-vision-model)) remains **AGPL v3**.

---

<div align="center"><i>Your windshield's co-pilot, now in your pocket.</i></div>
