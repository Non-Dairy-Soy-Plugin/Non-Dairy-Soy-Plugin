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

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 26, 2010
 * Time: 8:03:54 PM
 */
public abstract class AbstractElementPredicate implements ElementPredicate {

    public AbstractElementPredicate not() {
        final AbstractElementPredicate outer = this;
        return new AbstractElementPredicate() {
            @Override
            public boolean test(PsiElement element) {
                return !outer.test(element);
            }

            @Override
            public AbstractElementPredicate not() {
                return outer;
            }
        };
    }

    public TraversalPredicate onAncestors() {
        return new AbstractTraversalPredicate("<<") {
            @Override
            public PsiElementCollection traverse(Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement element : current) {
                    while (element != null && !(element instanceof PsiDirectory)) {
                        buffer.add(element);
                        element = element.getParent();
                    }
                }
                return buffer;
            }
        };
    }

    public TraversalPredicate onParent() {
        return new AbstractTraversalPredicate("<") {
            @Override
            public PsiElementCollection traverse(Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement element : current) {
                    element = element.getParent();
                    if (element != null && !(element instanceof PsiDirectory)) {
                        buffer.add(element);
                    }
                }
                return buffer;
            }
        };
    }

    public TraversalPredicate onFirstAncestor() {
        return new AbstractTraversalPredicate("<<1") {
            @Override
            public PsiElementCollection traverse(Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement element : current) {
                    element = element.getParent();
                    if (element != null && !(element instanceof PsiDirectory)) {
                        buffer.add(element);
                    }
                }
                return buffer;
            }

            @Override
            public boolean traverseAgainIfNoMatch() {
                return true;
            }
        };
    }

    public TraversalPredicate onDescendants() {
        return new AbstractTraversalPredicate(">>") {
            @Override
            public PsiElementCollection traverse(Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                Deque<PsiElement> queue = new LinkedList<PsiElement>(current);
                while (!queue.isEmpty()) {
                    List<PsiElement> children = Arrays.asList(queue.pop().getChildren());
                    buffer.addAll(children);
                    queue.addAll(children);
                }
                return buffer;
            }
        };
    }

    public TraversalPredicate onFirstChild() {
        return new AbstractTraversalPredicate(">:1") {
            @Override
            public PsiElementCollection traverse(Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement element : current) {
                    for (PsiElement child : element.getChildren()) {
                        if (AbstractElementPredicate.this.test(child)) {
                            buffer.add(child);
                            break;
                        }
                    }
                }
                return buffer;
            }
        };
    }

    public TraversalPredicate onChildren() {
        return new AbstractTraversalPredicate(">") {
            @Override
            public PsiElementCollection traverse(Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement element : current) {
                    buffer.addAll(Arrays.asList(element.getChildren()));
                }
                return buffer;
            }
        };
    }

    private abstract class AbstractTraversalPredicate implements TraversalPredicate {

        private final String symbol;

        public AbstractTraversalPredicate(@NotNull String symbol) {
            this.symbol = symbol;
        }

        @Override
        public boolean test(PsiElement element) {
            return AbstractElementPredicate.this.test(element);
        }

        @Override
        public boolean traverseAgainIfNoMatch() {
            return false;
        }

        @Override
        public String toString() {
            return symbol + "\t" + AbstractElementPredicate.this.toString();
        }
    }
}
