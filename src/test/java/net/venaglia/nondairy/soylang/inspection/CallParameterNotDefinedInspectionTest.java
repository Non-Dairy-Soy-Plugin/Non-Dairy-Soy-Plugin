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
import net.venaglia.nondairy.soylang.elements.CallParameterRefElement;
import net.venaglia.nondairy.soylang.elements.ParameterElement;
import net.venaglia.nondairy.util.ProjectFiles;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ed
 * Date: 4/2/12
 * Time: 7:43 PM
 */
@ProjectFiles
public class CallParameterNotDefinedInspectionTest extends AbstractPsiElementTest {

    private CallParameterNotDefinedInspection inspection;

    @Before
    public void setup() {
        inspection = new CallParameterNotDefinedInspection() {
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
    @ProjectFiles(files = "example.soy")
    public void testExternalTemplateCall_invalid() throws Exception {
        buildAnonymousTestTemplate(
                "call_external",
                "{call example.soy.nondairy}\n" +
                "    {param not_declared:''/}\n" +
                "{/call}"
        );

        ParameterElement call = findElement("call_external",
                                            ParameterElement.class,
                                            "not_declared",
                                            null);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("call_external"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(call, problem.getPsiElement());
    }

    @Test
    public void testLocalTemplateCall_invalid() throws Exception {
        buildAnonymousTestTemplate(
                "call_local",
                "/**\n" +
                " */\n" +
                "{template .call_local}\n" +
                "    {call .test}\n" +
                "        {param not_declared:''/}\n" +
                "    {/call}\n" +
                "{/template}\n" +
                "\n" +
                "/**\n" +
                " * @param? declared\n" +
                " */\n" +
                "{template .test}\n" +
                "    {$declared}\n" +
                "{/template}"
        );

        CallParameterRefElement ref = findElement("call_local",
                                                  CallParameterRefElement.class,
                                                  "not_declared",
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

    @Test
    public void testLocalTemplateCall_valid() throws Exception {
        buildAnonymousTestTemplate(
                "call_local",
                "/**\n" +
                " */\n" +
                "{template .call_local}\n" +
                "    {call .test}\n" +
                "        {param declared:''/}\n" +
                "    {/call}\n" +
                "{/template}\n" +
                "\n" +
                "/**\n" +
                " * @param? declared\n" +
                " */\n" +
                "{template .test}\n" +
                "    {$declared}\n" +
                "{/template}"
        );

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("call_local"), new MockInspectionManager(), true, problems);
        assertEquals(0, problems.size());
    }
}
