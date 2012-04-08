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

package net.venaglia.nondairy.soylang.elements;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.mocks.MockProjectEnvironment;
import net.venaglia.nondairy.soylang.SoyFile;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import net.venaglia.nondairy.util.ImmutableCharSequence;
import net.venaglia.nondairy.util.MockProjectEnvironmentRunner;
import net.venaglia.nondairy.util.ProjectFiles;
import net.venaglia.nondairy.util.SourceTuple;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.runner.RunWith;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * User: ed
 * Date: 3/7/12
 * Time: 7:27 PM
 *
 * Base class for tests that exercise the SoyPsiElement and PsiElementPath
 * definitions within them.
 */
@RunWith(MockProjectEnvironmentRunner.class)
@ProjectFiles(files = {"library.soy","render1.soy","render2.soy"})
public abstract class AbstractPsiElementTest {

    private static final AtomicInteger ANONYMOUS_SOURCE_SEQ = new AtomicInteger();
    private static final Pattern ERROR_PATTERN = Pattern.compile("error", Pattern.CASE_INSENSITIVE);

    @SuppressWarnings("HardCodedStringLiteral")
    protected void buildAnonymousTestTemplate(@NotNull @NonNls String name,
                                              @NotNull @NonNls String templateSource,
                                              @NotNull @NonNls String... params) {
        int seq = ANONYMOUS_SOURCE_SEQ.incrementAndGet();
        StringBuilder buffer = new StringBuilder();
        buffer.append("{namespace unit.testing.namespace._").append(seq).append("}\n");
        if (templateSource.contains("{template .") && templateSource.contains("{/template}") && params.length == 0) {
            buffer.append(templateSource).append("\n");
        } else {
            buffer.append("/**\n");
            for (String p : params) {
                buffer.append(" @param ").append(p).append("\n");
            }
            buffer.append(" */\n");
            buffer.append("{template .").append(name).append("}\n");
            buffer.append(templateSource).append("\n");
            buffer.append("{/template}\n");
        }
        SourceTuple tuple = new SourceTuple(name, new ImmutableCharSequence(buffer));
        PsiElementCollection elements = flatten(tuple.psi);
        for (PsiElement element : elements) {
            IElementType type = element.getNode().getElementType();
            assertFalse(SoyToken.ILLEGALS.contains(type));
            assertFalse(ERROR_PATTERN.matcher(type.toString()).find());
        }
    }

    protected PsiElementCollection flatten(@NotNull PsiFile root) {
        PsiElementCollection elements = new PsiElementCollection();
        Deque<PsiElement> stack = new ArrayDeque<PsiElement>();
        stack.push(root);
        while (!stack.isEmpty()) {
            PsiElement node = stack.pop();
            PsiElement[] children = node.getChildren();
            for (int i = children.length - 1; i >= 0; i--) {
                PsiElement child = children[i];
                stack.push(child);
            }
            if (node != root) {
                elements.add(node);
            }
        }
        return elements;
    }

    @NotNull
    protected <T extends PsiElement> PsiElementCollection findElements(@NotNull @NonNls String filename,
                                                                       @Nullable Class<T> type,
                                                                       @Nullable @NonNls String text,
                                                                       @Nullable ElementPredicate predicate) {
        Pattern regex = text == null ? null : Pattern.compile(text, Pattern.LITERAL);
        return findElements(filename, type, regex, predicate);
    }

    @NotNull
    protected <T extends PsiElement> PsiElementCollection findElements(@NotNull @NonNls String filename,
                                                                       @Nullable final Class<T> type,
                                                                       @Nullable final Pattern pattern,
                                                                       @Nullable ElementPredicate predicate) {
        PsiFile root = findRootElement(filename);
        PsiElementCollection elements = flatten(root);
        if (type != null) {
            elements = elements.applyPredicate(new ElementPredicate() {
                @Override
                public boolean test(PsiElement element) {
                    return type.isInstance(element);
                }
            });
        }
        if (pattern != null) {
            elements = elements.applyPredicate(new ElementPredicate() {
                @Override
                public boolean test(PsiElement element) {
                    return pattern.matcher(element.getText()).matches();
                }
            });
        }
        if (predicate != null) {
            elements = elements.applyPredicate(predicate);
        }
        return elements;
    }

    @NotNull
    protected <T extends PsiElement> T findElement(@NotNull @NonNls String filename,
                                                   @NotNull Class<T> type,
                                                   @Nullable @NonNls String text,
                                                   @Nullable ElementPredicate predicate) {
        PsiElementCollection elements = findElements(filename, type, text, predicate);
        assertEquals("Expected to find one element, but didn't", 1, elements.size());
        PsiElement element = elements.oneOrNull();
        assertNotNull(element);
        return type.cast(element);
    }

    @NotNull
    protected <T extends PsiElement> T findNthElement(@NotNull @NonNls String filename,
                                                      @NotNull Class<T> type,
                                                      @Nullable @NonNls String text,
                                                      @Nullable ElementPredicate predicate,
                                                      int n,
                                                      int expectedCount) {
        assertTrue(n > 0);
        assertTrue(expectedCount > 0);
        assertTrue(n <= expectedCount);
        PsiElementCollection elements = findElements(filename, type, text, predicate);
        for (PsiElement element : elements) {
            System.out.println(element);
        }
        assertEquals(expectedCount, elements.size());
        Iterator<PsiElement> iterator = elements.iterator();
        for (int i = 1; i < n; i++) {
            iterator.next();
        }
        return type.cast(iterator.next());
    }

    @NotNull
    protected SoyFile findRootElement(@NotNull @NonNls String filename) {
        PsiFile root = MockProjectEnvironment.findPsiFile(filename);
        assertNotNull(root);
        assertTrue(root instanceof SoyFile);
        return (SoyFile)root;
    }
}
