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
import net.venaglia.nondairy.soylang.elements.AbsoluteTemplateNameRef;
import net.venaglia.nondairy.soylang.elements.AbstractPsiElementTest;
import net.venaglia.nondairy.soylang.elements.LocalTemplateNameRef;
import net.venaglia.nondairy.util.ProjectFiles;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ed
 * Date: 3/31/12
 * Time: 4:34 PM
 */
@ProjectFiles
public class CallToUndeclaredTemplateInspectionTest extends AbstractPsiElementTest {

    private CallToUndeclaredTemplateInspection inspection;

    @Before
    public void setup() {
        inspection = new CallToUndeclaredTemplateInspection() {
            @Override
            protected void checkCanceled() {
                // no-op
            }
        };
    }

    @Test
    @ProjectFiles(files = "example.soy")
    public void testExternalTemplateCall_valid() throws Exception {
        buildAnonymousTestTemplate(
                "call_external",
                "{call example.soy.nondairy}\n" +
                "    {param required:''/}\n" +
                "{/call}"
        );

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("call_external"), new MockInspectionManager(), true, problems);
        assertEquals(0, problems.size());
    }

    @Test
    public void testExternalTemplateCall_invalid() throws Exception {
        buildAnonymousTestTemplate(
                "call_external",
                "{call example.soy.nondairy}\n" +
                "    {param required:''/}\n" +
                "{/call}"
        );

        AbsoluteTemplateNameRef ref = findElement("call_external",
                                                  AbsoluteTemplateNameRef.class,
                                                  "example.soy.nondairy",
                                                  null);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("call_external"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(ref, problem.getPsiElement());
    }

    @Test
    public void testLocalTemplateCall_valid() throws Exception {
        buildAnonymousTestTemplate(
                "call_local",
                "/**\n" +
                " */\n" +
                "{template .call_local}\n" +
                "    {call .test}\n" +
                "        {param required:''/}\n" +
                "    {/call}\n" +
                "{/template}\n" +
                "\n" +
                "/**\n" +
                " * @param required required param\n" +
                " * @param? optional required param\n" +
                " */\n" +
                "{template .test}\n" +
                "    {$optional ? $optional : $required}\n" +
                "{/template}\n"
        );

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("call_local"), new MockInspectionManager(), true, problems);
        assertEquals(0, problems.size());
    }

    @Test
    public void testLocalTemplateCall_invalid() throws Exception {
        buildAnonymousTestTemplate(
                "call_local",
                "/**\n" +
                " */\n" +
                "{template .call_local}\n" +
                "    {call .test_fail}\n" +
                "        {param required:''/}\n" +
                "    {/call}\n" +
                "{/template}\n" +
                "\n" +
                "/**\n" +
                " * @param required required param\n" +
                " * @param? optional required param\n" +
                " */\n" +
                "{template .test}\n" +
                "    {$optional ? $optional : $required}\n" +
                "{/template}\n"
        );

        LocalTemplateNameRef ref = findElement("call_local",
                                               LocalTemplateNameRef.class,
                                               ".test_fail",
                                               null);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("call_local"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(ref, problem.getPsiElement());
    }
}
