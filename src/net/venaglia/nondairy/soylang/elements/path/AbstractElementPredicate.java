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

package net.venaglia.nondairy.soylang.elements.path;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * User: ed
 * Date: Aug 26, 2010
 * Time: 8:03:54 PM
 *
 * This class is used as a base class of the standard set of ElementPredicate
 * definitions, providing methods to build traversal predicates.
 */
public abstract class AbstractElementPredicate implements ElementPredicate {

    /**
     * Negates the logic of {@link #test(com.intellij.psi.PsiElement)}
     * @return A predicate with inverted logic
     */
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

    /**
     * @return a TraversalPredicate that will include all ancestors (parents,
     *     parents of parents, etc) of elements when traversing that are found
     *     acceptable by the predicate logic of this ElementPredicate.
     */
    public TraversalPredicate onAncestors() {
        return new AbstractTraversalPredicate("<<") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
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

    /**
     * @return a TraversalPredicate that will include the immediate parent of
     *     elements when traversing that are found acceptable by the predicate
     *     logic of this ElementPredicate.
     */
    public TraversalPredicate onParent() {
        return new AbstractTraversalPredicate("<") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
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

    /**
     * @return a TraversalPredicate that will include the first ancestor found
     *     acceptable by the predicate logic of this ElementPredicate.
     */
    public TraversalPredicate onFirstAncestor() {
        return new AbstractTraversalPredicate("<<1") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
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

    /**
     * The traversal predicate returned here can return a large number of
     * elements to process through {@link #test(com.intellij.psi.PsiElement)}.
     * Caution should be used to ensure that this traversal is used only when
     * necessary. {@link #onDescendants(int)} should be considered as a more
     * efficient alternative.
     * @return a TraversalPredicate that will include ALL descendants of
     *     elements when traversing that are found acceptable by the predicate
     *     logic of this ElementPredicate.
     */
    public TraversalPredicate onAllDescendants() {
        return new AbstractTraversalPredicate(">>") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
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

    /**
     * @param depth Maximum depth to descend into the psi tree.
     * @return a TraversalPredicate that will include descendants of elements
     *     when traversing that are found acceptable by the predicate logic
     *     of this ElementPredicate.
     */
    public TraversalPredicate onDescendants(final int depth) {
        return onDescendants(1, depth);
    }

    /**
     * @param from Minimum depth in the psi tree to begin finding elements.
     * @param to Maximum depth to descend into the psi tree.
     * @return a TraversalPredicate that will include descendants of elements
     *     when traversing that are found acceptable by the predicate logic
     *     of this ElementPredicate.
     */
    public TraversalPredicate onDescendants(final int from, final int to) {
        if (from <= 0) {
            throw new IllegalArgumentException("'from' depth must be a positive integer");
        }
        if (from > to) {
            throw new IllegalArgumentException("'from' must be less than 'to'");
        }
        if (to <= 0) {
            throw new IllegalArgumentException("'to' depth must be a positive integer");
        }
        if (from == 1 && to == 1) {
            return onChildren();
        }
        return new AbstractTraversalPredicate(">>{" + from + ".." + to + "}") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                Deque<Pair<Integer,PsiElement>> queue = new LinkedList<Pair<Integer,PsiElement>>();
                for (PsiElement element : current) {
                    queue.add(Pair.create(1, element));
                }
                while (!queue.isEmpty()) {
                    Pair<Integer,PsiElement> pair = queue.pop();
                    int d = pair.first;
                    List<PsiElement> children = Arrays.asList(pair.second.getChildren());
                    if (d >= from) {
                        buffer.addAll(children);
                    }
                    if (d < to) {
                        for (PsiElement element : children) {
                            queue.add(Pair.create(d + 1, element));
                        }
                    }
                }
                return buffer;
            }
        };
    }

    /**
     * @return a TraversalPredicate that will include first direct child of
     *     elements when traversing that are found acceptable by the predicate
     *     logic of this ElementPredicate.
     */
    public TraversalPredicate onFirstChild() {
        return new AbstractTraversalPredicate(">:1") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
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

    /**
     * @return a TraversalPredicate that will include all direct children of
     *     elements when traversing that are found acceptable by the predicate
     *     logic of this ElementPredicate.
     */
    public TraversalPredicate onChildren() {
        return new AbstractTraversalPredicate(">") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement element : current) {
                    buffer.addAll(Arrays.asList(element.getChildren()));
                }
                return buffer;
            }
        };
    }

