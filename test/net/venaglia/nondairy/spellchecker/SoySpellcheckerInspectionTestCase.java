/*
 * Copyright 2010 - 2012 Ed Venaglia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.venaglia.nondairy.spellchecker;

import com.intellij.spellchecker.inspection.SpellcheckerInspectionTestCase;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import net.venaglia.nondairy.SoyTestUtil;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * NOTE: To resolve the dependency {@link SpellcheckerInspectionTestCase},
 * make the Community Edition plugin module "spellchecker". The
 * module dependencies must include:
 * [community_edition_home]/out/test/spellchecker.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public abstract class SoySpellcheckerInspectionTestCase
    extends JavaCodeInsightFixtureTestCase {

  protected String getTestDataPath() {
    try {
      return URLDecoder.decode(SoyTestUtil.class.getResource(
          "testSources/spellchecker").getPath(), "UTF-8");
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
  }

  protected void doTest(String file) {
    myFixture.enableInspections(
        SpellcheckerInspectionTestCase.getInspectionTools());
    myFixture.testHighlighting(false, false, true, file);
  }
}
