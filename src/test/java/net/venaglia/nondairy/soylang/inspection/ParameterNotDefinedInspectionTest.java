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

package net.venaglia.nondairy.soylang.inspection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.intellij.codeInspection.ProblemDescriptor;
import net.venaglia.nondairy.mocks.MockInspectionManager;
import net.venaglia.nondairy.soylang.elements.AbstractPsiElementTest;
import net.venaglia.nondairy.soylang.elements.ParameterElement;
import net.venaglia.nondairy.util.ProjectFiles;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ed
 * Date: 4/2/12
 * Time: 7:43 PM
 */
@ProjectFiles
public class ParameterNotDefinedInspectionTest extends AbstractPsiElementTest {

    private ParameterNotDefinedInspection inspection;

    @Before
    public void setup() {
        inspection = new ParameterNotDefinedInspection() {
            @Override
            protected void checkCanceled() {
                // no-op
            }
        };
    }

    @Test
    public void testParameterNotDefined_invalid() throws Exception {
        buildAnonymousTestTemplate(
                "local",
                "{for $i in range(5)}\n" +
                "    {$not_declared}\n" +
                "    {$i}\n" +
                "{/for}",
                "declared"
        );

        ParameterElement ref = findElement("local",
                                           ParameterElement.class,
                                           "$not_declared",
                                           null);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("local"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(ref, problem.getPsiElement());
    }

    @Test
    public void testParameterNotDefined_valid() throws Exception {
        buildAnonymousTestTemplate(
                "local",
                "{for $i in range(5)}\n" +
                "    {$declared}\n" +
                "    {$i}\n" +
                "{/for}",
                "declared"
        );

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("local"), new MockInspectionManager(), true, problems);
        assertEquals(0, problems.size());
    }

//    @Test
//    public void testInnerParameterNotDefined_valid() throws Exception {
//        buildAnonymousTestTemplate(
//                "local",
//                "" +
//                        "{template .test}\n" +
//                        "    {@param optional: string}\n" +
//                        "    {$optional}\n" +
//                        "{/template}\n"
//        );
//
//        List<ProblemDescriptor> problems = new ArrayList<>();
//        inspection.findProblems(findRootElement("local"), new MockInspectionManager(), true, problems);
//        assertEquals(0, problems.size());
//    }

    @Test
    public void testParameterNotDefined_ij() throws Exception {
        buildAnonymousTestTemplate(
                "local",
                "{$ij}"
        );

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("local"), new MockInspectionManager(), true, problems);
        assertEquals(0, problems.size());
    }
}
