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

package net.venaglia.nondairy.soylang.elements.factory;

import com.intellij.extapi.psi.PsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.IntermediateElement;
import net.venaglia.nondairy.soylang.elements.SoyPsiElement;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: ed
 * Date: Aug 25, 2010
 * Time: 7:34:10 AM
 * 
 * Primary {@link PsiElementFactory} implementation for the plugin.
 *
 * This factory will build build an internal index of delegate factories based
 * on {@link ElementClass} annotations on the constants defined in
 * {@link SoyElement} and {@link SoyToken}.
 */
public class SoyPsiElementFactory implements PsiElementFactory<PsiElement> {

    private static final SoyPsiElementFactory INSTANCE = new SoyPsiElementFactory();
    
    private SoyPsiElementFactory() { } // singleton

    private final static Map<IElementType, PsiElementFactory<?>> FACTORIES_BY_ELEMENT;

    static {
        Map<IElementType, PsiElementFactory<?>> factoriesByElement =
                new HashMap<IElementType, PsiElementFactory<?>>();
        try {
            for (Field field : SoyElement.class.getFields()) {
                if (shouldBuildSimpleFactoryFor(field)) {
                        factoriesByElement.put((SoyElement)field.get(null),
                                               buildFactory(field));
                }
            }
            for (Field field : SoyToken.class.getFields()) {
                if (shouldBuildSimpleFactoryFor(field)) {
                    factoriesByElement.put((SoyElement)field.get(null),
                                           buildFactory(field));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e); // shouldn't happen
        }
        FACTORIES_BY_ELEMENT = Collections.unmodifiableMap(factoriesByElement);
    }

    private static boolean shouldBuildSimpleFactoryFor(Field field) {
        ElementClass elementClassAnnotation = field.getAnnotation(ElementClass.class);
        if (elementClassAnnotation == null || !SoyElement.class.equals(field.getType())) {
            return false;
        }
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) &&
               Modifier.isStatic(modifiers) &&
               Modifier.isFinal(modifiers);
    }

    @SuppressWarnings("unchecked")
    private static <T extends PsiElementBase> PsiElementFactory<T> buildFactory(Field field)
            throws NoSuchMethodException, IllegalAccessException {
        Class<?> cls = field.getAnnotation(ElementClass.class).value();
        return new SimplePsiElementFactory<T>((Class<T>)cls);
    }

    @Override
    public PsiElement create(@NotNull ASTNode node) {
        PsiElementFactory<?> factory = FACTORIES_BY_ELEMENT.get(node.getElementType());
        if (factory == null) {
            return new SoyPsiElement(node);
        }
        PsiElement element = factory.create(node);
        if (element instanceof IntermediateElement) {
            return ((IntermediateElement)element).resolveFinalElement();
        }
        return element;
    }

    /**
     * @return the singleton instance of the SoyPsiElementFactory.
     */
    public static SoyPsiElementFactory getInstance() {
        return INSTANCE;
    }
}
