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

package net.venaglia.nondairy.soylang.elements.factory;

import com.intellij.extapi.psi.PsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.IntermediateElement;
import net.venaglia.nondairy.soylang.elements.SoyASTElement;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 25, 2010
 * Time: 7:34:10 AM
 */
public class SoyASTElementFactory {

    private SoyASTElementFactory() { } // pure static class

    private final static Map<IElementType, ASTElementFactory<?>> FACTORIES_BY_ELEMENT;

    static {
        Map<IElementType, ASTElementFactory<?>> factoriesByElement = new HashMap<IElementType, ASTElementFactory<?>>();
        for (Field field : SoyElement.class.getFields()) {
            ElementClass elementClassAnnotation = field.getAnnotation(ElementClass.class);
            int modifiers = field.getModifiers();
            if (elementClassAnnotation != null && SoyElement.class.equals(field.getType()) &&
                Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                try {
                    factoriesByElement.put((SoyElement)field.get(null),
                                           buildFactory(elementClassAnnotation));
                } catch (Exception e) {
                    throw new RuntimeException(e); // shouldn't happen
                }
            }
        }
        FACTORIES_BY_ELEMENT = Collections.unmodifiableMap(factoriesByElement);
    }

    @SuppressWarnings("unchecked")
    private static <T extends PsiElementBase> ASTElementFactory<T> buildFactory(ElementClass elementClass) {
        Class<?> cls = elementClass.value();
        return new SimpleASTElementFactory<T>((Class<T>)cls);
    }

    public static PsiElement createFor(ASTNode node) {
        ASTElementFactory<?> factory = FACTORIES_BY_ELEMENT.get(node.getElementType());
        if (factory == null) {
            return new SoyASTElement(node);
        }
        PsiElementBase element = factory.create(node);
        if (element instanceof IntermediateElement) {
            return ((IntermediateElement)element).resolveFinalElement();
        }
        return element;
    }
}
