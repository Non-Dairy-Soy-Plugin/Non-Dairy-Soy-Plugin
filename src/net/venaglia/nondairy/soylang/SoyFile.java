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

package net.venaglia.nondairy.soylang;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import net.venaglia.nondairy.soylang.elements.NamespaceDefElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiPath;
import org.jetbrains.annotations.NotNull;

/**
 * 
 */
public class SoyFile extends PsiFileBase {

    private final PsiPath referencePath;
    private final PsiPath namespaceNamePath;

    public SoyFile(FileViewProvider viewProvider) {
        super(viewProvider, SoyLanguage.INSTANCE);
        referencePath = new PsiPath(new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                                    new ElementTypePredicate(SoyElement.tag_and_doc_comment).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_tag_pair).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_tag).onChildren(),
                                    new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_name).onChildren())
                .or(    new PsiPath(new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_tag_pair).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_tag).onChildren(),
                                    new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                                    new ElementTypePredicate(SoyElement.template_name).onChildren()));
        namespaceNamePath = new PsiPath(new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                                        new ElementTypePredicate(SoyElement.namespace_def).onChildren(),
                                        new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                                        new ElementTypePredicate(SoyElement.namespace_name).onChildren());
    }

    @NotNull
    public FileType getFileType() {
        return SoyFileType.INSTANCE;
    }

    public PsiElementCollection getTemplateElements() {
        return referencePath.navigate(this);
    }

    public NamespaceDefElement getNamespaceElement() {
        return (NamespaceDefElement)namespaceNamePath.navigate(this).oneOrNull();
    }
}
