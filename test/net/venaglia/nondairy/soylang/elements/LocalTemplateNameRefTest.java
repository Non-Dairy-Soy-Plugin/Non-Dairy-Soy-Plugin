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

package net.venaglia.nondairy.soylang.elements;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import net.venaglia.nondairy.SoyTestUtil;
import net.venaglia.nondairy.util.TracePsiElementPath;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: ed
 * Date: 3/10/12
 * Time: 5:46 PM
 */
public class LocalTemplateNameRefTest extends AbstractPsiElementTest {

    @Test
    @TracePsiElementPath({"path_to_template_names"})
    public void testGetReference() throws Exception {
        System.out.println(SoyTestUtil.print(findRootElement("library.soy")));
        LocalTemplateNameRef ref = findElement("library.soy",
                                               LocalTemplateNameRef.class,
                                               ".format_city",
                                               null);
        LocalTemplateNameDef def = findElement("library.soy",
                                              LocalTemplateNameDef.class,
                                              ".format_city",
                                              null);
        PsiReference psiReference = ref.getReference();
        assertNotNull(psiReference);
        PsiElement actual = psiReference.resolve();
        assertNotNull(actual);
        assertSame(def, actual);
    }
}
