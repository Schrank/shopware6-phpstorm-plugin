package de.shyim.shopware6.action.generator.php

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.ui.php.NewScheduledTaskDialog
import javax.swing.JComponent

class NewScheduledTaskDialogWrapper(namespace: String) : DialogWrapper(true) {
    private var dialog: NewScheduledTaskDialog

    init {
        this.dialog = NewScheduledTaskDialog()
        this.dialog.namespaceField.text = namespace
        setSize(400, 200)
    }

    override fun createCenterPanel(): JComponent {
        return dialog.panel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return dialog.nameField
    }

    init {
        title = "Create a new scheduled task"
        init()
    }

    fun showAndGetConfig(): NewScheduledTaskConfig? {
        show()

        if (!isOK) {
            return null
        }

        return NewScheduledTaskConfig(
            dialog.nameField.getText(),
            dialog.taskField.getText(),
            dialog.intervalField.getText(),
            dialog.namespaceField.getText()
        )
    }
}