import SwiftUI
import WidgetKit

// Home screen widget: running and recent sessions of the active server, and a new session
// button. The extension cannot reach the server: the app writes a snapshot to the App Group
// (WidgetSnapshots.kt in the shared Kotlin module) and asks WidgetKit to reload.

@main
struct SessionsWidgetBundle: WidgetBundle {
    var body: some Widget {
        SessionsWidget()
    }
}

struct SessionsWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "SessionsWidget", provider: SnapshotProvider()) { entry in
            SessionsWidgetView(entry: entry)
        }
        .configurationDisplayName(Text("widget_label"))
        .description(Text("widget_description"))
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
        .contentMarginsDisabledIfAvailable()
    }
}

// MARK: - Snapshot (the JSON contract with WidgetSnapshots.kt)

struct Snapshot: Decodable {
    struct Row: Decodable, Identifiable {
        let id: String
        let title: String
        let project: String
        let updatedAt: Int64
        let running: Bool
    }

    struct Palette: Decodable {
        let background, surface, text, muted, accent, onAccent, running: String
    }

    let connected: Bool
    let serverName: String?
    let stale: Bool
    let sessions: [Row]
    let theme: String
    let updatedAt: Int64
    let light: Palette
    let dark: Palette

    static func read() -> Snapshot? {
        guard let group = Bundle.main.object(forInfoDictionaryKey: "OCAppGroup") as? String,
              let json = UserDefaults(suiteName: group)?.string(forKey: "widget.snapshot"),
              let data = json.data(using: .utf8) else { return nil }
        return try? JSONDecoder().decode(Snapshot.self, from: data)
    }

    static var placeholder: Snapshot {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        return Snapshot(
            connected: true,
            serverName: "OpenCode",
            stale: false,
            sessions: [
                Row(id: "ses_preview1", title: "Fix the login redirect", project: "web", updatedAt: now, running: true),
                Row(id: "ses_preview2", title: "Add dark mode to settings", project: "app", updatedAt: now - 2 * 3_600_000, running: false),
            ],
            theme: "System",
            updatedAt: now,
            // OcLightColors and OcDarkColors, until the app writes the real ones.
            light: Palette(background: "#FFFFFF", surface: "#F2F2F2", text: "#161616", muted: "#5C5C5C",
                           accent: "#3B5CF6", onAccent: "#FFFFFF", running: "#198B43"),
            dark: Palette(background: "#161616", surface: "#2E2E2E", text: "#FAFAFA", muted: "#AEAEAE",
                          accent: "#3B5CF6", onAccent: "#FFFFFF", running: "#6BD586")
        )
    }
}

struct SnapshotEntry: TimelineEntry {
    let date: Date
    /// Nil before the app has written one (never opened, or no App Group).
    let snapshot: Snapshot?
}

struct SnapshotProvider: TimelineProvider {
    func placeholder(in context: Context) -> SnapshotEntry {
        SnapshotEntry(date: Date(), snapshot: .placeholder)
    }

    func getSnapshot(in context: Context, completion: @escaping (SnapshotEntry) -> Void) {
        completion(SnapshotEntry(date: Date(), snapshot: context.isPreview ? .placeholder : Snapshot.read() ?? .placeholder))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<SnapshotEntry>) -> Void) {
        // The app reloads the timeline when sessions change; these entries only age the
        // relative times ("5 min ago") in between.
        let snapshot = Snapshot.read()
        let now = Date()
        let entries = (0..<12).map { SnapshotEntry(date: now.addingTimeInterval(Double($0) * 600), snapshot: snapshot) }
        completion(Timeline(entries: entries, policy: .atEnd))
    }
}

// MARK: - Views

private enum Link {
    static let newSession = URL(string: "opencodemobile://new-session")!
    static let app = URL(string: "opencodemobile://open")!
    static func session(_ id: String) -> URL { URL(string: "opencodemobile://session/\(id)")! }
}

struct SessionsWidgetView: View {
    let entry: SnapshotEntry
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.widgetFamily) private var family

    private var palette: Colors {
        let snapshot = entry.snapshot ?? .placeholder
        let dark: Bool
        switch snapshot.theme {
        case "Dark": dark = true
        case "Light": dark = false
        default: dark = colorScheme == .dark
        }
        return Colors(dark ? snapshot.dark : snapshot.light)
    }

    var body: some View {
        let colors = palette
        VStack(alignment: .leading, spacing: 8) {
            Header(snapshot: entry.snapshot, colors: colors, compact: family == .systemSmall)
            content(colors)
            Spacer(minLength: 0)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .widgetBackground(colors.background)
        .widgetURL(Link.app)
    }

    @ViewBuilder
    private func content(_ colors: Colors) -> some View {
        if let snapshot = entry.snapshot, snapshot.connected {
            if snapshot.sessions.isEmpty {
                Message(text: Text("widget_empty"), colors: colors, url: Link.newSession)
            } else {
                VStack(alignment: .leading, spacing: 4) {
                    ForEach(snapshot.sessions.prefix(rowLimit)) { session in
                        SessionRow(session: session, colors: colors, now: entry.date, compact: family == .systemSmall)
                    }
                }
            }
        } else {
            Message(text: Text("widget_no_server"), colors: colors, url: Link.app)
        }
    }

    private var rowLimit: Int {
        switch family {
        case .systemSmall: return 2
        case .systemMedium: return 2
        default: return 6
        }
    }
}

