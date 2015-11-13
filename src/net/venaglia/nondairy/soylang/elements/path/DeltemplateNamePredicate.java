/*
 * Copyright 2010 - 2014 Ed Venaglia
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

import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.elements.DelegateTemplateDefElement;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: 1/23/12
 * Time: 5:20 PM
 *
 * An ElementPredicate that matches soy tags that implement
 * {@link net.venaglia.nondairy.soylang.elements.TemplateMemberElement} and have a particular template name.
 */
public class DeltemplateNamePredicate extends AbstractElementPredicate {

    private final String myDeltemplateName;

    public DeltemplateNamePredicate(@NotNull String myDeltemplateName) {
        this.myDeltemplateName = myDeltemplateName;
    }

    @Override
    public boolean test(PsiElement element) {
        if (element instanceof DelegateTemplateDefElement) {
            String name = ((DelegateTemplateDefElement)element).getTemplateName();
            return myDeltemplateName.equals(name);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[deltemplateName=" + myDeltemplateName + "]"; //NON-NLS
    }
}
