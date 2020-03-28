// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.maven.dom.inspections

import com.intellij.util.xml.DomFileElement
import com.intellij.util.xml.highlighting.BasicDomElementsInspection
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import org.jetbrains.idea.maven.dom.MavenDomBundle
import org.jetbrains.idea.maven.dom.intentions.SortMavenDependenciesIntentionAction
import org.jetbrains.idea.maven.dom.model.MavenDomDependency
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel

/**
 * Provides an inspection to validate whether Maven dependencies are specified in sorted order.
 */
class MavenUnsortedDependenciesInspection : BasicDomElementsInspection<MavenDomProjectModel>(MavenDomProjectModel::class.java) {
  private companion object {
    val COMPARE_BY_GROUP_ID: java.util.Comparator<MavenDomDependency> = Comparator.comparing { it.groupId?.stringValue ?: "" }
    val COMPARE_BY_ARTIFACT_ID: java.util.Comparator<MavenDomDependency> = Comparator.comparing { it.artifactId?.stringValue ?: "" }
    val COMPARE_BY_GROUP_ID_THEN_ARTIFACT_ID: Comparator<MavenDomDependency> = COMPARE_BY_GROUP_ID then COMPARE_BY_ARTIFACT_ID
  }

  override fun checkFileElement(domFileElement: DomFileElement<MavenDomProjectModel>?, holder: DomElementAnnotationHolder?) {
    val dependencies = domFileElement?.rootElement?.dependencies ?: return
    val dependenciesList = dependencies.dependencies
    for (i in 1 until dependenciesList.size) {
      if (COMPARE_BY_GROUP_ID_THEN_ARTIFACT_ID.compare(dependenciesList[i - 1], dependenciesList[i]) > 0) {
        holder?.createProblem(dependencies, MavenDomBundle.message("inspection.unsorted.dependencies.name"),
                              SortMavenDependenciesIntentionAction())
        return
      }
    }
  }

}