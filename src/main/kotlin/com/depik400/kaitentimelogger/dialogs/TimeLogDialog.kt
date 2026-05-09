package com.depik400.kaitentimelogger.dialogs

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBTextArea
import com.depik400.kaitentimelogger.models.TimeLogData
import com.depik400.kaitentimelogger.services.KaitenApiService
import com.depik400.kaitentimelogger.services.SettingsService
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.time.LocalDate
import javax.swing.*

class TimeLogDialog(
    project: Project?,
    private val detectedCardId: Int?
) : DialogWrapper(project) {

    private lateinit var cardIdField: JBTextField
    private lateinit var timeSpinner: JSpinner
    private lateinit var timeUnitCombo: JComboBox<String>
    private lateinit var dateField: JBTextField
    private lateinit var roleCombo: JComboBox<RoleItem>
    private lateinit var commentArea: JBTextArea

    private val settings = SettingsService.getInstance()
    private val apiService = project?.let { KaitenApiService.getInstance(it) }
    private val LOG = logger<TimeLogDialog>()

    data class RoleItem(val id: Int, val name: String) {
        override fun toString(): String = "$name (ID: $id)"
    }

    init {
        title = "Логирование времени в Kaiten"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply {
                insets = Insets(5, 5, 5, 5)
                fill = GridBagConstraints.HORIZONTAL
            }

            // ID карточки
            gbc.gridx = 0; gbc.gridy = 0
            add(JLabel("ID карточки:"), gbc)
            gbc.gridx = 1
            cardIdField = JBTextField().apply {
                detectedCardId?.let { text = it.toString() }
                columns = 15
            }
            add(cardIdField, gbc)

            // Время
            gbc.gridx = 0; gbc.gridy = 1
            add(JLabel("Время:"), gbc)
            gbc.gridx = 1
            val timePanel = JPanel(BorderLayout()).apply {
                timeSpinner = JSpinner(SpinnerNumberModel(1, 1, 480, 1))
                timeUnitCombo = JComboBox(arrayOf("минут", "часов"))
                add(timeSpinner, BorderLayout.CENTER)
                add(timeUnitCombo, BorderLayout.EAST)
            }
            add(timePanel, gbc)

            // Дата
            gbc.gridx = 0; gbc.gridy = 2
            add(JLabel("Дата:"), gbc)
            gbc.gridx = 1
            dateField = JBTextField(getLocalDate())
            add(dateField, gbc)

            // Роль (временно только стандартная)
            gbc.gridx = 0; gbc.gridy = 3
            add(JLabel("Роль:"), gbc)
            gbc.gridx = 1
            roleCombo = JComboBox<RoleItem>().apply {
                addItem(RoleItem(-1, "Employee (по умолчанию)"))
            }
            add(roleCombo, gbc)

            // Комментарий
            gbc.gridx = 0; gbc.gridy = 4
            add(JLabel("Комментарий:"), gbc)
            gbc.gridx = 1
            gbc.gridheight = 3
            commentArea = JBTextArea(5, 30)
            add(JScrollPane(commentArea), gbc)
        }

        // Асинхронная загрузка ролей
        loadRolesAsync()

        return panel
    }

    private fun loadRolesAsync() {
        if (apiService == null) {
            LOG.warn("ApiService is null, cannot load roles")
            return
        }

        apiService.getUserRoles { roles ->
            ApplicationManager.getApplication().invokeLater {
                if (roles.isNotEmpty()) {
                    val existingIds = (0 until roleCombo.itemCount).map { roleCombo.getItemAt(it).id }.toSet()
                    for (role in roles) {
                        if (role.id !in existingIds) {
                            roleCombo.addItem(RoleItem(role.id, role.name))
                        }
                    }
                    LOG.info("Added ${roles.size} roles to combo box")
                } else {
                    LOG.warn("No roles loaded from API")
                }

                // Восстанавливаем сохранённую роль
                val savedRoleId = settings.getState().lastSelectedRole
                if (savedRoleId != null) {
                    val savedRole = findRoleById(savedRoleId)
                    if (savedRole != null) {
                        roleCombo.selectedItem = savedRole
                    }
                }
            }
        }
    }

    private fun findRoleById(id: Int): RoleItem? {
        for (i in 0 until roleCombo.itemCount) {
            val item = roleCombo.getItemAt(i)
            if (item?.id == id) return item
        }
        return null
    }

    override fun doOKAction() {
        val selectedRole = roleCombo.selectedItem as? RoleItem
        selectedRole?.let {
            settings.getState().lastSelectedRole = it.id
        }
        super.doOKAction()
    }

    private fun getLocalDate(): String = LocalDate.now().toString()

    fun getTimeLogData(): TimeLogData {
        val timeValue = timeSpinner.value as Int
        val isHours = timeUnitCombo.selectedItem == "часов"
        val minutes = if (isHours) timeValue * 60 else timeValue
        val selectedRole = roleCombo.selectedItem as? RoleItem
        return TimeLogData(
            cardId = cardIdField.text.toIntOrNull() ?: 0,
            timeSpent = minutes,
            forDate = dateField.text,
            comment = commentArea.text,
            roleId = selectedRole?.id ?: -1
        )
    }
}