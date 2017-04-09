/*
 * Copyright 2010 - 2013 Ed Venaglia
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

import net.venaglia.nondairy.soylang.SoyElement;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: 1/27/12
 * Time: 6:19 PM
 *
 * A PsiElementPath that will find template tab elements by their fully
 * qualified name.
 *
 * This path makes use of {@link net.venaglia.nondairy.soylang.elements.path.SoyFileElementTraversalPredicate} to navigate
 * to other files.
 */
public class NamespacePath extends PsiElementPath {

    private NamespacePath(@NotNull String namespaceName) {
        super(buildPath(namespaceName));
    }

    private static ElementPredicate[] buildPath(@NotNull String namespaceName) {
        namespaceName = namespaceName.trim();
        if (namespaceName.length() == 0) {
            throw new IllegalArgumentException("namespace name cannot be blank.");
        }
        if (namespaceName.endsWith(".") || namespaceName.startsWith(".")) {
            throw new IllegalArgumentException("invalid namespace name: " + namespaceName);
        }
        return new ElementPredicate[]{
                SoyFileElementTraversalPredicate.filesForNamespace(namespaceName),
                new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                new ElementTypePredicate(SoyElement.namespace_def).onChildren(),
                new ElementTypePredicate(SoyElement.namespace_name).onChildrenOfChildren(),
                new NamePredicate(namespaceName),
        };
    }

    /**
     * Safely builds a new {@link net.venaglia.nondairy.soylang.elements.path.PsiElementPath} for the specified namespace
     * name. If the namespace name is malformed, {@link net.venaglia.nondairy.soylang.elements.path.PsiElementPath#EMPTY} is
     * returned.
     * @param namespace The namespace name to navigate to.
     * @return A path object to navigate to the specified nmespace.
     */
    @NotNull
    public static PsiElementPath forNamespace(@NotNull String namespace) {
        try {
            return new NamespacePath(namespace);
        } catch (IllegalArgumentException e) {
            return PsiElementPath.EMPTY;
        }
    }
}
