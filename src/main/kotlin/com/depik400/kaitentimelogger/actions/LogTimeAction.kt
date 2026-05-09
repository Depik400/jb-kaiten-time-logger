package com.depik400.kaitentimelogger.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.depik400.kaitentimelogger.dialogs.TimeLogDialog
import com.depik400.kaitentimelogger.services.KaitenApiService
import com.depik400.kaitentimelogger.utils.BranchParser
import git4idea.GitUtil

class LogTimeAction : AnAction() {

    private val LOG = logger<LogTimeAction>()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        try {
            val branchName = getCurrentBranch(project)
            LOG.info("Current branch: $branchName")

            val cardId = BranchParser.extractCardId(branchName)

            val dialog = TimeLogDialog(project, cardId)
            if (dialog.showAndGet()) {
                val data = dialog.getTimeLogData()
                LOG.info("Logging time for card: ${data.cardId}")

                val apiService = KaitenApiService.getInstance(project)
                apiService.sendTimeLog(data)
            }
        } catch (e: Exception) {
            LOG.error("Error in LogTimeAction", e)
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val isGitAvailable = project?.let {
            GitUtil.getRepositoryManager(it).repositories.isNotEmpty()
        } ?: false

        e.presentation.isEnabled = isGitAvailable
        e.presentation.isVisible = isGitAvailable
    }

    private fun getCurrentBranch(project: Project): String? {
        return try {
            val repositories = GitUtil.getRepositoryManager(project).repositories
            repositories.firstOrNull()?.currentBranchName
        } catch (e: Exception) {
            LOG.warn("Failed to get current branch", e)
            null
        }
    }
}