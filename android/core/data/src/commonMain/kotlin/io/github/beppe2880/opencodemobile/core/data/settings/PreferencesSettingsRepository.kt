package io.github.beppe2880.opencodemobile.core.data.settings

import com.russhwolf.settings.Settings
import io.github.beppe2880.opencodemobile.core.domain.AppSettings
import io.github.beppe2880.opencodemobile.core.domain.SettingsRepository
import io.github.beppe2880.opencodemobile.core.domain.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Non-sensitive UI preferences. Unknown stored values fall back to [AppSettings] defaults. */
internal class PreferencesSettingsRepository(private val prefs: Settings) : SettingsRepository {

    private val mutex = Mutex()

    private val _settings = MutableStateFlow(read())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    override suspend fun update(transform: (AppSettings) -> AppSettings) = mutex.withLock {
        val next = transform(_settings.value)
        _settings.value = next
        prefs.putString(KEY_THEME, next.theme.name)
        prefs.putBoolean(KEY_SHOW_REASONING, next.showReasoning)
        prefs.putBoolean(KEY_EXPAND_TOOLS, next.expandToolsByDefault)
        prefs.putBoolean(KEY_SHOW_COST, next.showCost)
        prefs.putBoolean(KEY_SWIPE_CHATS, next.swipeBetweenChats)
        prefs.putBoolean(KEY_AGENT_OUTLINE, next.agentColorOutline)
        prefs.putBoolean(KEY_DICTATION, next.dictation)
        prefs.putBoolean(KEY_GROUP_ACTIVITY, next.groupActivity)
        prefs.putBoolean(KEY_NOTIFICATIONS, next.notifications)
        prefs.putBoolean(KEY_NOTIFY_PERMISSIONS, next.notifyPermissions)
        prefs.putBoolean(KEY_NOTIFY_QUESTIONS, next.notifyQuestions)
        prefs.putBoolean(KEY_NOTIFY_FINISHED, next.notifyFinished)
        prefs.putBoolean(KEY_NOTIFICATIONS_INTRO, next.notificationsIntroShown)
    }

    private fun read(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            theme = ThemeMode.entries.firstOrNull { it.name == prefs.getStringOrNull(KEY_THEME) } ?: defaults.theme,
            showReasoning = prefs.getBoolean(KEY_SHOW_REASONING, defaults.showReasoning),
            expandToolsByDefault = prefs.getBoolean(KEY_EXPAND_TOOLS, defaults.expandToolsByDefault),
            showCost = prefs.getBoolean(KEY_SHOW_COST, defaults.showCost),
            swipeBetweenChats = prefs.getBoolean(KEY_SWIPE_CHATS, defaults.swipeBetweenChats),
            agentColorOutline = prefs.getBoolean(KEY_AGENT_OUTLINE, defaults.agentColorOutline),
            dictation = prefs.getBoolean(KEY_DICTATION, defaults.dictation),
            groupActivity = prefs.getBoolean(KEY_GROUP_ACTIVITY, defaults.groupActivity),
            notifications = prefs.getBoolean(KEY_NOTIFICATIONS, defaults.notifications),
            notifyPermissions = prefs.getBoolean(KEY_NOTIFY_PERMISSIONS, defaults.notifyPermissions),
            notifyQuestions = prefs.getBoolean(KEY_NOTIFY_QUESTIONS, defaults.notifyQuestions),
            notifyFinished = prefs.getBoolean(KEY_NOTIFY_FINISHED, defaults.notifyFinished),
            notificationsIntroShown = prefs.getBoolean(KEY_NOTIFICATIONS_INTRO, defaults.notificationsIntroShown),
        )
    }

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_SHOW_REASONING = "show_reasoning"
        const val KEY_EXPAND_TOOLS = "expand_tools_by_default"
        const val KEY_SHOW_COST = "show_cost"
        const val KEY_SWIPE_CHATS = "swipe_between_chats"
        const val KEY_AGENT_OUTLINE = "agent_color_outline"
        const val KEY_DICTATION = "dictation"
        const val KEY_GROUP_ACTIVITY = "group_activity"
        const val KEY_NOTIFICATIONS = "notifications"
        const val KEY_NOTIFY_PERMISSIONS = "notify_permissions"
        const val KEY_NOTIFY_QUESTIONS = "notify_questions"
        const val KEY_NOTIFY_FINISHED = "notify_finished"
        const val KEY_NOTIFICATIONS_INTRO = "notifications_intro_shown"
    }
}
