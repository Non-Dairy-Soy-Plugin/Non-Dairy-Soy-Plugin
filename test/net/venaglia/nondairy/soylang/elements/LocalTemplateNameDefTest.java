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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.intellij.navigation.ItemPresentation;
import com.intellij.ui.RowIcon;
import com.intellij.util.PlatformIcons;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import net.venaglia.nondairy.util.ProjectFiles;
import org.junit.Test;

import javax.swing.*;

/**
 * User: ed
 * Date: 3/13/12
 * Time: 8:20 AM
 */
public class LocalTemplateNameDefTest extends AbstractPsiElementTest {

    @Test
    public void testGetNamespace() throws Exception {
        LocalTemplateNameDef def = findElement("library.soy",
                                               LocalTemplateNameDef.class,
                                               ".format_person",
                                               null);
        assertEquals("non.dairy.sample.library", def.getNamespace());
    }

    @Test
    public void testGetTemplateName() throws Exception {
        LocalTemplateNameDef def = findElement("library.soy",
                                               LocalTemplateNameDef.class,
                                               ".format_person",
                                               null);
        assertEquals("non.dairy.sample.library.format_person", def.getTemplateName());
    }

    @Test
    public void testGetName() throws Exception {
        LocalTemplateNameDef def = findElement("library.soy",
                                               LocalTemplateNameDef.class,
                                               ".format_person",
                                               null);
        assertEquals("format_person", def.getName());
    }

    @Test
    public void testGetPresentation() throws Exception {
        LocalTemplateNameDef def = findElement("library.soy",
                                               LocalTemplateNameDef.class,
                                               ".format_person",
                                               null);
        ItemPresentation presentation = def.getPresentation();
        assertNotNull(presentation);
        assertEquals("non.dairy.sample.library", presentation.getLocationString());
        assertEquals("non.dairy.sample.library.format_person", presentation.getPresentableText());
        Icon icon = presentation.getIcon(true);
        assertTrue(icon instanceof RowIcon);
        assertEquals(2, ((RowIcon)icon).getIconCount());
        assertEquals(SoyIcons.TEMPLATE, ((RowIcon)icon).getIcon(0));
        assertEquals(PlatformIcons.PUBLIC_ICON, ((RowIcon)icon).getIcon(1));
    }

    @Test
    @ProjectFiles({"example.soy"})
    public void testGetPresentation_private() throws Exception {
        LocalTemplateNameDef def = findElement("example.soy",
                                               LocalTemplateNameDef.class,
                                               ".nondairy",
                                               null);
        ItemPresentation presentation = def.getPresentation();
        assertNotNull(presentation);
        assertEquals("example.soy", presentation.getLocationString());
        assertEquals("example.soy.nondairy", presentation.getPresentableText());
        Icon icon = presentation.getIcon(true);
        assertTrue(icon instanceof RowIcon);
        assertEquals(2, ((RowIcon)icon).getIconCount());
        assertEquals(SoyIcons.TEMPLATE, ((RowIcon)icon).getIcon(0));
        assertEquals(PlatformIcons.PRIVATE_ICON, ((RowIcon)icon).getIcon(1));
    }

    @Test
    @ProjectFiles({"delegates.soy"})
    public void testGetPresentation_deltemplate() throws Exception {
        LocalTemplateNameDef def = findElement("delegates.soy",
                                               LocalTemplateNameDef.class,
                                               ".foo",
                                               null);
        ItemPresentation presentation = def.getPresentation();
        assertNotNull(presentation);
        assertEquals("my.namespace", presentation.getLocationString());
        assertEquals("my.namespace.foo", presentation.getPresentableText());
        Icon icon = presentation.getIcon(true);
        assertTrue(icon instanceof RowIcon);
        assertEquals(2, ((RowIcon)icon).getIconCount());
        assertEquals(SoyIcons.DELTEMPLATE, ((RowIcon)icon).getIcon(0));
        assertEquals(PlatformIcons.PUBLIC_ICON, ((RowIcon)icon).getIcon(1));
    }
}
