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

import com.intellij.navigation.ItemPresentation;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import net.venaglia.nondairy.util.ProjectFiles;
import org.junit.Test;

import javax.swing.*;

/**
 * User: ed
 * Date: 5/25/12
 * Time: 6:49 PM
 */
public class AbsoluteTemplateNameDefTest extends AbstractPsiElementTest {

    @Test
    @ProjectFiles(files = {"delegates-override.soy"}, inherit = false)
    public void testGetPresentation_deltemplate() throws Exception {
        AbsoluteTemplateNameDef def = findElement("delegates-override.soy",
                                                  AbsoluteTemplateNameDef.class,
                                                  "test.deltemplate.foo",
                                                  null);
        ItemPresentation presentation = def.getPresentation();
        assertNotNull(presentation);
        assertEquals("test.deltemplate", presentation.getLocationString());
        assertEquals("test.deltemplate.foo", presentation.getPresentableText());
        Icon icon = presentation.getIcon(true);
        assertEquals(SoyIcons.DELTEMPLATE, icon);
    }
}
