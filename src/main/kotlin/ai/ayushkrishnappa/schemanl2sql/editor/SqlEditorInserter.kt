package ai.ayushkrishnappa.schemanl2sql.editor

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class SqlEditorInserter {
    fun insert(project: Project, editor: Editor, sql: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(editor.caretModel.offset, sql)
        }
    }
}
