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

import com.intellij.mock.MockModule;
import com.intellij.mock.MockProject;
import com.intellij.mock.MockPsiManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import net.venaglia.nondairy.SoyTestUtil;
import net.venaglia.nondairy.soylang.cache.SoyCacheUpdater;
import net.venaglia.nondairy.util.SourceTuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: ed
 * Date: 2/27/12
 * Time: 8:17 AM
 *
 * This class contains a mock project object, a cache, and several parsed
 * files, suitable for testing tree navigation functions such as those used by
 * SoyPsiElementReference.
 */
public class MockProjectEnvironment {

    private static ThreadLocal<WeakReference<MockProjectEnvironment>> CURRENT_ENVIRONMENT
            = new ThreadLocal<WeakReference<MockProjectEnvironment>>();

    private Project project;
    private PsiManager psiManager;
    private Module module;
    private ProjectFileIndex fileIndex;
    private SoyCacheUpdater soyCacheUpdater;
    private Map<String,SourceTuple> parsedFiles;

    private final AtomicBoolean initialized = new AtomicBoolean();

    static {
        SoyTestUtil.init();
    }

    private void init() {
        if (!initialized.getAndSet(true)) {
            project = new MockProject(null, new MockDisposable());
            psiManager = new MockPsiManager(project) {
                @Override
                public PsiFile findFile(@NotNull VirtualFile file) {
                    SourceTuple tuple = MockProjectEnvironment.getTuple(file);
                    return tuple == null ? null : tuple.psi;
                }
            };
            module = new MockModule(project, new MockDisposable());
            fileIndex = new MockProjectFileIndex();
            parsedFiles = new HashMap<String,SourceTuple>();
            soyCacheUpdater = new SoyCacheUpdater(project);
        }
    }

    public void runWith(Runnable runnable) {
        try {
            CURRENT_ENVIRONMENT.set(new WeakReference<MockProjectEnvironment>(this));
            init();
            runnable.run();
        } finally {
            CURRENT_ENVIRONMENT.remove();
        }
    }

    private void addSourceTuple(SourceTuple tuple) {
        parsedFiles.put(tuple.name, tuple);
        soyCacheUpdater.updateCache(tuple.file);
    }

    @NotNull
    public static MockProjectEnvironment get() {
        WeakReference<MockProjectEnvironment> ref = CURRENT_ENVIRONMENT.get();
        MockProjectEnvironment environment = ref == null ? null : ref.get();
        if (environment == null) {
            environment = new MockProjectEnvironment();
            ref = new WeakReference<MockProjectEnvironment>(environment);
            CURRENT_ENVIRONMENT.set(ref);
            environment.init();
        }
        return environment;
    }

    @Nullable
    public static SourceTuple getTuple(@NotNull VirtualFile file) {
        for (SourceTuple tuple : get().parsedFiles.values()) {
            if (tuple.file.equals(file)) {
                return tuple;
            }
        }
        return null;
    }

    @Nullable
    public static PsiFile findPsiFile(String filename) {
        Map<String,SourceTuple> files = get().parsedFiles;
        SourceTuple tuple = files == null ? null : files.get(filename);
        return tuple == null ? null : tuple.psi;
    }

    public static PsiManager getUnitTestPsiManager() {
        return get().psiManager;
    }

    public static Project getUnitTestProject() {
        return get().project;
    }

    public static Module getUnitTestModule() {
        return get().module;
    }

    public static ProjectFileIndex getUnitTestProjectFileIndex() {
        return get().fileIndex;
    }

    public static void add(SourceTuple tuple) {
        get().addSourceTuple(tuple);
    }
}
