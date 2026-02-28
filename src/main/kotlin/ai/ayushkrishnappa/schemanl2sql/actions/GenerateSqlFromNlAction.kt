package ai.ayushkrishnappa.schemanl2sql.actions

import ai.ayushkrishnappa.schemanl2sql.agent.MockSqlGenerationAgent
import ai.ayushkrishnappa.schemanl2sql.editor.SqlEditorInserter
import ai.ayushkrishnappa.schemanl2sql.schema.DataGripSchemaIntrospector
import ai.ayushkrishnappa.schemanl2sql.ui.NaturalLanguageSqlPromptDialog
import ai.ayushkrishnappa.schemanl2sql.validation.SqlValidator
import ai.ayushkrishnappa.schemanl2sql.workflow.GenerateSqlFromNlCoordinator
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class GenerateSqlFromNlAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val project = event.project
        val editor = event.getData(CommonDataKeys.EDITOR)
        event.presentation.isEnabledAndVisible = project != null && editor != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return

        val dialog = NaturalLanguageSqlPromptDialog(project)
        if (!dialog.showAndGet()) {
            return
        }

        val request = dialog.promptText.trim()
        if (request.isEmpty()) {
            Messages.showWarningDialog(project, "Please describe the SQL you want to generate.", "Natural Language SQL")
            return
        }

        val coordinator = createCoordinator(project)
        val result = coordinator.generate(project, editor, request)

        when {
            result.insertedSql != null -> {
                val warnings = result.warnings.takeIf { it.isNotEmpty() }?.joinToString("\n") ?: "Inserted generated SQL."
                Messages.showInfoMessage(project, warnings, "Natural Language SQL")
            }

            else -> {
                val details = (result.errors + result.warnings).joinToString("\n").ifBlank {
                    "The request could not be completed."
                }
                Messages.showErrorDialog(project, details, "Natural Language SQL")
            }
        }
    }

    private fun createCoordinator(project: Project): GenerateSqlFromNlCoordinator {
        return GenerateSqlFromNlCoordinator(
            schemaIntrospector = DataGripSchemaIntrospector(),
            agent = MockSqlGenerationAgent(),
            validator = SqlValidator(),
            editorInserter = SqlEditorInserter(),
        )
    }
}
