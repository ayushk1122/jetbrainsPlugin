package ai.ayushkrishnappa.schemanl2sql.startup

import ai.ayushkrishnappa.schemanl2sql.schema.SchemaSnapshotPresenter
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class SchemaNl2SqlStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        NotificationGroup.balloonGroup("Schema NL2SQL")
            .createNotification(
                "Schema NL2SQL plugin is active.",
                "Use this notification to inspect the live schema snapshot while menu placement is being stabilized.",
                NotificationType.INFORMATION,
            )
            .addAction(OpenSchemaSnapshotAction(project))
            .notify(project)
    }

    private class OpenSchemaSnapshotAction(
        private val project: Project,
    ) : AnAction("Inspect Schema Snapshot") {
        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(event: AnActionEvent) {
            SchemaSnapshotPresenter.show(project)
        }
    }
}
