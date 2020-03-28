// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.maven.dom.intentions


import com.intellij.psi.impl.source.PostprocessReformattingAspect
import org.jetbrains.idea.maven.dom.MavenDomTestCase

class SortMavenDependenciesIntentionActionTest extends MavenDomTestCase {

  void testQuickFix() {
    createProjectPom("""
    <artifactId>childA</artifactId>
    <groupId>mavenParent</groupId>
    <version>1.0</version>

    <parent>
      <groupId>mavenParent</groupId>
      <artifactId>childA</artifactId>
      <version>1.0</version>
    </parent>
    
    <dependencies>
      <dependency>
        <groupId>AAA</groupId>
        <artifactId>CCC</artifactId>
      </dependency>
      <dependency>
        <groupId>ZZZ</groupId>
        <artifactId>AAA</artifactId>
      </dependency>
      <dependency>
        <groupId>AAA</groupId>
        <artifactId>AAA</artifactId>
      </dependency>
      <dependency>
        <groupId>AAA</groupId>
        <artifactId>BBB</artifactId>
      </dependency>
    </dependencies>
""")

    myFixture.configureFromExistingVirtualFile(myProjectPom)
    myFixture.doHighlighting()

    myFixture.launchAction(new SortMavenDependenciesIntentionAction())

    PostprocessReformattingAspect.getInstance(myProject).doPostponedFormatting()

    myFixture.checkResult(createPomXml("""
    <artifactId>childA</artifactId>
    <groupId>mavenParent</groupId>
    <version>1.0</version>

    <parent>
      <groupId>mavenParent</groupId>
      <artifactId>childA</artifactId>
      <version>1.0</version>
    </parent>
    
    <dependencies>
      <dependency>
        <groupId>AAA</groupId>
        <artifactId>AAA</artifactId>
      </dependency>
      <dependency>
        <groupId>AAA</groupId>
        <artifactId>BBB</artifactId>
      </dependency>
      <dependency>
        <groupId>AAA</groupId>
        <artifactId>CCC</artifactId>
      </dependency>
      <dependency>
        <groupId>ZZZ</groupId>
        <artifactId>AAA</artifactId>
      </dependency>
    </dependencies>
"""))
  }
}
