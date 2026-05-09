package com.depik400.kaitentimelogger.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.depik400.kaitentimelogger.services.SettingsService
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class SettingsConfigurable : Configurable {

    private lateinit var baseUrlField: JBTextField
    private lateinit var apiTokenField: JBPasswordField
    private lateinit var autoOpenCheckbox: JCheckBox
    private lateinit var autoDetectCheckbox: JCheckBox
    private lateinit var mainPanel: JPanel

    private val settings = SettingsService.getInstance()

    override fun getDisplayName(): String = "Kaiten Time Logger"

    override fun createComponent(): JComponent {
        mainPanel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply {
                insets = JBUI.insets(5)
                fill = GridBagConstraints.HORIZONTAL
                anchor = GridBagConstraints.WEST
            }

            // Заголовок
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.gridwidth = 2
            add(JLabel("<html><h3>Настройки Kaiten Time Logger</h3></html>"), gbc)
            gbc.gridwidth = 1

            // Базовый URL
            gbc.gridy = 1
            gbc.gridx = 0
            add(JLabel("Базовый URL:"), gbc)
            gbc.gridx = 1
            baseUrlField = JBTextField()
            add(baseUrlField, gbc)

            // API Token
            gbc.gridy = 2
            gbc.gridx = 0
            add(JLabel("API Token:"), gbc)
            gbc.gridx = 1
            apiTokenField = JBPasswordField()
            add(apiTokenField, gbc)

            // Авто-открытие
            gbc.gridy = 3
            gbc.gridx = 0
            gbc.gridwidth = 2
            autoOpenCheckbox = JCheckBox("Автоматически открывать после коммита")
            add(autoOpenCheckbox, gbc)

            // Авто-определение
            gbc.gridy = 4
            autoDetectCheckbox = JCheckBox("Автоматически определять карточку из ветки")
            add(autoDetectCheckbox, gbc)

            // Кнопка сброса
            gbc.gridy = 5
            val resetButton = JButton("Сбросить настройки").apply {
                addActionListener {
                    val confirmed = Messages.showYesNoDialog(
                        mainPanel,
                        "Вы уверены, что хотите сбросить все настройки?",
                        "Сброс настроек",
                        Messages.getQuestionIcon()
                    )
                    if (confirmed == Messages.YES) {
                        resetFields()
                    }
                }
            }
            add(resetButton, gbc)

            gbc.gridy = 6
            add(JLabel("<html><small>Как получить API токен: Настройки Kaiten → API Токены</small></html>"), gbc)
        }

        loadSettings()
        return mainPanel
    }

    private fun loadSettings() {
        if (!::baseUrlField.isInitialized) return
        baseUrlField.text = settings.state.baseUrl
        apiTokenField.text = settings.state.apiToken
        autoOpenCheckbox.isSelected = settings.state.autoOpenAfterCommit
        autoDetectCheckbox.isSelected = settings.state.autoDetectCardFromBranch
    }

    private fun resetFields() {
        baseUrlField.text = ""
        apiTokenField.text = ""
        autoOpenCheckbox.isSelected = false
        autoDetectCheckbox.isSelected = true
        apply()
        Messages.showInfoMessage(mainPanel, "Настройки сброшены", "Kaiten Time Logger")
    }

    override fun isModified(): Boolean {
        if (!::baseUrlField.isInitialized) return false
        return baseUrlField.text != settings.state.baseUrl ||
                String(apiTokenField.password) != settings.state.apiToken ||
                autoOpenCheckbox.isSelected != settings.state.autoOpenAfterCommit ||
                autoDetectCheckbox.isSelected != settings.state.autoDetectCardFromBranch
    }

    override fun apply() {
        if (!::baseUrlField.isInitialized) return
        settings.state.baseUrl = baseUrlField.text
        settings.state.apiToken = String(apiTokenField.password)
        settings.state.autoOpenAfterCommit = autoOpenCheckbox.isSelected
        settings.state.autoDetectCardFromBranch = autoDetectCheckbox.isSelected
    }

    override fun reset() {
        loadSettings()
    }
}