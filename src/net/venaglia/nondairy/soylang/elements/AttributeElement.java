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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: 1/28/12
 * Time: 10:29 AM
 *
 * SoyPsiElement to represent name/value attributes on soy tags.
 */
public class AttributeElement extends SoyPsiElement {

    private static final PsiElementPath PATH_TO_ATTRIBUTE_NAME = new PsiElementPath(
            new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
            new ElementTypePredicate(SoyElement.attribute).onChildren(),
            new ElementTypePredicate(SoyElement.attribute_key).onChildren()
    ).debug("path_to_attribute_name");

    private static final PsiElementPath PATH_TO_ATTRIBUTE_VALUE = new PsiElementPath(
            new ElementTypePredicate(SoyElement.expression).onChildren(),
            new ElementTypePredicate(SoyElement.attribute_value).onChildren()
    ).debug("path_to_attribute_value");

    public AttributeElement(@NotNull ASTNode node) {
        super(node);
    }

    /**
     * @return The name of the underlying attribute.
     */
    @Nullable
    public String getAttributeName() {
        PsiElement element = PATH_TO_ATTRIBUTE_NAME.navigate(this).oneOrNull();
        return element instanceof Key ? ((Key)element).getName() : null;
    }

    /**
     * @return The value of the underlying attribute.
     */
    @Nullable
    public String getAttributeValue() {
        PsiElement element = PATH_TO_ATTRIBUTE_VALUE.navigate(this).oneOrNull();
        return element instanceof Value ? ((Value)element).getValue() : null;
    }

    /**
     * SoyPsiElement to represent name on an attribute on a soy tag.
     */
    public static class Key extends SoyPsiElement implements PsiNamedElement {

        public Key(@NotNull ASTNode node) {
            super(node);
        }

        /**
         * @return The name of this attribute
         */
        @Override
        public String getName() {
            return getNode().getText();
        }
    }

    /**
     * SoyPsiElement to represent value on an attribute on a soy tag.
     */
    public static class Value extends SoyPsiElement {

        public Value(@NotNull ASTNode node) {
            super(node);
        }

        /**
         * @return The value of this attribute
         */
        public String getValue() {
            return getNode().getText();
        }
    }
}
