package ai.ayushkrishnappa.schemanl2sql.schema

import ai.ayushkrishnappa.schemanl2sql.ui.ReadonlyTextDialog
import com.intellij.openapi.project.Project

object SchemaSnapshotPresenter {
    fun show(project: Project) {
        val result = DataGripSchemaIntrospector().inspect(project)
        val text = SchemaSnapshotFormatter.format(result)

        ReadonlyTextDialog(
            project = project,
            dialogTitle = "Schema Snapshot",
            text = text,
        ).show()
    }
}
