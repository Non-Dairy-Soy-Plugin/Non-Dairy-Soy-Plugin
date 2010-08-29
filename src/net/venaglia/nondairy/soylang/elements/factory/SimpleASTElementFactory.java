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
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:40:46 PM
 */
public class SimpleASTElementFactory<T extends PsiElementBase> implements ASTElementFactory<T> {

    private final Constructor<? extends T> constructor;

    public SimpleASTElementFactory(@NotNull Class<? extends T> type) {
        try {
            constructor = type.getConstructor(ASTNode.class);
            if (!Modifier.isPublic(constructor.getModifiers())) {
                throw new IllegalAccessException("Constructor to " + type.getSimpleName() + " is not public.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
