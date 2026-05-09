//package com.depik400.kaitentimelogger.listeners
//
//import com.intellij.openapi.diagnostic.logger
//import com.intellij.openapi.project.Project
//import com.intellij.openapi.vcs.VcsDataKeys
//import com.intellij.openapi.vcs.checkin.CheckinHandler
//import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
//import com.intellij.openapi.vcs.ui.RefreshableOnComponent
////import com.intellij.openapi.vfs.VirtualFile
//
//class GitCommitListener : GitCommitEditorListener {
//
//    private val LOG = logger<GitCommitListener>()
//
//    override fun onCommitFinished(project: Project, files: MutableList<VirtualFile>) {
//        LOG.info("Commit finished, files: ${files.size}")
//        // Здесь будет логика автоматического открытия окна
//        // если настройка autoOpenAfterCommit включена
//    }
//}