/*
 * Copyright 2010 - 2013 Ed Venaglia
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

package net.venaglia.nondairy.soylang.inspection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.intellij.codeInspection.ProblemDescriptor;
import net.venaglia.nondairy.mocks.MockInspectionManager;
import net.venaglia.nondairy.soylang.elements.AbstractPsiElementTest;
import net.venaglia.nondairy.soylang.elements.ParameterDefElement;
import net.venaglia.nondairy.util.ProjectFiles;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ed
 * Date: 4/2/12
 * Time: 10:44 PM
 */
@ProjectFiles
public class ParameterNotUsedInspectionTest extends AbstractPsiElementTest {

    private ParameterNotUsedInspection inspection;

    @Before
    public void setup() {
        inspection = new ParameterNotUsedInspection() {
            @Override
            protected void checkCanceled() {
                // no-op
            }
        };
    }

    @Test
    public void testUnusedParameter_noParamsUsed() throws Exception {
        buildAnonymousTestTemplate(
                "unused_param",
                "dummy content",
                "my_param"
        );

        ParameterDefElement def = findElement("unused_param",
                                              ParameterDefElement.class,
                                              "my_param",
                                              null);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("unused_param"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(def, problem.getPsiElement());
    }

    @Test
    public void testUnusedParameter_oneParamUsed() throws Exception {
        buildAnonymousTestTemplate(
                "unused_param",
                "{$my_used_param}",
                "my_used_param",
                "my_unused_param"
        );

        ParameterDefElement def = findElement("unused_param",
                                              ParameterDefElement.class,
                                              "my_unused_param",
                                              null);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("unused_param"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(def, problem.getPsiElement());
    }

    @Test
    public void testUnusedParameter_allParamsUsed() throws Exception {
        buildAnonymousTestTemplate(
                "unused_param",
                "{$my_used_param}\n" +
                "{$my_unused_param}",
                "my_used_param",
                "my_unused_param"
        );

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("unused_param"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(0, problems.size());
    }

    @Test
    public void testUnusedParameter_usesDataAll() throws Exception {
        buildAnonymousTestTemplate(
                "unused_param",
                "{$my_used_param}\n" +
                "{call .dummy data=\"all\"/}",
                "my_used_param",
                "my_unused_param"
        );

        ParameterDefElement def = findElement("unused_param",
                                              ParameterDefElement.class,
                                              "my_unused_param",
                                              null);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("unused_param"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(def, problem.getPsiElement());
    }
}
