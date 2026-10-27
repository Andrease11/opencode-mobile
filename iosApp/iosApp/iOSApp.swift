import Shared
import SwiftUI
import WidgetKit

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
                // Links from the home screen widget.
                .onOpenURL { url in AppLinksKt.openLink(url: url.absoluteString) }
        }
    }
}

/// Hosts the shared Compose UI (`MainViewController` in the `shared` Gradle module).
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(reloadWidgets: { WidgetCenter.shared.reloadAllTimelines() })
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
