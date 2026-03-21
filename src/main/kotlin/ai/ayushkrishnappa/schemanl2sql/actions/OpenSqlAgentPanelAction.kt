package ai.ayushkrishnappa.schemanl2sql.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager

class OpenSqlAgentPanelAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Schema NL2SQL Agent")
        if (toolWindow == null) {
            Messages.showErrorDialog(project, "Agent panel is not available.", "Schema NL2SQL")
            return
        }

        toolWindow.activate(null)
    }
}

