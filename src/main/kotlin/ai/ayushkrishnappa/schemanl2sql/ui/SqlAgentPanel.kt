package ai.ayushkrishnappa.schemanl2sql.ui

import ai.ayushkrishnappa.schemanl2sql.agent.SqlGenerationAgentFactory
import ai.ayushkrishnappa.schemanl2sql.editor.SqlEditorInserter
import ai.ayushkrishnappa.schemanl2sql.schema.DataGripSchemaIntrospector
import ai.ayushkrishnappa.schemanl2sql.validation.SqlValidator
import ai.ayushkrishnappa.schemanl2sql.workflow.GenerateSqlFromNlCoordinator
import ai.ayushkrishnappa.schemanl2sql.workflow.GenerateSqlFromNlResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

class SqlAgentPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val messagesPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(8)
        isOpaque = false
    }
    private val transcriptScroll = JBScrollPane(messagesPanel)
    private val promptArea = JBTextArea(4, 60).apply {
        lineWrap = true
        wrapStyleWord = true
        emptyText.text = "Example: Find top 10 customers by revenue in the last 30 days."
    }
    private val sendButton = JButton("Send")
    private val clearButton = JButton("Clear")
    private val insertSqlButton = JButton("Insert Latest SQL").apply {
        isEnabled = false
    }

    private val coordinator = GenerateSqlFromNlCoordinator(
        schemaIntrospector = DataGripSchemaIntrospector(),
        agent = SqlGenerationAgentFactory.create(),
        validator = SqlValidator(),
        editorInserter = SqlEditorInserter(),
    )
    private val editorInserter = SqlEditorInserter()

    private var latestSql: String? = null

    init {
        val bottomPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(8)
            add(JBScrollPane(promptArea), BorderLayout.CENTER)
            add(
                JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
                    add(clearButton)
                    add(insertSqlButton)
                    add(sendButton)
                },
                BorderLayout.SOUTH,
            )
        }

        add(transcriptScroll, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)

        sendButton.addActionListener { submitPrompt() }
        clearButton.addActionListener { clearConversationAndEditor() }
        insertSqlButton.addActionListener { insertLatestSql() }

        appendBubble(
            text = "Schema NL2SQL Agent ready.\nAsk for a query and refine via clarifications.",
            fromUser = false,
        )
    }

    private fun submitPrompt() {
        val prompt = promptArea.text.trim()
        if (prompt.isBlank()) {
            Messages.showWarningDialog(project, "Please describe the SQL you want to generate.", "Schema NL2SQL Agent")
            return
        }

        appendBubble(prompt, fromUser = true)
        promptArea.text = ""
        setBusy(true)

        ApplicationManager.getApplication().executeOnPooledThread {
            val result = coordinator.generate(project, prompt)
            ApplicationManager.getApplication().invokeLater {
                renderResult(result)
                setBusy(false)
            }
        }
    }

    private fun renderResult(result: GenerateSqlFromNlResult) {
        if (result.generatedSql != null && result.errors.isEmpty()) {
            latestSql = result.generatedSql
            insertSqlButton.isEnabled = true
            appendBubble("Here is the generated SQL:\n${result.generatedSql}", fromUser = false)
        } else if (result.status == "needs_clarification") {
            val text = buildString {
                append("I need clarification before generating SQL.")
                if (result.clarifyingQuestions.isNotEmpty()) {
                    append("\n")
                    result.clarifyingQuestions.forEach { append("\n- $it") }
                }
                if (result.guidance.isNotEmpty()) {
                    append("\n")
                    append("\nGuidance:")
                    result.guidance.forEach { append("\n- $it") }
                }
            }
            appendBubble(text, fromUser = false)
        } else {
            val text = buildString {
                append("Request failed.")
                if (result.errors.isNotEmpty()) {
                    result.errors.forEach { append("\n- $it") }
                }
            }
            appendBubble(text, fromUser = false)
        }

        result.validationFindings.forEach { appendBubble("Validation: $it", fromUser = false) }
        result.warnings.forEach { appendBubble("Warning: $it", fromUser = false) }
    }

    private fun insertLatestSql() {
        val sql = latestSql ?: return
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (editor == null) {
            Messages.showWarningDialog(
                project,
                "Open a SQL editor tab and place the cursor where SQL should be inserted.",
                "Schema NL2SQL Agent",
            )
            return
        }

        editorInserter.insert(project, editor, sql)
        appendBubble("Inserted latest SQL into active editor.", fromUser = false)
    }

    private fun clearConversationAndEditor() {
        promptArea.text = ""
        latestSql = null
        insertSqlButton.isEnabled = false

        messagesPanel.removeAll()
        appendBubble("Conversation cleared.", fromUser = false)

        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (editor != null) {
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.setText("")
            }
            appendBubble("Cleared SQL in the active editor.", fromUser = false)
        } else {
            appendBubble("No active editor found to clear.", fromUser = false)
        }

        messagesPanel.revalidate()
        messagesPanel.repaint()
    }

    private fun setBusy(value: Boolean) {
        sendButton.isEnabled = !value
        promptArea.isEnabled = !value
    }

    private fun appendBubble(text: String, fromUser: Boolean) {
        val row = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = JBUI.Borders.empty(4, 0)
            isOpaque = false
        }

        val safeText = StringUtil.escapeXmlEntities(text).replace("\n", "<br/>")
        val bubbleLabel = JBLabel("<html><body style='width: 320px'>$safeText</body></html>").apply {
            border = JBUI.Borders.compound(
                JBUI.Borders.customLine(BUBBLE_BORDER_COLOR, 1),
                JBUI.Borders.empty(8),
            )
            isOpaque = true
            background = if (fromUser) USER_BUBBLE_COLOR else AGENT_BUBBLE_COLOR
        }

        if (fromUser) {
            row.add(Box.createHorizontalGlue())
            row.add(bubbleLabel)
        } else {
            row.add(bubbleLabel)
            row.add(Box.createHorizontalGlue())
        }

        row.maximumSize = Dimension(Int.MAX_VALUE, bubbleLabel.preferredSize.height + 8)
        messagesPanel.add(row)
        messagesPanel.revalidate()
        messagesPanel.repaint()
        scrollToBottom()
    }

    private fun scrollToBottom() {
        val vertical = transcriptScroll.verticalScrollBar
        vertical.value = vertical.maximum
    }

    companion object {
        private val USER_BUBBLE_COLOR = JBColor(0xDDEBFF, 0x2F4F7A)
        private val AGENT_BUBBLE_COLOR = JBColor(0xF3F4F6, 0x3D3F43)
        private val BUBBLE_BORDER_COLOR = JBColor(0xC4CAD5, 0x555A61)
    }
}
