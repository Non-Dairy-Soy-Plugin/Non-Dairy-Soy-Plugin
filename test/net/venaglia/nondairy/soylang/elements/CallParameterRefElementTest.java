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
 * Date: 3/10/12
 * Time: 3:33 AM
 */
public class CallParameterRefElementTest extends AbstractPsiElementTest {

    @Test
    public void testGetReference_sameFile() throws Exception {
        CallParameterRefElement ref = findElement("library.soy",
                                                  CallParameterRefElement.class,
                                                  "city",
                                                  null);
        ParameterDefElement def = findElement("library.soy",
                                              ParameterDefElement.class,
                                              "city",
                                              null);
        PsiReference psiReference = ref.getReference();
        assertNotNull(psiReference);
        PsiElement actual = psiReference.resolve();
        assertNotNull(actual);
        assertSame(def, actual);
    }

    @Test
    public void testGetReference_crossFile() throws Exception {
        CallParameterRefElement ref = findNthElement("render1.soy",
                                                     CallParameterRefElement.class,
                                                     "person",
                                                     null,
                                                     0,
                                                     2);
        ParameterDefElement def = findElement("library.soy",
                                              ParameterDefElement.class,
                                              "person",
                                              null);
        PsiReference psiReference = ref.getReference();
        assertNotNull(psiReference);
        PsiElement actual = psiReference.resolve();
        assertNotNull(actual);
        assertSame(def, actual);
    }
}