    /**
     * @return a TraversalPredicate that will include children of child
     *     elements when traversing that are found acceptable by the predicate
     *     logic of this ElementPredicate.
     */
    public TraversalPredicate onChildrenOfChildren() {
        return new AbstractTraversalPredicate("> >") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement child : current) {
                    for (PsiElement grandChild : child.getChildren()) {
                        buffer.addAll(Arrays.asList(grandChild.getChildren()));
                    }
                }
                return buffer;
            }
        };
    }

    /**
     * @return a TraversalPredicate that will include the previous sibling of
     *     elements when traversing that are found acceptable by the predicate
     *     logic of this ElementPredicate.
     */
    public TraversalPredicate onPreviousSibling() {
        return new AbstractTraversalPredicate("{.}s") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement element : current) {
                    PsiElement e = element.getPrevSibling();
                    if (e != null) {
                        buffer.add(e);
                    }
                }
                return buffer;
            }
        };
    }

    /**
     * @param andSelf true if the returned collection should also include the
     *                starting elements, false otherwise.
     * @return a TraversalPredicate that will include the previous sibling of
     *     elements when traversing that are found acceptable by the predicate
     *     logic of this ElementPredicate.
     */
    public TraversalPredicate onPreviousSiblings(final boolean andSelf) {
        return new AbstractTraversalPredicate(andSelf ? "{..s}" : "{..}s") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement element : current) {
                    for (PsiElement e = andSelf ? element : element.getPrevSibling();
                         e != null;
                         e = e.getPrevSibling()) {
                        buffer.add(e);
                    }
                }
                return buffer;
            }
        };
    }

    /**
     * @return a TraversalPredicate that will include the previous sibling of
     *     elements when traversing that are found acceptable by the predicate
     *     logic of this ElementPredicate.
     */
    public TraversalPredicate onNextSibling() {
        return new AbstractTraversalPredicate("s{.}") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement element : current) {
                    PsiElement e = element.getNextSibling();
                    if (e != null) {
                        buffer.add(e);
                    }
                }
                return buffer;
            }
        };
    }

    /**
     * @param andSelf true if the returned collection should also include the
     *                starting elements, false otherwise.
     * @return a TraversalPredicate that will include the previous sibling of
     *     elements when traversing that are found acceptable by the predicate
     *     logic of this ElementPredicate.
     */
    public TraversalPredicate onNextSiblings(final boolean andSelf) {
        return new AbstractTraversalPredicate(andSelf ? "{s..}" : "s{..}") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
                PsiElementCollection buffer = new PsiElementCollection();
                for (PsiElement element : current) {
                    for (PsiElement e = andSelf ? element : element.getNextSibling();
                         e != null;
                         e = e.getNextSibling()) {
                        buffer.add(e);
                    }
                }
                return buffer;
            }
        };
    }

    /**
     * @return A TraversalPredicate that does not navigate the psi tree.
     */
    public TraversalPredicate onSelf() {
        return new AbstractTraversalPredicate("{s}") {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
                return new PsiElementCollection(current);
            }
        };
    }

    /**
     * Base class used for all TraversalPredicate inner class implementations.
     */
    private abstract class AbstractTraversalPredicate implements TraversalPredicate {

        private final String symbol;

        public AbstractTraversalPredicate(@NotNull @NonNls String symbol) {
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
            return String.format("%-8s  %s", symbol, AbstractElementPredicate.this.toString()); //NON-NLS
        }
    }
}
