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
import net.venaglia.nondairy.mocks.MockProjectEnvironment;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.util.MockProjectEnvironmentRunner;
import net.venaglia.nondairy.util.ProjectFiles;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
@ProjectFiles({"library.soy","render1.soy","render2.soy","minimal.soy","delegates.soy"})
public abstract class AbstractPsiElementTest {

    private Map<String,PsiFile> rootPsiElementsByTestFile;

    @Before
    public void setup() {
        rootPsiElementsByTestFile = new HashMap<String,PsiFile>();
        for (String filename : getClass().getAnnotation(ProjectFiles.class).value()) {
            PsiFile root = MockProjectEnvironment.findPsiFile(filename);
            assertNotNull(root);
            rootPsiElementsByTestFile.put(filename, root);
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
        PsiFile root = rootPsiElementsByTestFile.get(filename);
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
        assertTrue(n >= 0);
        assertTrue(expectedCount > 0);
        assertTrue(n < expectedCount);
        PsiElementCollection elements = findElements(filename, type, text, predicate);
        for (PsiElement element : elements) {
            System.out.println(element);
        }
        assertEquals(expectedCount, elements.size());
        Iterator<PsiElement> iterator = elements.iterator();
        for (int i = 0; i < n; i++) {
            iterator.next();
        }
        return type.cast(iterator.next());
    }

    protected PsiFile findRootElement(@NotNull @NonNls String filename) {
        return rootPsiElementsByTestFile.get(filename);
    }
}
