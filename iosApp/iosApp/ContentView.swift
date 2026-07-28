import SwiftUI
import CivicSenseShared

/// Root tab view: Dashboard (real-time status) and Alerts (history log).
struct ContentView: View {

    let viewModel: CivicSenseViewModel

    var body: some View {
        TabView {
            DashboardView(viewModel: viewModel)
                .tabItem {
                    Image(systemName: "gauge.with.dots.needle.33percent")
                    Text("Dashboard")
                }

            AlertListView(viewModel: viewModel)
                .tabItem {
                    Image(systemName: "list.bullet.clipboard")
                    Text("Alerts")
                }

            SettingsView()
                .tabItem {
                    Image(systemName: "gearshape")
                    Text("Settings")
                }
        }
    }
}

/// Minimal settings placeholder.
private struct SettingsView: View {
    var body: some View {
        NavigationStack {
            List {
                Section("Connection") {
                    LabeledContent("Pipeline Host", value: "localhost:8080")
                }
                Section("About") {
                    LabeledContent("Version", value: "0.1.0")
                    LabeledContent("Pipeline", value: "Rust Edge AI (gRPC)")
                }
            }
            .navigationTitle("Settings")
        }
    }
}
