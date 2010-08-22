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

package net.venaglia.nondairy;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileTypeManager;
import net.venaglia.nondairy.soylang.SoyFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 6, 2010
 * Time: 8:49:36 PM
 */
public class NonDairyMain implements ApplicationComponent {

    public NonDairyMain() {
    }

    public void initComponent() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                FileTypeManager.getInstance().registerFileType(SoyFileType.INSTANCE, "soy");
            }
        });
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "NonDairySoyPlugin";
    }

}
