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
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: ed
 * Date: 3/13/12
 * Time: 6:33 PM
 */
public class NamespaceTagElementTest extends AbstractPsiElementTest {

    @Test
    public void testGetNamespace() throws Exception {
        NamespaceTagElement tag = findElement("library.soy",
                                              NamespaceTagElement.class,
                                              "{namespace non.dairy.sample.library}",
                                              null);
        assertEquals("non.dairy.sample.library", tag.getNamespace());
    }

    @Test
    public void testGetName() throws Exception {
        NamespaceTagElement tag = findElement("library.soy",
                                              NamespaceTagElement.class,
                                              "{namespace non.dairy.sample.library}",
                                              null);
        assertNull(tag.getName());
    }

    @Test
    public void testGetPresentation() throws Exception {
        NamespaceTagElement tag = findElement("library.soy",
                                              NamespaceTagElement.class,
                                              "{namespace non.dairy.sample.library}",
                                              null);
        ItemPresentation presentation = tag.getPresentation();
        assertNull(presentation);
    }

}
