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

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 26, 2010
 * Time: 7:28:11 AM
 */
public class PsiPath_ {

    /**
     * Use this object when you wish to match any element in a PsiPath.
     */
    public static final ElementPredicate ANY;

    /**
     * Use this object when you wish to reference the direct children in a
     * PsiPath.
     */
    public static final ElementPredicate ALL_CHILDREN;

    /**
     * Use this object when you wish to reference the all descendant children
     * in a PsiPath.
     */
    public static final ElementPredicate ALL_CHILDREN_DEEP;

    /**
     * Use this object when you wish to reference the parent element in a
     * PsiPath.
     */
    public static final ElementPredicate PARENT_ELEMENT;

    static {
        AbstractElementPredicate any = new AbstractElementPredicate() {
            @Override
            public boolean test(PsiElement element) {
                return true;
            }

            @Override
            public String toString() {
                return "*";
            }
        };
        ANY = any;
        ALL_CHILDREN = any.onChildren();
        ALL_CHILDREN_DEEP = any.onDescendants();
        PARENT_ELEMENT = any.onParent();
    }

    private final ElementPredicate[] elementReferencePath;

    private String name;
    private boolean traceEnabled;

    public PsiPath(ElementPredicate... elementReferencePath) {
        this.elementReferencePath = elementReferencePath;
    }

    public final @NotNull PsiElementCollection navigate(@NotNull PsiElement start) {
        return navigate(Collections.singleton(start));
    }

    private static final ThreadLocal<Boolean> TRACE_ENABLED = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public final @NotNull PsiElementCollection navigate(@NotNull Collection<PsiElement> start) {
        final boolean traceEnabled = this.traceEnabled;
        final boolean previousValue = TRACE_ENABLED.get();
        if (traceEnabled) {
            TRACE_ENABLED.set(true);
            traceMessage("## [ begin path: %s ]", name);
        } else {
            TRACE_ENABLED.set(false);
        }
        try {
            return navigateImpl(start);
        } finally {
            if (traceEnabled) {
                traceMessage("## [ end path: %s ]", name);
            }
            TRACE_ENABLED.set(previousValue);
        }
    }

    static void traceMessage(@NonNls String format, Object... args) {
        if (!TRACE_ENABLED.get()) return;
        System.out.println(String.format(format, args));
    }

    @NotNull PsiElementCollection navigateImpl(@NotNull Collection<PsiElement> start) {
        if (start.isEmpty()) return PsiElementCollection.EMPTY;
        PsiElementCollection current = new PsiElementCollection(start);
        for (int i = 0, j = elementReferencePath.length; i < j && !current.isEmpty(); ++i) {
            ElementPredicate next = elementReferencePath[i];

            if (next == null) {
                throw new NullPointerException("elementReferencePath[" + i + "]");
            }
            if (next instanceof TraversalPredicate) {
                TraversalPredicate traversal = (TraversalPredicate)next;
                PsiElementCollection buffer;
                do {
                    current = traversal.traverse(current);
                    if (current.isEmpty()) {
                        buffer = PsiElementCollection.EMPTY;
                        break;
                    }
                    buffer = current.applyPredicate(next);
                } while (buffer.isEmpty() && traversal.traverseAgainIfNoMatch());
                current = buffer;
            } else {
                current = current.applyPredicate(next);
            }
            traceMessage("\t%s (%d %s)",
                         next,
                         current.size(),
                         current.size() == 1 ? "element" : "elements"); //NON-NLS
            if (i < j && current.isEmpty()) traceMessage("\tABORT!");
        }
        if (current.isEmpty()) return PsiElementCollection.EMPTY;
        return current;
    }

    public PsiPath or(@NotNull PsiPath psiPath) {
        return new OrPsiPath(this, psiPath);
    }

    public PsiPath exclude(@NotNull PsiPath psiPath) {
        return new ExcludePsiPath(psiPath);
    }

    public PsiPath append(PsiPath next) {
        return new AppendPsiPath(this, next);
    }

    public PsiPath append(ElementPredicate... elementReferencePath) {
        if (elementReferencePath.length == 0) return this;
        return append(new PsiPath(elementReferencePath));
    }

    public PsiPath trace(@NonNls String name) {
        this.name = name;
        this.traceEnabled = true;
        return this;
    }

    private static class OrPsiPath extends PsiPath {

        private final Collection<PsiPath> delegates;

        private OrPsiPath(PsiPath... delegates) {
            this.delegates = new LinkedList<PsiPath>(Arrays.asList(delegates));
        }

        @Override
        public PsiPath or(@NotNull PsiPath psiPath) {
            delegates.add(psiPath);
            return this;
        }

        @NotNull
        @Override
        PsiElementCollection navigateImpl(@NotNull Collection<PsiElement> start) {
            PsiElementCollection buffer = new PsiElementCollection();
            for (PsiPath psiPath : delegates) {
                buffer.addAll(psiPath.navigateImpl(start));
            }
            return buffer;
        }
    }

    private class ExcludePsiPath extends PsiPath {

        private final Collection<PsiPath> exclude;

        public ExcludePsiPath(@NotNull PsiPath exclude) {
            this.exclude = new LinkedList<PsiPath>();
            this.exclude.add(exclude);
        }

        @NotNull
        @Override
        PsiElementCollection navigateImpl(@NotNull Collection<PsiElement> start) {
            PsiElementCollection buffer = PsiPath.this.navigateImpl(start);
            for (PsiPath psiPath : exclude) {
                traceMessage("## begin exclude...");
                buffer.removeAll(psiPath.navigateImpl(start));
                traceMessage("## end exclude.");
                if (buffer.isEmpty()) return PsiElementCollection.EMPTY;
            }
            return buffer;
        }

        @Override
        public PsiPath or(@NotNull PsiPath psiPath) {
            throw new UnsupportedOperationException("or() not supported after calling seq()");
        }

        @Override
        public PsiPath exclude(@NotNull PsiPath psiPath) {
            exclude.add(psiPath);
            return this;
        }
    }

    private static class AppendPsiPath extends PsiPath {

        private final Collection<PsiPath> seq;

        public AppendPsiPath(@NotNull PsiPath start, @NotNull PsiPath next) {
            seq = new LinkedList<PsiPath>();
            seq.add(start);
            seq.add(next);
        }

        @Override
        public PsiPath append(@NotNull PsiPath next) {
            seq.add(next);
            return this;
        }

        @NotNull
        @Override
        PsiElementCollection navigateImpl(@NotNull Collection<PsiElement> start) {
            PsiElementCollection buffer = new PsiElementCollection(start);
            for (PsiPath path : seq) {
                buffer = path.navigateImpl(buffer);
            }
            return buffer;
        }
    }
}
