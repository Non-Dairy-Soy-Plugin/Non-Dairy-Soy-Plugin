/*
 * Copyright 2012 Ed Venaglia
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

import static net.venaglia.nondairy.soylang.SoyElement.*;

import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: 1/17/12
 * Time: 8:31 AM
 */
public interface TemplateMemberElement {

    static final PsiElementPath PATH_TO_CONTAINING_TEMPLATE_NAME =
            new PsiElementPath(new ElementTypePredicate(tag_and_doc_comment).onFirstAncestor(),
                               new ElementTypePredicate(template_tag).onChildren(),
                               new ElementTypePredicate(tag_between_braces).onChildren(),
                               new ElementTypePredicate(template_name).onChildren());

    static final PsiElementPath PATH_TO_NAMESPACE_NAME =
            new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onFirstAncestor(),
                               new ElementTypePredicate(SoyElement.namespace_def).onChildren(),
                               new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                               new ElementTypePredicate(SoyElement.namespace_name).onChildren());

    /**
     * @return The fully qualified name of this template
     */
    @Nullable
    String getTemplateName();

    /**
     * @return The fully qualified name of the namespace in which the template
     *     is defined.
     */
    @Nullable
    String getNamespace();
}
