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

import static net.venaglia.nondairy.soylang.SoyElement.*;

import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: 1/17/12
 * Time: 8:31 AM
 *
 * Interface to be implemented by SoyPsiElements that live within a soy
 * template.
 */
public interface TemplateMemberElement extends NamespaceMemberElement {

    static final PsiElementPath PATH_TO_CONTAINING_TEMPLATE_NAME =
            new PsiElementPath(new ElementTypePredicate(tag_and_doc_comment).onFirstAncestor(),
                               new ElementTypePredicate(template_tag_pair, deltemplate_tag_pair).onChildren(),
                               new ElementTypePredicate(template_tag, deltemplate_tag).onChildren())
                    .debug("path_to_containing_template_name");

    /**
     * @return The fully qualified name of this template
     */
    @Nullable
    String getTemplateName();
}
