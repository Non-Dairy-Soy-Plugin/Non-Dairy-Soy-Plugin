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

import com.intellij.codeInspection.ProblemDescriptor;
import net.venaglia.nondairy.mocks.MockInspectionManager;
import net.venaglia.nondairy.soylang.elements.AbstractPsiElementTest;
import net.venaglia.nondairy.soylang.elements.SoyCommandTag;
import net.venaglia.nondairy.util.ProjectFiles;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: ed
 * Date: 3/31/12
 * Time: 4:34 PM
 */
@ProjectFiles
public class CallMissingRequiredParamInspectionTest extends AbstractPsiElementTest {

    private CallMissingRequiredParamInspection inspection;

    @Before
    public void setup() {
        inspection = new CallMissingRequiredParamInspection() {
            @Override
            protected void checkCanceled() {
                // no-op
            }
        };
    }

    @Test
    @ProjectFiles(files = "example.soy")
    public void testExternalTemplateCall() throws Exception {
        buildAnonymousTestTemplate(
                "call_external",
                "{call example.soy.nondairy}\n" +
                "    {param optional:''/}\n" +
                "{/call}\n" +
                "{call example.soy.nondairy}\n" +
                "    {param required:''/}\n" +
                "{/call}"
        );

        SoyCommandTag call = findNthElement("call_external",
                                            SoyCommandTag.class,
                                            "{call example.soy.nondairy}",
                                            null,
                                            1,
                                            2);

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
    public void testLocalTemplateCall() throws Exception {
        buildAnonymousTestTemplate(
                "call_local",
                "/**\n" +
                " */\n" +
                "{template .call_local}\n" +
                "    {call .test}\n" +
                "        {param optional:''/}\n" +
                "    {/call}\n" +
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

        SoyCommandTag call = findNthElement("call_local",
                                            SoyCommandTag.class,
                                            "{call .test}",
                                            null,
                                            1,
                                            2);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("call_local"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(call, problem.getPsiElement());
    }
}
