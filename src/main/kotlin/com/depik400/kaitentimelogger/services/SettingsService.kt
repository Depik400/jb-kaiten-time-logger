package com.depik400.kaitentimelogger.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*

@Service
@State(
    name = "KaitenTimeLoggerSettings",
    storages = [Storage("kaiten-time-logger.xml")]
)
class SettingsService : PersistentStateComponent<SettingsService.SettingsState> {

    data class SettingsState(
        var baseUrl: String = "",
        var apiToken: String = "",
        var lastSelectedRole: Int? = null,
        var autoOpenAfterCommit: Boolean = false,
        var autoDetectCardFromBranch: Boolean = true
    )

    private var myState = SettingsState()

    override fun getState(): SettingsState = myState

    override fun loadState(state: SettingsState) {
        myState = state
    }

    fun getCurrentState(): SettingsState = myState

    fun resetSettings() {
        myState = SettingsState()
    }

    companion object {
        fun getInstance(): SettingsService = ApplicationManager.getApplication().getService(SettingsService::class.java)
    }
}