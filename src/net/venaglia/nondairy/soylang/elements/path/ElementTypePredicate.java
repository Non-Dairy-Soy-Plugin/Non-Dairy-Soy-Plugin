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

package net.venaglia.nondairy.soylang.elements.path;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
* Created by IntelliJ IDEA.
* User: ed
* Date: Aug 26, 2010
* Time: 7:30:25 AM
*/
public class ElementTypePredicate extends AbstractElementPredicate {

    private final Set<IElementType> types;

    public ElementTypePredicate(@NotNull IElementType type) {
        this.types = Collections.singleton(type);
    }

    public ElementTypePredicate(@NotNull IElementType type, IElementType... types) {
        this.types = new HashSet<IElementType>();
        this.types.add(type);
        this.types.addAll(Arrays.asList(types));
    }

    @Override
    public boolean test(PsiElement element) {
        ASTNode node = element.getNode();
        return node != null && types.contains(node.getElementType());
    }

    @Override
    public String toString() {
        if (types.size() == 1) {
            return "." + types.iterator().next();
        } else {
            StringBuilder buffer = new StringBuilder(64);
            buffer.append("[ ");
            for (IElementType type : types) {
                if (buffer.length() > 2) buffer.append(" | ");
                buffer.append('.').append(type);
            }
            buffer.append(" ]");
            return buffer.toString();
        }
    }
}
