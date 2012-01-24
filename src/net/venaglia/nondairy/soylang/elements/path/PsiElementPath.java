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
public class PsiElementPath {

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
     * Use this object when you wish to reference the direct children in a
     * PsiPath.
     */
    public static final ElementPredicate FIRST_CHILD;

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

    /**
     * Use this object when you wish to build a path that returns the original
     * node.
     */
    public static final PsiElementPath SELF;

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
        FIRST_CHILD = any.onFirstChild();
        ALL_CHILDREN_DEEP = any.onDescendants();
        PARENT_ELEMENT = any.onParent();
        SELF = new PsiElementPath(any);
    }

    private final ElementPredicate[] elementReferencePath;

    protected String name;
    protected boolean traceEnabled;

    public PsiElementPath(ElementPredicate... elementReferencePath) {
        this.elementReferencePath = elementReferencePath;
    }

    public final @NotNull PsiElementCollection navigate(@NotNull PsiElement start) {
        return navigate(Collections.singleton(start));
    }

    private static final ThreadLocal<Integer> TRACE_ENABLED = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };
    private static final ThreadLocal<String> TRACE_NAME = new ThreadLocal<String>();

    public final @NotNull PsiElementCollection navigate(@NotNull Collection<PsiElement> start) {
        final int previousValue = TRACE_ENABLED.get();
        final boolean traceEnabled = this.traceEnabled || previousValue > 0;
        String previousName = null;
        if (traceEnabled) {
            previousName = TRACE_NAME.get();
            TRACE_NAME.set(getTraceName());
            TRACE_ENABLED.set(previousValue + 1);
            traceMessage("## [ begin path: %s ]", getTraceName());
        }
        try {
            return navigateImpl(start);
        } finally {
            if (traceEnabled) {
                traceMessage("## [ end path: %s ]", getTraceName());
                TRACE_NAME.set(previousName);
            }
            TRACE_ENABLED.set(previousValue);
        }
    }

    private String getTraceName() {
        if (name == null) {
            String active = TRACE_NAME.get();
            if (active != null) {
                return active;
            }
        }
        return name;
    }
    
    static void traceMessage(@NonNls String format, Object... args) {
        Integer depth = TRACE_ENABLED.get();
        if (depth == 0) return;
        String padding = "\t\t\t\t\t\t\t\t\t\t\t".substring(0, depth);
        String msg = String.format(format, args);
        System.out.println(padding + msg);
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

    public PsiElementPath or(@NotNull PsiElementPath psiPath) {
        return new OrPsiPath(this, psiPath);
    }

    public PsiElementPath exclude(@NotNull PsiElementPath psiPath) {
        return new ExcludePsiPath(psiPath);
    }

    public PsiElementPath append(PsiElementPath next) {
        return new AppendPsiPath(this, next);
    }

    public PsiElementPath append(ElementPredicate... elementReferencePath) {
        if (elementReferencePath.length == 0) return this;
        return append(new PsiElementPath(elementReferencePath));
    }

    public PsiElementPath trace(@NonNls String name) {
        this.name = name;
        this.traceEnabled = true;
        return this;
    }

    private static class OrPsiPath extends PsiElementPath {

        private final Collection<PsiElementPath> delegates;

        private OrPsiPath(PsiElementPath... delegates) {
            this.delegates = new LinkedList<PsiElementPath>(Arrays.asList(delegates));
        }

        @Override
        public PsiElementPath or(@NotNull PsiElementPath psiPath) {
            delegates.add(psiPath);
            return this;
        }

        @NotNull
        @Override
        PsiElementCollection navigateImpl(@NotNull Collection<PsiElement> start) {
            PsiElementCollection buffer = new PsiElementCollection();
            int seq = 0;
            for (PsiElementPath psiPath : delegates) {
                if (TRACE_ENABLED.get() > 0) {
                    String previousName = TRACE_NAME.get();
                    TRACE_NAME.set(previousName + " or[" + (seq++) + "]"); // NON-NLS
                    try {
                        buffer.addAll(psiPath.navigate(start));
                    } finally {
                        TRACE_NAME.set(previousName);
                    }
                } else {
                    buffer.addAll(psiPath.navigateImpl(start));
                }
            }
            return buffer;
        }
    }

    private class ExcludePsiPath extends PsiElementPath {

        private final Collection<PsiElementPath> exclude;

        public ExcludePsiPath(@NotNull PsiElementPath exclude) {
            this.exclude = new LinkedList<PsiElementPath>();
            this.exclude.add(exclude);
        }

        @NotNull
        @Override
        PsiElementCollection navigateImpl(@NotNull Collection<PsiElement> start) {
            PsiElementCollection buffer = PsiElementPath.this.navigateImpl(start);
            int seq = 0;
            for (PsiElementPath psiPath : exclude) {
                traceMessage("## begin exclude...");
                if (TRACE_ENABLED.get() > 0) {
                    String previousName = TRACE_NAME.get();
                    TRACE_NAME.set(previousName + " exclude[" + (seq++) + "]"); // NON-NLS
                    try {
                        buffer.removeAll(psiPath.navigate(start));
                    } finally {
                        TRACE_NAME.set(previousName);
                    }
                } else {
                    buffer.removeAll(psiPath.navigateImpl(start));
                }
                traceMessage("## end exclude.");
                if (buffer.isEmpty()) return PsiElementCollection.EMPTY;
            }
            return buffer;
        }

        @Override
        public PsiElementPath or(@NotNull PsiElementPath psiPath) {
            throw new UnsupportedOperationException("or() not supported after calling seq()");
        }

        @Override
        public PsiElementPath exclude(@NotNull PsiElementPath psiPath) {
            exclude.add(psiPath);
            return this;
        }
    }

    private static class AppendPsiPath extends PsiElementPath {

        private final Collection<PsiElementPath> seq;

        public AppendPsiPath(@NotNull PsiElementPath start, @NotNull PsiElementPath next) {
            seq = new LinkedList<PsiElementPath>();
            seq.add(start);
            seq.add(next);
        }

        @Override
        public PsiElementPath append(@NotNull PsiElementPath next) {
            seq.add(next);
            return this;
        }

        @NotNull
        @Override
        PsiElementCollection navigateImpl(@NotNull Collection<PsiElement> start) {
            PsiElementCollection buffer = new PsiElementCollection(start);
            int seq = 0;
            for (PsiElementPath path : this.seq) {
                if (TRACE_ENABLED.get() > 0) {
                    String previousName = TRACE_NAME.get();
                    TRACE_NAME.set(previousName + " chain[" + (seq++) + "]"); // NON-NLS
                    try {
                        buffer = path.navigate(buffer);
                    } finally {
                        TRACE_NAME.set(previousName);
                    }
                } else {
                    buffer = path.navigateImpl(buffer);
                }
            }
            return buffer;
        }
    }
}
