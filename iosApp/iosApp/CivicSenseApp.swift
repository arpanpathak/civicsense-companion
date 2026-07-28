import SwiftUI
import CivicSenseShared

/// Main entry point for the CivicSense iOS companion app.
///
/// Uses the shared KMP framework (`CivicSenseShared`) for:
///   - `CivicSenseViewModel` (business logic / state management)
///   - `CivicSenseServiceIOS` (HTTP bridge to the Rust pipeline)
///
/// The shared framework is built by the `shared` KMP module
/// and linked via the Xcode project's framework search path.
@main
struct CivicSenseApp: App {

    /// Shared ViewModel — lifetime matches the app.
    let viewModel: CivicSenseViewModel

    init() {
        // Instantiate the iOS-specific service (Ktor-based HTTP bridge).
        let service = CivicSenseServiceIOS(baseUrl: "http://localhost:8080")
        viewModel = CivicSenseViewModel(service: service)
        viewModel.connect()
    }

    var body: some Scene {
        WindowGroup {
            ContentView(viewModel: viewModel)
                .preferredColorScheme(.dark)
        }
    }
}
