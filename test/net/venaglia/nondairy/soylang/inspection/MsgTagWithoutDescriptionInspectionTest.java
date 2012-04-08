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
import net.venaglia.nondairy.soylang.elements.SoyCommandTag;
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
public class MsgTagWithoutDescriptionInspectionTest extends AbstractPsiElementTest {

    private MsgTagWithoutDescriptionInspection inspection;

    @Before
    public void setup() {
        inspection = new MsgTagWithoutDescriptionInspection() {
            @Override
            protected void checkCanceled() {
                // no-op
            }
        };
    }

    @Test
    public void testMsgMissingDesc() throws Exception {
        buildAnonymousTestTemplate(
                "unused_param",
                "{msg}\n" +
                "    dummy content\n" +
                "{/msg}\n" +
                "{msg desc=\"for testing\"}\n" +
                "    dummy content\n" +
                "{/msg}",
                "my_param"
        );

        SoyCommandTag msg = findElement("unused_param",
                                        SoyCommandTag.class,
                                        "{msg}",
                                        null);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("unused_param"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(msg, problem.getPsiElement());
    }

    @Test
    public void testMsgWithEmptyDesc() throws Exception {
        buildAnonymousTestTemplate(
                "unused_param",
                "{msg desc=\"\"}\n" +
                "    dummy content\n" +
                "{/msg}\n" +
                "{msg desc=\"for testing\"}\n" +
                "    dummy content\n" +
                "{/msg}",
                "my_param"
        );

        SoyCommandTag msg = findElement("unused_param",
                                        SoyCommandTag.class,
                                        "{msg desc=\"\"}",
                                        null);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("unused_param"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(msg, problem.getPsiElement());
    }

    @Test
    public void testMsgWithBlankDesc() throws Exception {
        buildAnonymousTestTemplate(
                "unused_param",
                "{msg desc=\"\t \"}\n" +
                "    dummy content\n" +
                "{/msg}\n" +
                "{msg desc=\"for testing\"}\n" +
                "    dummy content\n" +
                "{/msg}",
                "my_param"
        );

        SoyCommandTag msg = findElement("unused_param",
                                        SoyCommandTag.class,
                                        "{msg desc=\"\t \"}",
                                        null);

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("unused_param"), new MockInspectionManager(), true, problems);
        for (ProblemDescriptor problem : problems) {
            System.out.println(problem);
        }
        assertEquals(1, problems.size());
        ProblemDescriptor problem = problems.get(0);
        assertSame(msg, problem.getPsiElement());
    }

    @Test
    public void testMsgWithDesc() throws Exception {
        buildAnonymousTestTemplate(
                "unused_param",
                "{msg desc=\"for testing\"}\n" +
                "    dummy content\n" +
                "{/msg}",
                "my_param"
        );

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("unused_param"), new MockInspectionManager(), true, problems);
        assertEquals(0, problems.size());
    }

    @Test
    public void testNoMsg() throws Exception {
        buildAnonymousTestTemplate(
                "unused_param",
                "dummy content",
                "my_param"
        );

        List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        inspection.findProblems(findRootElement("unused_param"), new MockInspectionManager(), true, problems);
        assertEquals(0, problems.size());
    }
}
