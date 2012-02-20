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

package net.venaglia.nondairy.soylang;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.elements.NamespaceDefElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NotNull;

/**
 * Class to represent soy files in IntelliJ.
 */
public class SoyFile extends PsiFileBase {

    private static final PsiElementPath REFERENCE_PATH =
            new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                               new ElementTypePredicate(SoyElement.tag_and_doc_comment).onChildren(),
                               new ElementTypePredicate(SoyElement.template_tag_pair).onChildren(),
                               new ElementTypePredicate(SoyElement.template_tag).onChildren(),
                               new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                               new ElementTypePredicate(SoyElement.template_name).onChildren())
        .or(new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                               new ElementTypePredicate(SoyElement.template_tag_pair).onChildren(),
                               new ElementTypePredicate(SoyElement.template_tag).onChildren(),
                               new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                               new ElementTypePredicate(SoyElement.template_name).onChildren()))
        .or(new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                               new ElementTypePredicate(SoyElement.template_tag).onChildren(),
                               new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                               new ElementTypePredicate(SoyElement.template_name).onChildren()))
        .debug("reference_path");
    private static final PsiElementPath NAMESPACE_NAME_PATH =
            new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                               new ElementTypePredicate(SoyElement.namespace_def).onChildren(),
                               new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                               new ElementTypePredicate(SoyElement.namespace_name).onChildren())
         .debug("namespace_name_path");

    public SoyFile(FileViewProvider viewProvider) {
        super(viewProvider, SoyLanguage.INSTANCE);
    }

    @NotNull
    public FileType getFileType() {
        return SoyFileType.INSTANCE;
    }

    public PsiElementCollection getTemplateElements() {
        return REFERENCE_PATH.navigate(this);
    }

    public NamespaceDefElement getNamespaceElement() {
        PsiElement element = NAMESPACE_NAME_PATH.navigate(this).oneOrNull();
        return element instanceof NamespaceDefElement ? (NamespaceDefElement)element : null;
    }
}
