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

import static net.venaglia.nondairy.soylang.elements.path.TraverseEmpty.*;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.util.TinySet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static final AbstractElementPredicate ANY;

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
        AbstractElementPredicate any = new MyAlwaysTruePredicate();
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
                return ANY;
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

    private final TraverseEmpty defaultTraverseEmpty;

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
        this(ABORT, elementReferencePath);
    }

    /**
     * Constructs a new path defined by the passed predicates. Predicates are
     * applied in the order passed.
     *
     * Any or all of the passed predicates my implement
     * {@link TraversalPredicate}. In such cases, the traversal is executed
     * first, then the predicate is applied.
     * @param defaultTraverseEmpty The default traversal rule to use when the
     *     buffer becomes empty.
     * @param elementReferencePath The list of predicates to execute.
     */
    public PsiElementPath(@NotNull TraverseEmpty defaultTraverseEmpty,
                          ElementPredicate... elementReferencePath) {
        if (TraceState.TRACING_ENABLED) {
            for (StackTraceElement ste : new Throwable().getStackTrace()) {
                name = "anonymous at " + ste;
                if (!name.contains(PsiElementPath.class.getName())) {
                    break;
                }
            }
        }
        this.elementReferencePath = elementReferencePath;
        this.defaultTraverseEmpty = defaultTraverseEmpty;
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
        } catch (ProcessCanceledException e) {
            TraceState.summaryMessage("## [ cancelled path: %s ] (%s: %s)", name, e.getClass().getSimpleName(), e.getMessage());
            return PsiElementCollection.EMPTY;
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
        Map<Key,Object> navigationData = null;
        PsiElementCollection current = new PsiElementCollection(start);
        for (int i = 0, j = elementReferencePath.length; i < j; ++i) {
            ElementPredicate next = elementReferencePath[i];

            if (next == null) {
                throw new NullPointerException("elementReferencePath[" + i + "]");
            }
            if (next == LogElementsPredicate.INSTANCE) {
                if (TraceState.isFineEnabled()) {
                    TraceState.fineMessage("\telements = {");
                    for (PsiElement element : current) {
                        TraceState.fineMessage("\t\t%s:'%s'", element, element.getText());
                    }
                    TraceState.fineMessage("\t}");
                }
                continue;
            }
            if (next instanceof InstancePredicate) {
                if (navigationData == null) {
                    navigationData = new HashMap<Key,Object>();
                }
                next = ((InstancePredicate)next).getInstance(navigationData);
            }
            if (current.isEmpty()) {
                if (getNoMatchOnStart(next, defaultTraverseEmpty) == ABORT) {
                    TraceState.detailMessage("\tABORT!");
                    return PsiElementCollection.EMPTY;
                }
            }
            TraverseEmpty noMatch = getNoMatch(next, defaultTraverseEmpty);
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
                } while (buffer.isEmpty() && noMatch == TRAVERSE_AGAIN);
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
                    TraceState.fineMessage("\t\t%s", join(current));
                }
            }
            if (i < j && noMatch == ABORT && current.isEmpty() && PushPopPredicate.isStackEmpty(navigationData)) {
                TraceState.detailMessage("\tABORT!");
                break;
            }
        }
        if (current.isEmpty()) return PsiElementCollection.EMPTY;
        return current;
    }

    @NotNull
    protected TraverseEmpty getNoMatchOnStart(@NotNull Object predicate,
                                              @NotNull TraverseEmpty defaultValue) {
        NoMatchHanding noMatch = predicate.getClass().getAnnotation(NoMatchHanding.class);
        return noMatch == null ? defaultValue : noMatch.onStart();
    }

    @NotNull
    protected TraverseEmpty getNoMatch(@NotNull Object predicate,
                                       @NotNull TraverseEmpty defaultValue) {
        NoMatchHanding noMatch = predicate.getClass().getAnnotation(NoMatchHanding.class);
        return noMatch == null ? defaultValue : noMatch.onNoMatch();
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
                buffer.append(":\'");
                String text = node.getText().replace("\\", "\\\\").replace("\n", "\\n"); // NON_NLS
                if (text.length() > 100) {
                    Matcher m1 = Pattern.compile("^\\s*(.{0,74}\\w)\\b").matcher(text);
                    if (m1.find()) {
                        String head = m1.group(1);
                        String re = "\\b(\\w.{0,96})\\s*$".replace("96", String.valueOf(96 - head.length())); //NON-NLS
                        Matcher m2 = Pattern.compile(re).matcher(text.substring(text.length() - 100));
                        if (m2.find()) {
                            text = head + "..." + m2.group(1);
                        } else {
                            text = head + "..." + text.substring(text.length() - 97 + head.length());
                        }
                    } else {
                        text = text.substring(0,97) + "...";
                    }
                }
                buffer.append(text);
                buffer.append("\'");
            } else {
                buffer.append("[null]"); // NON-NLS
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Builds a view of this path that behaves as a traversal predicate. Each 
     * element passed into the 
     * {@link TraversalPredicate#traverse(java.util.Collection)} method 
     * performs a discrete "forked" navigation.
     * 
     * Normally it is not necessary to fork traversal operations, and doing so
     * may be less efficient. However, it is sometimes necessary when using
     * {@link InstancePredicate}.
     * @return A view of this path that can be used as a traversal predicate.
     */
    public TraversalPredicate asForkingTraversalPredicate() {
        return new TraversalPredicate.AlwaysTrue() {
            @NotNull
            @Override
            public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
                PsiElementCollection result = new PsiElementCollection();
                for (PsiElement element : current) {
                    result.addAll(navigate(element));
                }
                return result;
            }

            @Override
            public boolean test(PsiElement element) {
                return true;
            }

            @Override
            public String toString() {
                return "fork!    "; // NON-NLS
            }
        };
    }
    
    /**
     * Creates a compound predicate, by OR-ing navigation results of this path
     * with navigation results of the passed path. The combined results will
     * retain order stability, but duplicate elements found by executing the
     * additional path will not be added again.
     *
     * This creates a new path, and does not change the logic of this one.
     * @param psiPath The path(s) to combine with this one.
     * @return A new PsiElementPath that will follow multiple simultaneous
     *     paths.
     */
    public PsiElementPath or(@NotNull PsiElementPath... psiPath) {
        PsiElementPath[] args = new PsiElementPath[psiPath.length + 1];
        args[0] = this;
        System.arraycopy(psiPath, 0, args, 1, psiPath.length);
        return new OrPsiPath(args);
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
     * not inherited by paths built using {@link #or(PsiElementPath...)},
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
        public PsiElementPath or(@NotNull PsiElementPath... psiPath) {
            Collections.addAll(delegates, psiPath);
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
                } else if (TraceState.neverTrace()) {
                    buffer.addAll(psiPath.navigateImpl(start));
                } else {
                    buffer.addAll(psiPath.navigate(start));
                }
            }
            return buffer;
        }
    }

    private static class MyAlwaysTruePredicate extends AbstractElementPredicate implements ElementPredicate.AlwaysTrue {

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
        public PsiElementPath or(@NotNull PsiElementPath... psiPath) {
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
        private static final boolean NEVER_TRACE;
        private static final Set<String> TRACING_FOR;

        static {
            boolean tracingPerThread = false;
            boolean tracesDefined = false;
            String names = System.getProperty(TRACE_PATH_PROPERTY_NAME);
            String prefix = TRACE_PATH_PROPERTY_NAME + ".";
            Set<String> implicitNames = new HashSet<String>();
            for (Map.Entry<Object,Object> entry : System.getProperties().entrySet()) {
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
                if (key.startsWith(prefix) && Level.isValidLevel(value)) {
                    implicitNames.add(key.substring(prefix.length()));
                }
            }
            if (names == null) {
                TRACING_FOR = implicitNames.isEmpty() ? Collections.<String>emptySet() : Collections.unmodifiableSet(implicitNames);
            } else if ("*".equals(names)) {
                tracesDefined= true;
                TRACING_FOR = new AllPathsSet();
            } else if (TRACE_PATH_BY_THREAD.equals(names)) {
                tracingPerThread = true;
                TRACING_FOR = new ThreadLocalSet();
            } else {
                Set<String> namesSet = new HashSet<String>(implicitNames);
                for (String name : names.toLowerCase().split("\\|")) {
                    namesSet.add(name.trim());
                }
                namesSet.remove(""); // can't contain the empty string
                tracesDefined = !namesSet.isEmpty();
                TRACING_FOR = Collections.unmodifiableSet(namesSet);
            }
            TRACING_PER_THREAD = tracingPerThread;
            TRACING_ENABLED = TRACING_PER_THREAD || !TRACING_FOR.isEmpty();
            if (TRACING_ENABLED) {
                if (TRACING_PER_THREAD && implicitNames.isEmpty()) {
                    System.out.println("PsiTemplatePath tracing is dynamic, configured per thread"); //NON-NLS
                } else if (TRACING_PER_THREAD) {
                    System.out.println("PsiTemplatePath tracing is dynamic, configured per thread -- ignoring implicit static trace for:"); //NON-NLS
                    for (String name : implicitNames) {
                        System.out.println("\t" + name);
                    }
                } else if (TRACING_FOR.contains(ALL_PATHS)) {
                    System.out.println("PsiTemplatePath tracing is enabled for:"); //NON-NLS
                } else {
                    System.out.println("PsiTemplatePath tracing is enabled for:"); //NON-NLS
                    for (String name : TRACING_FOR) {
                        System.out.println("\t" + name);
                    }
                }
            }
            NEVER_TRACE = !(TRACING_ENABLED && (tracesDefined || tracingPerThread));
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

            public static boolean isValidLevel(String value) {
                try {
                    valueOf(value);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        }

        /**
         * Thread local that always points to the top of the TraceState stack.
         */
        private static final ThreadLocal<TraceState> TRACE_STATE = new ThreadLocal<TraceState>() {
            @Override
            protected TraceState initialValue() {
                return new TraceState(Level.FINE, null, null, -1);
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
         * @return true if trace is not configured for any path
         */
        public static boolean neverTrace() {
            return NEVER_TRACE;
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
                } else if (checkIfTracingEnabled(name)) {
                    String override = System.getProperty(TRACE_PATH_PROPERTY_NAME + "." + name);
                    for (String partialName = name; override == null; ) {
                        int lastBang = partialName.lastIndexOf('!');
                        if (lastBang >= 0) {
                            partialName = partialName.substring(0, lastBang);
                            override = System.getProperty(TRACE_PATH_PROPERTY_NAME + "." + partialName + "!*");
                        } else {
                            break;
                        }
                    }
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

        private static boolean checkIfTracingEnabled(String name) {
            name = name.toLowerCase();
            if (TRACING_FOR.contains(name)) return true;
            int until = name.length();
            int lastBang;
            do {
                lastBang = name.lastIndexOf('!', until - 1);
                if (lastBang >= 0) {
                    if (TRACING_FOR.contains(name.substring(0, lastBang) + "!*")) {
                        return true;
                    }
                    until = lastBang;
                }
            } while (lastBang >= 0);
            return false;
        }

        /**
         * Pushes a new entry on the stack. The specified name is concatenated
         * with the current name, and the new TraceState will receive the same
         * detail level as the current top of hte TraceState stack.
         * 
         * This method is typically used inside compound paths, such as those
         * created by {@link PsiElementPath#or(PsiElementPath...)} or
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
