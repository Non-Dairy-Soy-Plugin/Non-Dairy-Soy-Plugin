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
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:40:46 PM
 * 
 * Baseline implementation of PsiElementFactory for creating PsiElements using
 * reflection. Classes that use this factory are required to provide a public
 * constructor that takes a single argument of type {@link ASTNode}.
 */
public class SimplePsiElementFactory<T extends PsiElementBase> implements PsiElementFactory<T> {

    private final Constructor<? extends T> constructor;

    /**
     * Takes a {@link Class} object for the PsiElement type this factory will
     * build.
     * @param type The typ of PsiElement this factory will build
     * @throws NoSuchMethodException if this referenced class does not
     *     define a constructor that takes a single argument of type
     *     {@link ASTNode}.
     * @throws IllegalAccessException if this referenced class defines a public
     *     constructor that takes a single argument of type {@link ASTNode},
     *     but that constructor is not public.
     */
    public SimplePsiElementFactory(@NotNull Class<? extends T> type)
            throws IllegalAccessException, NoSuchMethodException {
        constructor = type.getConstructor(ASTNode.class);
        if (!Modifier.isPublic(constructor.getModifiers())) {
            throw new IllegalAccessException("Constructor to " + type.getSimpleName() + " is not public.");
        }
    }

    @Override
    public T create(@NotNull ASTNode node) {
        try {
            return constructor.newInstance(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
