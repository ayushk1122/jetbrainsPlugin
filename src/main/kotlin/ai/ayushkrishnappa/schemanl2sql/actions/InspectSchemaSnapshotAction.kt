package ai.ayushkrishnappa.schemanl2sql.actions

import ai.ayushkrishnappa.schemanl2sql.schema.SchemaSnapshotPresenter
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class InspectSchemaSnapshotAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isVisible = true
        event.presentation.isEnabled = event.project != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        SchemaSnapshotPresenter.show(project)
    }
}
