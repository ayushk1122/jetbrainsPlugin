package ai.ayushkrishnappa.schemanl2sql.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.Dimension
import javax.swing.JComponent

class NaturalLanguageSqlPromptDialog(project: Project) : DialogWrapper(project) {
    private val promptArea = JBTextArea(8, 60).apply {
        lineWrap = true
        wrapStyleWord = true
        emptyText.text = "Example: Show the top 10 customers by revenue in the last 30 days."
    }

    val promptText: String
        get() = promptArea.text

    init {
        title = "Generate SQL from Natural Language"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return JBScrollPane(promptArea).apply {
            preferredSize = Dimension(520, 180)
        }
    }
}
