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

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.soylang.icons.SoyIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * User: ed
 * Date: Jul 31, 2010
 * Time: 2:48:16 PM
 *
 * Color settings page used for customizing the syntax highlighting of soy files.
 */
public class SoyColorsAndFontsPage implements ColorSettingsPage {
    @NotNull
    public String getDisplayName() {
        return I18N.msg("syntax.color_editor.title");
    }

    @Nullable
    public Icon getIcon() {
        return SoyIcons.FILE;
    }

    @NotNull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRS;
    }

    private static final AttributesDescriptor[] ATTRS =
            new AttributesDescriptor[]{
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.command"), SoySyntaxHighlighter.SOY_COMMAND),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.keyword"), SoySyntaxHighlighter.SOY_KEYWORD),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.special_char"), SoySyntaxHighlighter.SOY_SPECIAL_CHAR),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.namespace"), SoySyntaxHighlighter.SOY_NAMESPACE_ID),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.template.identifier"), SoySyntaxHighlighter.SOY_TEMPLATE_ID),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.template.content"), SoySyntaxHighlighter.SOY_TEMPLATE_CONTENT),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.parameter"), SoySyntaxHighlighter.SOY_TEMPLATE_PARAMETER),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.function"), SoySyntaxHighlighter.SOY_FUNCTION),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.doc_comment"), SoySyntaxHighlighter.SOY_DOC_COMMENT),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.doc_comment.tag"), SoySyntaxHighlighter.SOY_DOC_COMMENT_TAG),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.doc_comment.identifier"), SoySyntaxHighlighter.SOY_DOC_COMMENT_IDENTIFIER),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.directive"), SoySyntaxHighlighter.SOY_DIRECTIVE_IDENTIFIER),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.directive.operator"), SoySyntaxHighlighter.SOY_DIRECTIVE_OPERATOR),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.string"), SoySyntaxHighlighter.SOY_STRING),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.number"), SoySyntaxHighlighter.SOY_NUMBER),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.operator"), SoySyntaxHighlighter.SOY_OPERATOR),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.tag_braces"), SoySyntaxHighlighter.SOY_TAG_BRACES),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.braces"), SoySyntaxHighlighter.SOY_BRACES),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.brackets"), SoySyntaxHighlighter.SOY_BRACKETS),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.parentheses"), SoySyntaxHighlighter.SOY_PARENTHS),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.illegal"), SoySyntaxHighlighter.SOY_BAD),
                    new AttributesDescriptor(I18N.msg("syntax.color_editor.category.ignore"), SoySyntaxHighlighter.SOY_IGNORE),
            };

    @NotNull
    public ColorDescriptor[] getColorDescriptors() {
        return new ColorDescriptor[0];
    }

    @NotNull
    public SyntaxHighlighter getHighlighter() {
        return new SoySyntaxHighlighter();
    }

    @NonNls
    @NotNull
    public String getDemoText() {
        return "// Example line comment\n" +
                "\n" +
                "{namespace example.soy}\n" +
                "\n" +
                "/**\n" +
                " * An example template doc comment\n" +
                " * @param required Need this parameter\n" +
                " * @param? optional Sometimes need this one\n" +
                " */\n" +
                "{template .nondairy autoescape=\"true\"}\n\n" +
                "    {if length($required) >= 100}\n" +
                "        Last Item: {$required[length($required) - 1]}<br>{sp}\n" +
                "    {/if}\n" +
                "\n" +
                "    {foreach $i in $required}\n" +
                "        Hello {$i|escapeHtml}{\\n}\n" +
                "    {ifempty \\} // <-- Illegal character\n" +
                "        {$optional|insertWordBreaks:8}\n" +
                "    {/foreach}\n" +
                "{/template}\n" +
                "\n" +
                "This text is ignored because it is not in a template.\n";
    }

    @Nullable
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        Map<String, TextAttributesKey> map = new HashMap<String, TextAttributesKey>();
        return map;
    }
}
