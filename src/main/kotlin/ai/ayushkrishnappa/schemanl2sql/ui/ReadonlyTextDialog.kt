package ai.ayushkrishnappa.schemanl2sql.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.Dimension
import javax.swing.JComponent

class ReadonlyTextDialog(
    project: Project,
    private val dialogTitle: String,
    text: String,
) : DialogWrapper(project) {
    private val textArea = JBTextArea(text, 24, 100).apply {
        isEditable = false
        lineWrap = false
        caretPosition = 0
    }

    init {
        title = dialogTitle
        init()
    }

    override fun createCenterPanel(): JComponent {
        return JBScrollPane(textArea).apply {
            preferredSize = Dimension(820, 520)
        }
    }
}
