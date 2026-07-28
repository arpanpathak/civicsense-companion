import SwiftUI
import CivicSenseShared

/// Dashboard screen: pipeline status, alert summary badge, active alerts list.
struct DashboardView: View {

    let viewModel: CivicSenseViewModel
    @State private var connectionText = "Idle"
    @State private var connectionColor = Color.gray
    @State private var activeAlerts: [ActiveAlert] = []
    @State private var fps: Float = 0
    @State private var uptime: String = "—"
    @State private var pipelineState: String = "—"
    @State private var criticalCount = 0
    @State private var warningCount = 0
    @State private var infoCount = 0

    // Observe Kotlin StateFlows via Combine publishers
    private let connectionAdapter = FlowAdapter<ConnectionState>()
    private let alertsAdapter = FlowAdapter<ActiveAlert>()
    private let statusAdapter = FlowAdapter<SystemStatus>()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    // Connection status card
                    statusCard

                    // Alert summary badge row
                    alertSummaryRow

                    // Active alerts section
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Active Alerts (\(activeAlerts.count))")
                            .font(.headline)

                        if activeAlerts.isEmpty {
                            Text("No active alerts — all clear.")
                                .foregroundColor(.secondary)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        } else {
                            ForEach(activeAlerts, id: \.id) { alert in
                                alertRow(alert)
                            }
                        }
                    }
                }
                .padding()
            }
            .background(Color(red: 0.1, green: 0.1, blue: 0.18))
            .navigationTitle("Dashboard")
        }
        .onAppear {
            observeFlows()
        }
    }

    // ── Connection status card ──────────────────────────

    private var statusCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Circle()
                    .fill(connectionColor)
                    .frame(width: 10, height: 10)
                Text("Pipeline: \(connectionText)")
                    .fontWeight(.semibold)
            }

            HStack(spacing: 24) {
                VStack {
                    Text(String(format: "%.1f", fps))
                        .foregroundColor(.green)
                        .fontWeight(.bold)
                    Text("FPS").font(.caption).foregroundColor(.secondary)
                }
                VStack {
                    Text(uptime)
                        .foregroundColor(.green)
                        .fontWeight(.bold)
                    Text("Uptime").font(.caption).foregroundColor(.secondary)
                }
                VStack {
                    Text(pipelineState)
                        .foregroundColor(.green)
                        .fontWeight(.bold)
                    Text("State").font(.caption).foregroundColor(.secondary)
                }
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(red: 0.15, green: 0.15, blue: 0.25))
        .cornerRadius(12)
    }

    // ── Alert summary row ──────────────────────────────

    private var alertSummaryRow: some View {
        HStack(spacing: 0) {
            summaryBadge(label: "Critical", count: criticalCount, color: .red)
            Spacer()
            summaryBadge(label: "Warning", count: warningCount, color: .yellow)
            Spacer()
            summaryBadge(label: "Info", count: infoCount, color: .green)
        }
        .padding()
        .background(Color(red: 0.15, green: 0.15, blue: 0.25))
        .cornerRadius(12)
    }

    private func summaryBadge(label: String, count: Int, color: Color) -> some View {
        VStack {
            Text("\(count)")
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(color)
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }

    // ── Alert row ───────────────────────────────────────

    private func alertRow(_ alert: ActiveAlert) -> some View {
        let severityColor: Color = {
            switch alert.severity {
            case .critical: return .red
            case .warning: return .yellow
            case .info: return .green
            }
        }()

        return HStack {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(severityColor)
            VStack(alignment: .leading) {
                Text(alert.title)
                    .fontWeight(.semibold)
                Text(alert.category.name
                    .replacingOccurrences(of: "_", with: " ")
                    .capitalized)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(red: 0.15, green: 0.15, blue: 0.25))
        .cornerRadius(8)
    }

    // ── Flow observation ─────────────────────────────────

    private func observeFlows() {
        // In production, use SKIE or KMP-NativeCoroutines to bridge
        // Kotlin StateFlows into Swift async sequences.
        // For now, this is a placeholder showing the intended wiring.
    }
}

// MARK: - Kotlin Flow → Swift bridge adapter (placeholder)

/// Thin adapter to observe Kotlin StateFlows from Swift.
/// In production, use the `SKIE` compiler plugin or
/// `KMP-NativeCoroutines` for ergonomic Swift async/await bridging.
class FlowAdapter<T> {
    // Placeholder — actual bridging depends on the KMP toolchain.
}