private struct Header: View {
    let snapshot: Snapshot?
    let colors: Colors
    let compact: Bool

    var body: some View {
        HStack(alignment: .center, spacing: 8) {
            VStack(alignment: .leading, spacing: 1) {
                Text(verbatim: "OpenCode")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(colors.text)
                subtitle
                    .font(.system(size: 12))
                    .foregroundColor(colors.muted)
                    .lineLimit(1)
            }
            Spacer(minLength: 4)
            if snapshot?.connected == true {
                SwiftUI.Link(destination: Link.newSession) {
                    HStack(spacing: 2) {
                        Image(systemName: "plus").font(.system(size: 12, weight: .semibold))
                        if !compact { Text("widget_new_session").font(.system(size: 13, weight: .medium)) }
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .foregroundColor(colors.onAccent)
                    .background(Capsule().fill(colors.accent))
                }
            }
        }
    }

    private var subtitle: Text {
        guard let snapshot, snapshot.connected, let server = snapshot.serverName else {
            return Text("widget_not_connected")
        }
        if snapshot.stale {
            return Text(String(format: NSLocalizedString("widget_offline", comment: ""), server))
        }
        let running = snapshot.sessions.filter(\.running).count
        if running == 0 { return Text(verbatim: server) }
        return Text(String.localizedStringWithFormat(NSLocalizedString("widget_running", comment: ""), server, running))
    }
}

private struct SessionRow: View {
    let session: Snapshot.Row
    let colors: Colors
    let now: Date
    let compact: Bool

    var body: some View {
        SwiftUI.Link(destination: Link.session(session.id)) {
            HStack(spacing: 8) {
                Circle()
                    .fill(session.running ? colors.running : colors.muted.opacity(0.4))
                    .frame(width: 6, height: 6)
                VStack(alignment: .leading, spacing: 1) {
                    Group {
                        if session.title.trimmingCharacters(in: .whitespaces).isEmpty {
                            Text("widget_untitled")
                        } else {
                            Text(verbatim: session.title)
                        }
                    }
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(colors.text)
                    .lineLimit(1)
                    if !compact {
                        detail
                            .font(.system(size: 11))
                            .foregroundColor(session.running ? colors.running : colors.muted)
                            .lineLimit(1)
                    }
                }
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 6)
            .background(RoundedRectangle(cornerRadius: 8).fill(colors.surface))
        }
    }

    private var detail: Text {
        let project = Text(verbatim: session.project + " · ")
        if session.running { return project + Text("widget_session_running") }
        let updated = Date(timeIntervalSince1970: Double(session.updatedAt) / 1000)
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return project + Text(verbatim: formatter.localizedString(for: min(updated, now), relativeTo: now))
    }
}

private struct Message: View {
    let text: Text
    let colors: Colors
    let url: URL

    var body: some View {
        SwiftUI.Link(destination: url) {
            text
                .font(.system(size: 13))
                .foregroundColor(colors.muted)
                .multilineTextAlignment(.leading)
        }
    }
}

private struct Colors {
    let background, surface, text, muted, accent, onAccent, running: Color

    init(_ palette: Snapshot.Palette) {
        background = Color(hex: palette.background)
        surface = Color(hex: palette.surface)
        text = Color(hex: palette.text)
        muted = Color(hex: palette.muted)
        accent = Color(hex: palette.accent)
        onAccent = Color(hex: palette.onAccent)
        running = Color(hex: palette.running)
    }
}

private extension Color {
    /// `#RRGGBB`; anything else is gray.
    init(hex: String) {
        let value = UInt32(hex.dropFirst(), radix: 16) ?? 0x808080
        self.init(
            red: Double((value >> 16) & 0xFF) / 255,
            green: Double((value >> 8) & 0xFF) / 255,
            blue: Double(value & 0xFF) / 255
        )
    }
}

private extension View {
    /// iOS 17 draws the widget background itself and needs it declared.
    @ViewBuilder
    func widgetBackground(_ color: Color) -> some View {
        if #available(iOS 17.0, *) {
            containerBackground(for: .widget) { color }
        } else {
            background(color)
        }
    }
}

private extension WidgetConfiguration {
    /// The content sets its own padding, on every iOS version.
    func contentMarginsDisabledIfAvailable() -> some WidgetConfiguration {
        if #available(iOSApplicationExtension 17.0, *) {
            return contentMarginsDisabled()
        }
        return self
    }
}
