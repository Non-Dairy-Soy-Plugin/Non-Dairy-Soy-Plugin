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

package net.venaglia.nondairy.soylang;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import net.venaglia.nondairy.i18n.I18N;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: Jul 31, 2010
 * Time: 9:08:58 AM
 *
 * IntelliJ language definition for closure templates
 */
public class SoyLanguage extends Language {

    public static final SoyLanguage INSTANCE = new SoyLanguage();

    @NonNls
    private static final String ID = "ClosureTemplate";

    private SoyLanguage() {
        super(ID);
        SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(this, new SingleLazyInstanceSyntaxHighlighterFactory() {
            @NotNull
            protected SyntaxHighlighter createHighlighter() {
                return new SoySyntaxHighlighter();
            }
        });
    }

    @Override
    public boolean isCaseSensitive() {
        return true;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return I18N.msg("soy.language.display_name");
    }
}
