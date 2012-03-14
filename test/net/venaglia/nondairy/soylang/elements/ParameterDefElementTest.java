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
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: ed
 * Date: 3/13/12
 * Time: 8:20 AM
 */
public class ParameterDefElementTest extends AbstractPsiElementTest {

    @Test
    public void testGetNamespace() throws Exception {
        ParameterDefElement def = findElement("library.soy",
                                              ParameterDefElement.class,
                                              "person",
                                              null);
        assertEquals("non.dairy.sample.library", def.getNamespace());
    }

    @Test
    public void testGetTemplateName() throws Exception {
        ParameterDefElement def = findElement("library.soy",
                                              ParameterDefElement.class,
                                              "person",
                                              null);
        assertEquals("non.dairy.sample.library.format_person", def.getTemplateName());
    }

    @Test
    public void testGetName() throws Exception {
        ParameterDefElement def = findElement("library.soy",
                                              ParameterDefElement.class,
                                              "person",
                                              null);
        assertEquals("person", def.getName());
    }

    @Test
    public void testGetPresentation() throws Exception {
        ParameterDefElement def = findElement("library.soy",
                                              ParameterDefElement.class,
                                              "person",
                                              null);
        ItemPresentation presentation = def.getPresentation();
        assertNotNull(presentation);
        assertEquals("non.dairy.sample.library.format_person", presentation.getLocationString());
        assertEquals("person", presentation.getPresentableText());
        assertEquals(SoyIcons.PARAMETER, presentation.getIcon(true));
        assertEquals(SoyIcons.PARAMETER, presentation.getIcon(false));
    }
}
