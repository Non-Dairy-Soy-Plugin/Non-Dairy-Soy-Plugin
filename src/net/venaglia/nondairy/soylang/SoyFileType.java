/*
 * Copyright 2010 Ed Venaglia
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

package net.venaglia.nondairy.soylang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.tree.IFileElementType;
import net.venaglia.nondairy.i18n.I18N;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
* Created by IntelliJ IDEA.
* User: ed
* Date: Jul 31, 2010
* Time: 9:50:44 AM
* To change this template use File | Settings | File Templates.
*/
public class SoyFileType extends LanguageFileType {

    public static final SoyFileType INSTANCE = new SoyFileType();
    public static final Icon SOY_ICON = IconLoader.getIcon("/net/venaglia/nondairy/soylang/icons/soy.png");
    public static final IFileElementType FILE = new IFileElementType("Soy File Type", SoyLanguage.INSTANCE);

    private SoyFileType() {
        super(SoyLanguage.INSTANCE);
    }

    @NotNull
    public String getName() {
        return I18N.msg("soy.file_type.display_name");
    }

    @NotNull
    public String getDescription() {
        return I18N.msg("soy.file_type.description");
    }

    @NotNull
    public String getDefaultExtension() {
        return "soy";
    }

    public Icon getIcon() {
        return SOY_ICON;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
