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

package net.venaglia.nondairy.mocks;

import com.intellij.lang.FileASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.SoyFile;
import org.jetbrains.annotations.NotNull;

/**
* User: ed
* Date: 3/11/12
* Time: 9:49 PM
*/
public class MockSoyFile extends SoyFile {

    private final PsiElement[] children;

    FileASTNode fileASTNode;

    public MockSoyFile(PsiElement[] children, FileViewProvider fileViewProvider) {
        super(fileViewProvider);
        this.children = children;
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return children;
    }

    @Override
    public FileASTNode getNode() {
        return fileASTNode;
    }

    public void setNode(FileASTNode fileASTNode) {
        this.fileASTNode = fileASTNode;
    }
}
