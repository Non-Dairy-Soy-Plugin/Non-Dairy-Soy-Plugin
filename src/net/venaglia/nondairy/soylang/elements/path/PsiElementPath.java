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

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.util.TinySet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * User: ed
 * Date: Aug 26, 2010
 * Time: 7:28:11 AM
 * 
 * This utility is used to navigate the PSI tree in a way that is easy to
 * define and debug.
 *
 * Further system properties must be set on the Java command line to enable
 * the debug output. See javadoc on {@link #debug(String)} for more
 * information.
 */
public class PsiElementPath {

    private static final Logger LOG = Logger.getInstance(PsiElementPath.class);

    /**
     * Use this object when you wish to match any element in a PsiPath.
     */
    public static final ElementPredicate ANY;

    /**
     * Use this object when you wish to match no elements in a PsiPath.
     */
    public static final ElementPredicate NONE;

    /**
     * Use this object when you wish to reference the direct children in a
     * PsiPath.
     */
    public static final TraversalPredicate ALL_CHILDREN;

    /**
     * Use this object when you wish to reference the direct children in a
     * PsiPath.
     */
    public static final TraversalPredicate FIRST_CHILD;

    /**
     * Use this object when you wish to reference the all descendant children
     * in a PsiPath.
     */
    public static final TraversalPredicate ALL_CHILDREN_DEEP;

    /**
     * Use this object when you wish to reference the parent element in a
     * PsiPath.
     */
    public static final TraversalPredicate PARENT_ELEMENT;

    /**
     * Use this object when you wish to build a path that returns the original
     * node.
     */
    public static final PsiElementPath SELF;

    /**
     * Use this object when you wish to build a path that returns the original
     * node.
     */
    public static final PsiElementPath EMPTY;

    /**
     * System property that may be set to enable logging of PsiElementPath
     * traversal operations.
     *
     * The system property may be set to "*" to allow logging of ALL path
     * traversal operations. This will result in extremely verbose output.
     */
    @NonNls
    public static final String TRACE_PATH_PROPERTY_NAME = "net.venaglia.nondairy.path.debug";

    /**
     * System property value for {@link PsiElementPath#TRACE_PATH_PROPERTY_NAME}
     * that will allow dynamic configuration of which path traversals will be
     * logged.
     */
    @NonNls
    public static final String TRACE_PATH_BY_THREAD = "[dyanmic - per thread]";

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

            @Override
            public AbstractElementPredicate not() {
                return (AbstractElementPredicate)NONE;
            }
        };
        AbstractElementPredicate none = new AbstractElementPredicate() {
            @Override
            public boolean test(PsiElement element) {
                return false;
            }

            @Override
            public String toString() {
                return "nil"; //NON-NLS
            }

            @Override
            public AbstractElementPredicate not() {
                return (AbstractElementPredicate)ANY;
            }
        };
        ANY = any;
        NONE = none;
        ALL_CHILDREN = any.onChildren();
        FIRST_CHILD = any.onFirstChild();
        ALL_CHILDREN_DEEP = any.onAllDescendants();
        PARENT_ELEMENT = any.onParent();
        SELF = new PsiElementPath(any);
        EMPTY = new PsiElementPath(none);
    }

    private final ElementPredicate[] elementReferencePath;

    /** name, used for debugging */
    @NonNls
    protected String name;

    /**
     * Constructs a new path defined by the passed predicates. Predicates are
     * applied in the order passed.
     * 
     * Any or all of the passed predicates my implement
     * {@link TraversalPredicate}. In such cases, the traversal is executed
     * first, then the predicate is applied.
     * @param elementReferencePath The list of predicates to execute.
     */
    public PsiElementPath(ElementPredicate... elementReferencePath) {
        if (TraceState.TRACING_ENABLED) {
            for (StackTraceElement ste : new Throwable().getStackTrace()) {
                name = "anonymous at " + ste;
                if (!name.contains(PsiElementPath.class.getName())) {
                    break;
                }
            }
        }
        this.elementReferencePath = elementReferencePath;
    }

    /**
     * Executes navigation starting from the passed element.
     * @param start Where to start
     * @return A collection of elements containing the results of navigation.
     */
    public final @NotNull PsiElementCollection navigate(@NotNull PsiElement start) {
        return navigate(Collections.singleton(start));
    }

    /**
     * Executes navigation starting from the passed elements.
     * @param start Elements indicating where navigation should start
     * @return A collection of elements containing the results of navigation.
     */
    public final @NotNull PsiElementCollection navigate(@NotNull Collection<PsiElement> start) {
        TraceState.push(name);
        String name = null;
        if (TraceState.isSummaryEnabled()) {
            name = TraceState.getName();
            TraceState.summaryMessage("## [ begin path: %s ]", name);
        }
        try {
            return navigateImpl(start);
        } catch (RuntimeException e) {
            LOG.error(e);
            TraceState.summaryMessage("## [ abort path: %s ] (%s: %s)", name, e.getClass().getSimpleName(), e.getMessage());
            return PsiElementCollection.EMPTY;
        } finally {
            if (TraceState.isSummaryEnabled()) {
                TraceState.summaryMessage("## [ end path: %s ]", name);
            }
            TraceState.pop();
        }
    }

    @NotNull PsiElementCollection navigateImpl(@NotNull Collection<PsiElement> start) {
        if (start.isEmpty()) {
            TraceState.detailMessage("\tABORT!");
            return PsiElementCollection.EMPTY;
        }
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
            if (TraceState.isDetailEnabled()) {
                TraceState.detailMessage("\t%s (%d %s)",
                                         next,
                                         current.size(),
                                         current.size() == 1 ? "element" : "elements"); //NON-NLS
                if (!current.isEmpty() && TraceState.isFineEnabled()) {
                    TraceState.fineMessage("\t\t" + join(current));
                }
            }
            if (i < j && current.isEmpty()) {
                TraceState.detailMessage("\tABORT!");
            }
        }
        if (current.isEmpty()) return PsiElementCollection.EMPTY;
        return current;
    }

    private String join(@NotNull Collection<PsiElement> current) {
        StringBuilder buffer = new StringBuilder(current.size() * 16);
        buffer.append("[");
        boolean first = true;
        for (PsiElement element : current) {
            ASTNode node = element.getNode();
            if (first) first = false; else buffer.append(",");
            if (node != null && node.getElementType() != null) {
                buffer.append(node.getElementType());
//                buffer.append("=\"");
//                buffer.append(node.getText());
//                buffer.append("\"");
            } else {
                buffer.append("[null]"); // NON-NLS
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Creates a compound predicate, by OR-ing navigation results of this path
     * with navigation results of the passed path. The combined results will
     * retain order stability, but duplicate elements found by executing the
     * additional path will not be added again.
     *
     * This creates a new path, and does not change the logic of this one.
     * @param psiPath The path to combine with this one.
     * @return A new PsiElementPath that will follow multiple simultaneous
     *     paths.
     */
    public PsiElementPath or(@NotNull PsiElementPath psiPath) {
        return new OrPsiPath(this, psiPath);
    }

    /**
     * Create a compound predicate, by omitting navigation results of the
     * passed path, from the result of this one.
     *
     * This creates a new path, and does not change the logic of this one.
     * @param psiPath The path to combine with this one.
     * @return A new PsiElementPath that will follow multiple simultaneous
     *     paths.
     */
    public PsiElementPath exclude(@NotNull PsiElementPath psiPath) {
        return new ExcludePsiPath(psiPath);
    }

    /**
     * Builds a new PsiElementPath that will follow the navigation logic of
     * this path, then continue navigation using the passed path.
     *
     * This creates a new path, and does not change the logic of this one.
     * @param next The path to append to this one.
     * @return A new PsiElementPath that will follow multiple simultaneous
     *     paths.
     */
    public PsiElementPath append(PsiElementPath next) {
        return new AppendPsiPath(this, next);
    }

    /**
     * Builds a new PsiElementPath that will follow the navigation logic of
     * this path, then continue navigation using the passed element predicates
     * in the order passed.
     *
     * Any or all of the passed predicates my implement
     * {@link TraversalPredicate}. In such cases, the traversal is executed
     * first, then the predicate is applied.
     *
     * This creates a new path, and does not change the logic of this one.
     * @param elementReferencePath The path to append to this one.
     * @return A new PsiElementPath that will follow multiple simultaneous
     *     paths.
     */
    public PsiElementPath append(ElementPredicate... elementReferencePath) {
        if (elementReferencePath.length == 0) return this;
        return append(new PsiElementPath(elementReferencePath));
    }

    /**
     * Identifies the name of this path for debugging purposes. This name is
     * not inherited by paths built using {@link #or(PsiElementPath)},
     * {@link #exclude(PsiElementPath)}, {@link #append(PsiElementPath)}, or
     * {@link #append(ElementPredicate...)}.
     *
     * To enable debugging, first call this method:
     *     myPath.debug("abc_xyz");
     * Then add this system property to the run configuration:
     *     net.venaglia.nondairy.path.debug=abc_xyz
     * If you want more control you can also set:
     *     net.venaglia.nondairy.path.debug.abc_xyz=SUMMARY
     *
     * Valid detail levels are:
     *     INHERIT, NONE, SUMMARY, DETAIL, FINE
     * Default detail level for names not explicitly called out in
     * net.venaglia.nondairy.path.debug is INHERIT.
     * @param name The name of this path, to identify it to the debugging
     *     mechanism.
     * @return This path object, suitable for chaining this call during
     *     creation.
     */
    public PsiElementPath debug( @NotNull @NonNls String name) {
        this.name = name;
        return this;
    }

    /**
     * Inner sub-class to handle the OR-ing of two or more PsiElementPath
     * objects.
     */
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
                if (TraceState.traceActive()) {
                    TraceState.pushName("or[" + (seq++) + "]");
                    try {
                        buffer.addAll(psiPath.navigate(start));
                    } finally {
                        TraceState.pop();
                    }
                } else {
                    buffer.addAll(psiPath.navigateImpl(start));
                }
            }
            return buffer;
        }
    }

    /**
     * Inner sub-class to handle the exclusion of one or more PsiElementPath
     * objects.
     */
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
                TraceState.summaryMessage("## begin exclude...");
                if (TraceState.traceActive()) {
                    TraceState.pushName("exclude[" + (seq++) + "]");
                    try {
                        buffer.removeAll(psiPath.navigate(start));
                    } finally {
                        TraceState.pop();
                    }
                } else {
                    buffer.removeAll(psiPath.navigateImpl(start));
                }
                TraceState.summaryMessage("## end exclude.");
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

    /**
     * Inner sub-class to handle the chaining of two or more PsiElementPath
     * objects.
     */
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
                if (TraceState.traceActive()) {
                    TraceState.pushName("chain[" + (seq++) + "]");
                    try {
                        buffer = path.navigate(buffer);
                    } finally {
                        TraceState.pop();
                    }
                } else {
                    buffer = path.navigateImpl(buffer);
                }
            }
            return buffer;
        }
    }

    /**
     * Inner class used to trace path execution.
     *
     * Usage of this class is typically through static methods, while
     * instances are chained to form a unidirectional linked list. This
     * chain forms a stack, with the top of this stack being available
     * through a {@link ThreadLocal}.
     */
    public static class TraceState {

        @NonNls
        public static final String ALL_PATHS = "[all paths!]";

        private static final boolean TRACING_ENABLED;
        private static final boolean TRACING_PER_THREAD;
        private static final Set<String> TRACING_FOR;

        static {
            boolean tracingPerThread = false;
            String names = System.getProperty(TRACE_PATH_PROPERTY_NAME);
            if (names == null) {
                TRACING_FOR = Collections.emptySet();
            } else if ("*".equals(names)) {
                TRACING_FOR = new AllPathsSet();
            } else if (TRACE_PATH_BY_THREAD.equals(names)) {
                tracingPerThread = true;
                TRACING_FOR = new ThreadLocalSet();
            } else {
                Set<String> namesSet = new HashSet<String>();
                for (String name : names.toLowerCase().split("\\|")) {
                    namesSet.add(name.trim());
                }
                namesSet.remove(""); // can't contain the empty string
                TRACING_FOR = Collections.unmodifiableSet(namesSet);
            }
            TRACING_PER_THREAD = tracingPerThread;
            TRACING_ENABLED = TRACING_PER_THREAD || !TRACING_FOR.isEmpty();
            if (TRACING_ENABLED) {
                if (TRACING_PER_THREAD) {
                    System.out.println("PsiTemplatePath tracing is dynamic, configured per thread"); //NON-NLS
                } else if (TRACING_FOR.contains(ALL_PATHS)) {
                    System.out.println("PsiTemplatePath tracing is enabled for:"); //NON-NLS
                } else {
                    System.out.println("PsiTemplatePath tracing is enabled for:"); //NON-NLS
                    for (String name : TRACING_FOR) {
                        System.out.println("\t" + name);
                    }
                }
            }
        }

        /**
         * Enumerates the detail levels supported by this mechanism.
         */
        public enum Level {
            INHERIT, NONE, SUMMARY(NONE), DETAIL, FINE(DETAIL);
            
            final Level nextAfterInherit;

            private Level() {
                this.nextAfterInherit = this;
            }

            private Level(Level nextAfterInherit) {
                this.nextAfterInherit = nextAfterInherit;
            }
        }

        /**
         * Thread local that always points to the top of the TraceState stack.
         */
        private static final ThreadLocal<TraceState> TRACE_STATE = new ThreadLocal<TraceState>() {
            @Override
            protected TraceState initialValue() {
                return new TraceState(Level.NONE, null, null, -1);
            }
        };

        /** The detail level being logged at this place in the stack. */
        private final Level level;

        /** The TraceState under this one in the stack, or null if this is the bottom entry */
        private final TraceState before;

        /** The debug name that applies at this place in the stack */
        private final String name;

        /** How TraceStates under this one have debugging enabled */
        private final int depth;

        private TraceState(Level level, TraceState before, String name, int depth) {
            this.level = level;
            this.before = before;
            this.name = name == null ? "(?)" : name;
            this.depth = depth;
        }

        /**
         * @return true if path tracing is currently active and debug output
         *     should be generated.
         */
        public static boolean traceActive() {
            return TRACING_ENABLED && TRACE_STATE.get().level != Level.NONE;
        }

        /**
         * @return The debug name of the current path navigation execution, if
         *     it is known.
         */
        public static String getName() {
            return TRACING_ENABLED ? TRACE_STATE.get().name : "(?)";
        }

        /**
         * Pushes a new entry on the stack. If the specified name overrides
         * debug settings, those settings take effect. If no debug override is
         * explicitly set for the passed name, the debug level is inherited
         * from the current top of the TraceState stack.
         * @param name The debug name to be pushed on the stack.
         */
        public static void push(@Nullable @NonNls String name) {
            if (TRACING_ENABLED) {
                Level level;
                if (name == null || name.startsWith("anonymous at ")) {
                    level = Level.INHERIT;
                } else if (TRACING_FOR.contains(name.toLowerCase())) {
                    String override = System.getProperty(TRACE_PATH_PROPERTY_NAME + "." + name);
                    try {
                        level = override != null ? Level.valueOf(override.toUpperCase()) : Level.DETAIL;
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        level = Level.DETAIL;
                    }
                } else {
                    level = Level.NONE;
                }
                TraceState before = TRACE_STATE.get();
                Level nextLevel = level == Level.INHERIT ? before.level.nextAfterInherit : level;
                int nextDepth = before.depth + (nextLevel == Level.NONE ? 0 : 1);
                TRACE_STATE.set(new TraceState(nextLevel, before, name, nextDepth));
            }
        }

        /**
         * Pushes a new entry on the stack. The specified name is concatenated
         * with the current name, and the new TraceState will receive the same
         * detail level as the current top of hte TraceState stack.
         * 
         * This method is typically used inside compound paths, such as those
         * created by {@link PsiElementPath#or(PsiElementPath)} or
         * {@link PsiElementPath#exclude(PsiElementPath)}.
         * @param name The debug name to be pushed on the stack.
         */
        public static void pushName(@NotNull @NonNls String name) {
            if (TRACING_ENABLED) {
                TraceState before = TRACE_STATE.get();
                Level nextLevel = before.level;
                int nextDepth = before.depth;
                String newName = before.name != null ? before.name + " " + name : name;
                TRACE_STATE.set(new TraceState(nextLevel, before, newName, nextDepth));
            }
        }

        /**
         * Discards the top entry of the stack, reverting the current top-most 
         * TraceState to its previous value.
         */
        public static void pop() {
            if (TRACING_ENABLED) {
                TraceState state = TRACE_STATE.get();
                if (state.before != null) {
                    TRACE_STATE.set(state.before);
                }
            }
        }

        private static boolean isLevelEnabled(@NotNull Level level) {
            return TRACING_ENABLED && level != Level.NONE &&
                   level.ordinal() <= TRACE_STATE.get().level.ordinal();
        }
        
        private static void levelMessage(@NotNull Level level, @NotNull @NonNls String format, Object[] args) {
            if (TRACING_ENABLED) {
                TraceState state = TRACE_STATE.get();
                if (state.level == Level.NONE || level.ordinal() > state.level.ordinal()) {
                    return;
                }
                String padding = "\t\t\t\t\t\t\t\t\t\t\t".substring(0, state.depth);
                String msg = String.format(format, args);
                System.out.println(padding + msg);
            }
        }

        /**
         * @return true is debug messages should be created at a level of
         *     detail of {@link Level#SUMMARY} or higher.
         */
        public static boolean isSummaryEnabled() {
            return isLevelEnabled(Level.SUMMARY);
        }

        /**
         * Records the specified formatted message to the debug log, if the 
         * current level of detail is at {@link Level#SUMMARY} or higher.
         * @param format The format string.
         * @param args arguments to the format string.
         * @see String#format(String, Object...)
         */
        public static void summaryMessage(@NotNull @NonNls String format, Object... args) {
            levelMessage(Level.SUMMARY, format, args);
        }

        /**
         * @return true is debug messages should be created at a level of
         *     detail of {@link Level#DETAIL} or higher.
         */
        public static boolean isDetailEnabled() {
            return isLevelEnabled(Level.DETAIL);
        }

        /**
         * Records the specified formatted message to the debug log, if the
         * current level of detail is at {@link Level#DETAIL} or higher.
         * @param format The format string.
         * @param args arguments to the format string.
         * @see String#format(String, Object...)
         */
        public static void detailMessage(@NotNull @NonNls String format, Object... args) {
            levelMessage(Level.DETAIL, format, args);
        }

        /**
         * @return true is debug messages should be created at a level of
         *     detail of {@link Level#FINE} or higher.
         */
        public static boolean isFineEnabled() {
            return isLevelEnabled(Level.FINE);
        }

        /**
         * Records the specified formatted message to the debug log, if the
         * current level of detail is at {@link Level#FINE} or higher.
         * @param format The format string.
         * @param args arguments to the format string.
         * @see String#format(String, Object...)
         */
        public static void fineMessage(@NotNull @NonNls String format, Object... args) {
            levelMessage(Level.FINE, format, args);
        }

        /**
         * @return true if path navigation tracing can be changed dynamically,
         *     per thread, false otherwise.
         */
        public static boolean isDebugPerThread() {
            return TRACING_PER_THREAD;
        }

        /**
         * @param names The names to enable debugging for
         * @throws IllegalStateException if path navigation cannot be changed
         *     dynamically
         */
        public static void enableDebugFor(String... names) {
            if (!TRACING_PER_THREAD) {
                throw new IllegalStateException();
            }
            if (names.length > 0) {
                Set<String> nameSet = new HashSet<String>(Arrays.asList(names));
                if (nameSet.contains(ALL_PATHS)) {
                    nameSet = new AllPathsSet();
                }
                ThreadLocalSet.SET.set(nameSet);
                System.out.println("PsiTemplatePath tracing is enabled for (" + Thread.currentThread() + "):"); //NON-NLS
                for (String name : TRACING_FOR) {
                    System.out.println("\t" + name);
                }
            } else {
                System.out.println("PsiTemplatePath tracing is disabled (" + Thread.currentThread() + ")"); //NON-NLS
                ThreadLocalSet.SET.remove();
            }
        }

        /**
         * This class is used when debugging all path navigation operations
         */
        private static class AllPathsSet extends TinySet<String> {

            private AllPathsSet() {
                this.add(ALL_PATHS);
            }

            @Override
            public boolean contains(Object o) {
                return true;
            }
        }

        /**
         * This class is used by the TraceState when tracing can be configured
         * dynamically
         */
        private static class ThreadLocalSet extends AbstractSet<String> {

            static ThreadLocal<Set<String>> SET = new ThreadLocal<Set<String>>();
            
            @Override
            public Iterator<String> iterator() {
                Set<String> set = SET.get();
                return set == null ? Collections.<String>emptySet().iterator() : set.iterator();
            }

            @Override
            public int size() {
                Set<String> set = SET.get();
                return set == null ? 0 : set.size();
            }

            @Override
            public boolean contains(Object o) {
                Set<String> set = SET.get();
                return set != null && (set.contains(o) || set.contains("*"));
            }
        }
    }
}
