package io.github.beppe2880.opencodemobile.core.data.events

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIApplicationWillEnterForegroundNotification

/** Visible while the app is in the foreground (active or about to become active). */
internal class ApplicationStateVisibility : AppVisibility {
    override val isVisible: Flow<Boolean> = callbackFlow {
        val center = NSNotificationCenter.defaultCenter
        val queue = NSOperationQueue.mainQueue
        val observers = listOf(
            center.addObserverForName(UIApplicationWillEnterForegroundNotification, null, queue) { trySend(true) },
            center.addObserverForName(UIApplicationDidBecomeActiveNotification, null, queue) { trySend(true) },
            center.addObserverForName(UIApplicationDidEnterBackgroundNotification, null, queue) { trySend(false) },
        )
        queue.addOperationWithBlock {
            trySend(UIApplication.sharedApplication.applicationState != UIApplicationState.UIApplicationStateBackground)
        }
        awaitClose { observers.forEach(center::removeObserver) }
    }.distinctUntilChanged()
}
