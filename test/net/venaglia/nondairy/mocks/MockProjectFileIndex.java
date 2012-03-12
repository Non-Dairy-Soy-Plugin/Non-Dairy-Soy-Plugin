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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * User: ed
 * Date: 3/11/12
 * Time: 9:50 PM
 */
public class MockProjectFileIndex implements ProjectFileIndex {

    @Override
    public Module getModuleForFile(@NotNull VirtualFile file) {
        return MockProjectEnvironment.getUnitTestModule();
    }

    @NotNull
    @Override
    public List<OrderEntry> getOrderEntriesForFile(@NotNull VirtualFile file) {
        return Collections.emptyList();
    }

    @Override
    public VirtualFile getClassRootForFile(@NotNull VirtualFile file) {
        return null;
    }

    @Override
    public VirtualFile getSourceRootForFile(@NotNull VirtualFile file) {
        return null;
    }

    @Override
    public VirtualFile getContentRootForFile(@NotNull VirtualFile file) {
        return null;
    }

    @Override
    public String getPackageNameByDirectory(@NotNull VirtualFile dir) {
        return null;
    }

    @Override
    public boolean isLibraryClassFile(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public boolean isInSource(@NotNull VirtualFile fileOrDir) {
        return false;
    }

    @Override
    public boolean isInLibraryClasses(@NotNull VirtualFile fileOrDir) {
        return false;
    }

    @Override
    public boolean isInLibrarySource(@NotNull VirtualFile fileOrDir) {
        return false;
    }

    @Override
    public boolean isIgnored(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public boolean iterateContent(@NotNull ContentIterator iterator) {
        return false;
    }

    @Override
    public boolean iterateContentUnderDirectory(@NotNull VirtualFile dir,
                                                @NotNull ContentIterator iterator) {
        return false;
    }

    @Override
    public boolean isInContent(@NotNull VirtualFile fileOrDir) {
        return false;
    }

    @Override
    public boolean isContentJavaSourceFile(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public boolean isInSourceContent(@NotNull VirtualFile fileOrDir) {
        return false;
    }

    @Override
    public boolean isInTestSourceContent(@NotNull VirtualFile fileOrDir) {
        return false;
    }
}
