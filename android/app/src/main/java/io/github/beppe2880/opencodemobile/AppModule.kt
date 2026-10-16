package io.github.beppe2880.opencodemobile

import io.github.beppe2880.opencodemobile.api.OpenCodeApiVersion
import io.github.beppe2880.opencodemobile.feature.chat.ChatViewModel
import io.github.beppe2880.opencodemobile.feature.chat.ImageMemory
import io.github.beppe2880.opencodemobile.feature.chat.ScrollMemory
import io.github.beppe2880.opencodemobile.feature.connect.ConnectViewModel
import io.github.beppe2880.opencodemobile.feature.sessions.SessionsViewModel
import io.github.beppe2880.opencodemobile.feature.settings.AppInfo
import io.github.beppe2880.opencodemobile.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppInfo(versionName = BuildConfig.VERSION_NAME, apiVersion = OpenCodeApiVersion.OPENCODE) }
    viewModel { ConnectViewModel(get(), get()) }
    viewModel { SessionsViewModel(get(), get(), get(), get(), get()) }
    // Kept across chats for the whole app run.
    single { ImageMemory() }
    single { ScrollMemory() }
    viewModel { params -> ChatViewModel(params.get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
}
