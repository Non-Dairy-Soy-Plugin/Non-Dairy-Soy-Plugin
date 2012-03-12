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

import net.venaglia.nondairy.mocks.MockProjectEnvironment;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * User: ed
 * Date: 3/11/12
 * Time: 10:17 PM
 */
public class MockProjectEnvironmentRunner extends BlockJUnit4ClassRunner {

    private final ProjectFiles projectFiles;

    public MockProjectEnvironmentRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        projectFiles = testClass.getAnnotation(ProjectFiles.class);
    }

    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (String name : projectFiles.value()) {
                    new SourceTuple(name);
                }
                if (PsiElementPath.TraceState.isDebugPerThread()) {
                    TracePsiElementPath trace = method.getMethod().getAnnotation(TracePsiElementPath.class);
                    if (trace == null) {
                        trace = getTestClass().getJavaClass().getAnnotation(TracePsiElementPath.class);
                    }
                    if (trace != null) {
                        PsiElementPath.TraceState.enableDebugFor(trace.value());
                    } else {
                        PsiElementPath.TraceState.enableDebugFor();
                    }
                }
                runChildImpl(method, notifier);
            }
        };
        MockProjectEnvironment.get().runWith(runnable);
    }

    private void runChildImpl(FrameworkMethod method, RunNotifier notifier) {
        super.runChild(method, notifier);
    }
}
