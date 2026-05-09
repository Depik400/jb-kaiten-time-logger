package com.depik400.kaitentimelogger.dialogs

import com.depik400.kaitentimelogger.models.TimeLogData
import com.depik400.kaitentimelogger.services.KaitenApiService
import com.depik400.kaitentimelogger.services.SettingsService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import java.time.LocalDate
import javax.swing.*


class TimeLogDialog(
    project: Project?,
    private val detectedCardId: Int?
) : DialogWrapper(project) {

    // Модели и компоненты
    private val roleModel = DefaultComboBoxModel<RoleItem>()
    private lateinit var cardIdField: JTextField
    private lateinit var commentArea: JTextArea
    private lateinit var timeSpinner: JSpinner
    private lateinit var timeUnitCombo: JComboBox<String>
    private lateinit var dateField: JTextField

    private val settings = SettingsService.getInstance()
    private val apiService = project?.let { KaitenApiService.getInstance(it) }

    data class RoleItem(val id: Int, val name: String)

    init {
        title = "Логирование времени в Kaiten"
        roleModel.addElement(RoleItem(-1, "Employee (по умолчанию)"))
        init()
        loadRolesAsync()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("ID карточки:") {
            cardIdField = textField()
                .columns(COLUMNS_MEDIUM)
                .applyToComponent { text = detectedCardId?.toString() ?: "" }
                .component
        }

        row("Время:") {
            timeSpinner = spinner(1..480).component
            timeUnitCombo = comboBox(DefaultComboBoxModel(arrayOf("минут", "часов"))).component
        }

        row("Дата:") {
            dateField = textField()
                .applyToComponent { text = LocalDate.now().toString() }
                .component
        }

        row("Роль:") {
            comboBox(roleModel)
                .align(AlignX.FILL)
                .applyToComponent {
                    // Используем статический метод создания рендерера
                    renderer = SimpleListCellRenderer.create("") { value ->
                        value?.let { "${it.name} (ID: ${it.id})" }
                    }
                }
        }

        row("Комментарий:") {
            commentArea = textArea()
                .rows(5)
                .align(AlignX.FILL)
                .component
        }.topGap(TopGap.SMALL)
    }

    private fun loadRolesAsync() {
        apiService?.getUserRoles { roles ->
            // UI DSL автоматически обновляет UI при изменении модели
            SwingUtilities.invokeLater {
                roles.forEach { role ->
                    val item = RoleItem(role.id, role.name)
                    if (findRoleById(item.id) == null) {
                        roleModel.addElement(item)
                    }
                }

                // Восстанавливаем сохранённую роль
                settings.getState().lastSelectedRole?.let { savedId ->
                    findRoleById(savedId)?.let { roleModel.selectedItem = it }
                }
            }
        }
    }

    private fun findRoleById(id: Int): RoleItem? {
        for (i in 0 until roleModel.size) {
            val item = roleModel.getElementAt(i)
            if (item.id == id) return item
        }
        return null
    }

    // ... методы doOKAction и getTimeLogData остаются прежними,
    // используя ссылки на созданные выше компоненты


    override fun doOKAction() {
        val selectedRole = roleModel.selectedItem as? RoleItem
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
        val selectedRole = roleModel.selectedItem as? RoleItem
        return TimeLogData(
            cardId = cardIdField.text.toIntOrNull() ?: 0,
            timeSpent = minutes,
            forDate = dateField.text,
            comment = commentArea.text,
            roleId = selectedRole?.id ?: -1
        )
    }
}