package com.depik400.kaitentimelogger.listeners

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.depik400.kaitentimelogger.actions.openDialog
import com.depik400.kaitentimelogger.services.SettingsService
import com.intellij.openapi.vcs.changes.CommitContext

class CommitListenerFactory : CheckinHandlerFactory() {

    override fun createHandler(
        panel: CheckinProjectPanel,
        p1: CommitContext
    ): CheckinHandler {
        return object : CheckinHandler() {
            override fun checkinSuccessful() {
                val project = panel.project
                val commitMessage = panel.commitMessage
                val settings = SettingsService.getInstance()
                if (settings.getState().autoOpenAfterCommit) {
                    ApplicationManager.getApplication().invokeLater {
                        openDialog(project, null, commitMessage)
                    }
                }
            }
        }
    }
}