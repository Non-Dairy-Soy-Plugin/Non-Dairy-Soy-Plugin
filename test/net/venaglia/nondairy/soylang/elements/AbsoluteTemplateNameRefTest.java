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
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: ed
 * Date: 3/8/12
 * Time: 6:09 PM
 */
public class AbsoluteTemplateNameRefTest extends AbstractPsiElementTest {

    @Test
    public void testGetReference() throws Exception {
        AbsoluteTemplateNameRef ref = findNthElement("render1.soy",
                                                     AbsoluteTemplateNameRef.class,
                                                     "non.dairy.sample.library.format_person",
                                                     null,
                                                     1,
                                                     2);
        LocalTemplateNameDef def = findElement("library.soy",
                                               LocalTemplateNameDef.class,
                                               ".format_person",
                                               null);
        PsiReference psiReference = ref.getReference();
        assertNotNull(psiReference);
        PsiElement actual = psiReference.resolve();
        assertNotNull(actual);
        assertSame(def, actual);
    }
}
