// Placeholder — WidgetKit extension (build on macOS with Xcode).
// SPDX-License-Identifier: MIT

import WidgetKit
import SwiftUI

private let appGroupId = "group.org.aetherfeed.app"
private let snapshotName = "unread_widget_snapshot.json"

struct UnreadWidgetEntry: TimelineEntry {
    let date: Date
    let unreadTotal: Int
}

struct AetherFeedUnreadProvider: TimelineProvider {
    func placeholder(in context: Context) -> UnreadWidgetEntry {
        UnreadWidgetEntry(date: .now, unreadTotal: 0)
    }

    func getSnapshot(in context: Context, completion: @escaping (UnreadWidgetEntry) -> Void) {
        completion(readEntry())
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<UnreadWidgetEntry>) -> Void) {
        let entry = readEntry()
        completion(Timeline(entries: [entry], policy: .after(.now.addingTimeInterval(900))))
    }

    private func readEntry() -> UnreadWidgetEntry {
        guard let base = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: appGroupId) else {
            return UnreadWidgetEntry(date: .now, unreadTotal: 0)
        }
        let url = base.appendingPathComponent(snapshotName)
        guard let data = try? Data(contentsOf: url),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let total = json["unreadTotal"] as? Int else {
            return UnreadWidgetEntry(date: .now, unreadTotal: 0)
        }
        return UnreadWidgetEntry(date: .now, unreadTotal: total)
    }
}

struct AetherFeedUnreadWidgetView: View {
    let entry: UnreadWidgetEntry
    var body: some View {
        Text("Unread: \(entry.unreadTotal)")
    }
}

@main
struct AetherFeedUnreadWidgetBundle: WidgetBundle {
    var body: some Widget {
        StaticConfiguration(kind: "AetherFeedUnreadWidget", provider: AetherFeedUnreadProvider()) { entry in
            AetherFeedUnreadWidgetView(entry: entry)
        }
        .configurationDisplayName("AetherFeed unread")
        .description("Shows unread article count.")
        .supportedFamilies([.systemSmall, .accessoryCircular])
    }
}