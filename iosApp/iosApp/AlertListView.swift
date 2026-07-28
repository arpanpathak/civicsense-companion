import SwiftUI
import CivicSenseShared

/// Scrollable alert log — shows the most recent alerts from the pipeline.
struct AlertListView: View {

    let viewModel: CivicSenseViewModel
    @State private var alerts: [AlertEvent] = []

    var body: some View {
        NavigationStack {
            if alerts.isEmpty {
                ContentUnavailableView(
                    "No Alerts",
                    systemImage: "checkmark.circle",
                    description: Text("Connect to the pipeline to receive real-time alerts.")
                )
            } else {
                List(alerts, id: \.id) { alert in
                    alertRow(alert)
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Alert Log")
    }

    private func alertRow(_ alert: AlertEvent) -> some View {
        let severityColor: Color = {
            switch alert.severity {
            case .critical: return .red
            case .warning: return .yellow
            case .info: return .green
            }
        }()

        let iconName: String = {
            switch alert.category {
            case .stopSignViolation: return "stop.circle"
            case .speedWarning: return "speedometer"
            case .noTurnSignal, .lateTurnSignal, .multiLaneCut: return "car"
            case .intersectionOccupancy, .leftLaneCamping: return "bicycle"
            default: return "exclamationmark.triangle"
            }
        }()

        return HStack(alignment: .top) {
            Image(systemName: iconName)
                .foregroundColor(severityColor)
                .font(.title3)
                .frame(width: 28)

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(alert.title)
                        .fontWeight(.semibold)
                    Spacer()
                    if !alert.active {
                        Text("RESOLVED")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }

                Text(alert.description_)
                    .font(.subheadline)
                    .foregroundColor(.secondary)

                let distance = alert.distanceM?.floatValue ?? 0
                let speed = alert.egoSpeedKmh?.floatValue ?? 0
                if distance > 0 || speed > 0 {
                    HStack(spacing: 16) {
                        if distance > 0 {
                            Label(String(format: "%.0f m", distance),
                                  systemImage: "ruler")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        if speed > 0 {
                            Label(String(format: "%.0f km/h", speed),
                                  systemImage: "gauge")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
        }
        .padding(.vertical, 4)
    }
}
