// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.maven.dom.intentions

import com.intellij.codeInspection.IntentionAndQuickFixAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.idea.maven.dom.MavenDomBundle
import org.jetbrains.idea.maven.dom.MavenDomUtil
import org.jetbrains.idea.maven.dom.model.MavenDomDependency
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel
import java.util.Comparator.comparing

/**
 * Defines an action and intention to sort the dependencies list of a Maven project file (usually "pom.xml").
 * By default this sorts by [MavenDomDependency.getGroupId] and then by [MavenDomDependency.getArtifactId],
 * but instances of this class can be created to sort the dependencies with any arbitrary [Comparator].
 *
 * @author JJ.Brown
 */
class SortMavenDependenciesIntentionAction @JvmOverloads constructor(val comparator: Comparator<MavenDomDependency> = comparing<MavenDomDependency, String?> {
  it.groupId?.stringValue ?: ""
} then comparing { it.artifactId?.stringValue ?: "" })
  : IntentionAndQuickFixAction() {
  override fun getFamilyName(): String =
    MavenDomBundle.message("inspection.group")

  override fun getName(): String =
    MavenDomBundle.message("maven.dom.quickfix.sort.maven.dependencies")

  override fun applyFix(project: Project, file: PsiFile?, editor: Editor?) {
    file ?: return

    val model = MavenDomUtil.getMavenDomProjectModel(project, file.virtualFile) ?: return

    val previousDependencyList = model.dependencies.dependencies
    val sortedDependencyList = previousDependencyList.sortedWith(comparator)

    val document = FileDocumentManager.getInstance().getDocument(file.virtualFile) ?: return

    // Modifying the document inside of a WriteCommandAction
    // so that we know we have proper exclusive write access to its text
    WriteCommandAction.writeCommandAction(project, file)
      .withName(MavenDomBundle.message("maven.dom.quickfix.sort.maven.dependencies"))
      .run<RuntimeException> {
        (previousDependencyList zip sortedDependencyList)
          // We must replace the text of the dependency tags in reverse
          // so that replacing the text of one dependency does not affect the position of the next dependency we edit
          .asReversed()
          // If we replace the text of every dependency so that it's in sorted order,
          // then we effectively sort the dependency list without having to reformat
          .forEach { (oldDependency, replacement) ->
            document.replaceString(oldDependency.xmlElement!!.startOffset,
                                   oldDependency.xmlElement!!.endOffset,
                                   replacement.xmlElement!!.text)
          }
      }
  }

  /**
   * Verifies whether the given file defines a Maven project, which is the only file this intention action applies to.
   * @see MavenDomUtil.isProjectFile
   */
  override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean =
    MavenDomUtil.isProjectFile(file)


  /**
   * Assuming that this intention action [isAvailable] for this file,
   * gets the XML element representing the dependencies list,
   * which this intention will edit.
   *
   * @return the XML element representing this Maven project file's dependencies list
   * @see isAvailable
   */
  override fun getElementToMakeWritable(currentFile: PsiFile): PsiElement? =
    MavenDomUtil.getMavenDomModel(currentFile, MavenDomProjectModel::class.java)?.dependencies?.xmlElement
}