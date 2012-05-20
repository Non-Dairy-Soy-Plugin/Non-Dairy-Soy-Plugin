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

package net.venaglia.nondairy.util;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.mock.MockDocument;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.LightVirtualFile;
import net.venaglia.nondairy.SoyTestUtil;
import net.venaglia.nondairy.mocks.MockProjectEnvironment;
import net.venaglia.nondairy.mocks.MockSoyFile;
import net.venaglia.nondairy.soylang.SoyFileType;
import net.venaglia.nondairy.soylang.SoyLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* User: ed
* Date: 3/11/12
* Time: 9:53 PM
*/
public class SourceTuple {

    private static final CharSequence SOURCE_AS_RESOURCE = new ImmutableCharSequence("");

    public final String name;
    public final PsiElement root;
    public final PsiFile psi;
    public final VirtualFile file;
    public final String fileUrl;
    public final Document document;

    public SourceTuple(@NonNls String name) {
        this(name, SOURCE_AS_RESOURCE);
    }

    public SourceTuple(@NonNls @NotNull String name, @NonNls @NotNull CharSequence source) {
        this.name = name;
        PsiElement[] children = { null };
        psi = new MockSoyFile(children, new MyFileViewProvider());
        root = source == SOURCE_AS_RESOURCE
                         ? SoyTestUtil.getPsiTreeFor(psi, name)
                         : SoyTestUtil.getPsiTreeFor(psi, name, source);
        children[0] = root;
        String text = root.getText();
        file = new LightVirtualFile(name,
                                    SoyFileType.INSTANCE,
                                    text,
                                    Charset.defaultCharset(),
                                    System.currentTimeMillis());
        fileUrl = file.getUrl();
        document = new MockDocument(text);
        MockProjectEnvironment.add(this);
    }

    private class MyFileViewProvider implements FileViewProvider {

        private Map<Key,Object> userData = new HashMap<Key,Object>();

        @NotNull
        @Override
        public PsiManager getManager() {
            return MockProjectEnvironment.getUnitTestPsiManager();
        }

        @Override
        public Document getDocument() {
            return document;
        }

        @NotNull
        @Override
        public CharSequence getContents() {
            return root.getText();
        }

        @NotNull
        @Override
        public VirtualFile getVirtualFile() {
            return file;
        }

        @NotNull
        @Override
        public Language getBaseLanguage() {
            return SoyLanguage.INSTANCE;
        }

        @NotNull
        @Override
        public Set<Language> getLanguages() {
            return Collections.<Language>singleton(SoyLanguage.INSTANCE);
        }

        @Override
        public PsiFile getPsi(@NotNull Language target) {
            return psi;
        }

        @NotNull
        @Override
        public List<PsiFile> getAllFiles() {
            return Collections.singletonList(psi);
        }

        @Override
        public boolean isEventSystemEnabled() {
            return true;
        }

        @Override
        public boolean isPhysical() {
            return true;
        }

        @Override
        public long getModificationStamp() {
            return file.getModificationStamp();
        }

        @Override
        public boolean supportsIncrementalReparse(@NotNull Language rootLanguage) {
            return false;
        }

        @Override
        public void rootChanged(@NotNull PsiFile psiFile) {
        }

        @Override
        public void beforeContentsSynchronized() {
        }

        @Override
        public void contentsSynchronized() {
        }

        @SuppressWarnings("CloneDoesntCallSuperClone")
        @Override
        public FileViewProvider clone() {
            return new MyFileViewProvider();
        }

        @Override
        public PsiElement findElementAt(int offset) {
            ASTNode node = root.getNode();
            if (offset >= node.getTextLength()) {
                return null;
            }
            boolean foundChild = true;
            while (foundChild) {
                foundChild = false;
                for (ASTNode child : node.getChildren(null)) {
                    int startOffset = child.getStartOffset();
                    if (offset >= startOffset && offset < startOffset + child.getTextLength()) {
                        node = child;
                        foundChild = true;
                        break;
                    }
                }
            }
            return node.getPsi();
        }

        @Override
        public PsiReference findReferenceAt(int offset) {
            PsiElement element = findElementAt(offset);
            return element == null ? null : element.getReference();
        }

        @Override
        public PsiElement findElementAt(int offset, @NotNull Language language) {
            if (language == Language.ANY || language.equals(SoyLanguage.INSTANCE)) {
                return findElementAt(offset);
            }
            return null;
        }

        @Override
        public PsiElement findElementAt(int offset, @NotNull Class<? extends Language> lang) {
            if (SoyLanguage.class.isAssignableFrom(lang)) {
                return findElementAt(offset);
            }
            return null;
        }

        @Override
        public PsiReference findReferenceAt(int offsetInElement, @NotNull Language language) {
            if (language == Language.ANY || language.equals(SoyLanguage.INSTANCE)) {
                return findReferenceAt(offsetInElement);
            }
            return null;
        }

        @NotNull
        @Override
        public FileViewProvider createCopy(@NotNull VirtualFile copy) {
            return new MyFileViewProvider();
        }

        @Override
        public <T> T getUserData(@NotNull Key<T> key) {
            return key.get(userData);
        }

        @Override
        public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
            userData.put(key, value);
        }
    }
}
