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

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: ed
 * Date: 3/13/12
 * Time: 6:43 PM
 */
public class FunctionCallRefElementTest extends AbstractPsiElementTest {

    @Test
    public void testGetReference() throws Exception {
        FunctionCallRefElement ref = findElement("render1.soy",
                                                 FunctionCallRefElement.class,
                                                 "length",
                                                 null);
        PsiReference reference = ref.getReference();
        assertNotNull(reference);
        PsiElement element = reference.resolve();
        assertNotNull(element);
        assertTrue(element instanceof PsiNamedElement);
        assertEquals("length", ((PsiNamedElement)element).getName());
        assertTrue(element instanceof NavigationItem);
        ItemPresentation presentation = ((NavigationItem)element).getPresentation();
        assertNotNull(presentation);
        assertNull(presentation.getLocationString());
        assertEquals("length", presentation.getPresentableText());
        assertEquals(SoyIcons.FUNCTION, presentation.getIcon(true));
        assertEquals(SoyIcons.FUNCTION, presentation.getIcon(false));
    }

    @Test
    public void testGetPresentation() throws Exception {
        FunctionCallRefElement ref = findElement("render1.soy",
                                                 FunctionCallRefElement.class,
                                                 "length",
                                                 null);
        ItemPresentation presentation = ref.getPresentation();
        assertNull(presentation);
    }

    @Test
    public void testGetName() throws Exception {

    }
}
