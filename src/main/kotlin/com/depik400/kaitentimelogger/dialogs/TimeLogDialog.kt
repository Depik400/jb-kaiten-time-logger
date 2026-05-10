package com.depik400.kaitentimelogger.dialogs

import com.depik400.kaitentimelogger.models.TimeLogData
import com.depik400.kaitentimelogger.services.KaitenApiService
import com.depik400.kaitentimelogger.services.SettingsService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.table.JBTable
import java.time.LocalDate
import javax.swing.*
import javax.swing.table.DefaultTableModel


class TimeLogDialog(
    project: Project?,
    private val detectedCardId: Int?,
    private val commitMessage: String?
) : DialogWrapper(project) {

    // Модели и компоненты
    private val roleModel = DefaultComboBoxModel<RoleItem>()
    private lateinit var cardIdField: JTextField
    private lateinit var commentArea: JTextArea
    private lateinit var timeSpinner: JSpinner
    private lateinit var timeUnitCombo: JComboBox<String>
    private lateinit var dateField: JTextField
    private lateinit var timeTable: JBTable
    private lateinit var tableModel: DefaultTableModel

    private var debounceTimer: Timer? = null

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
        tableModel = DefaultTableModel(arrayOf(), arrayOf("Дата", "Время", "Комментарий"))
        row {
            timeTable = JBTable(tableModel)
            scrollCell(timeTable)
                .align(AlignX.FILL)
        }.resizableRow()

        row("ID карточки:") {
            cardIdField = textField()
                .columns(COLUMNS_MEDIUM)
                .applyToComponent { text = detectedCardId?.toString() ?: "" }
                .onChanged { onCardIdChanged(it) }
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
                .applyToComponent { text = commitMessage ?: "" }
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

    private fun onCardIdChanged(value: JBTextField): Unit {
        debounceTimer?.stop()

        val cardIDText = value.text
        if (cardIDText.isBlank()) return;

        debounceTimer = Timer(500) {
            cardIDText.toIntOrNull()?.let {
                apiService?.getLoggedTime(it) { list ->
                    tableModel.setNumRows(0)
                    for (item in list) {
                        tableModel.addRow(
                            arrayOf(
                                item.forDate,
                                formatTime(item.timeSpent),
                                item.comment.toString()
                            )
                        )
                    }
                    timeTable.revalidate();
                    timeTable.repaint()
                }
            }
        }.apply {
            isRepeats = false
            start()
        }
    }

    fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) {
            if (mins > 0) "${hours}ч ${mins}м" else "${hours}ч"
        } else {
            "${mins}м"
        }
    }

    private fun findRoleById(id: Int): RoleItem? {
        for (i in 0 until roleModel.size) {
            val item = roleModel.getElementAt(i)
            if (item.id == id) return item
        }
        return null
    }

    override fun doOKAction() {
        val selectedRole = roleModel.selectedItem as? RoleItem
        selectedRole?.let {
            settings.getState().lastSelectedRole = it.id
        }
        super.doOKAction()
    }

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