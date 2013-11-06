/*
 * Copyright 2010 - 2013 Ed Venaglia
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

package net.venaglia.nondairy.soylang.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.SoyFile;
import net.venaglia.nondairy.soylang.elements.AliasTagElement;
import net.venaglia.nondairy.soylang.elements.NamespaceMemberElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.PushPopPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ed
 * Date: 3/14/12
 * Time: 6:18 PM
 */
public class AliasNotUsedInspection extends AbstractSoyInspectionWithSingleQuickFix {

    private static final PushPopPredicate.PopJoin POP_JOIN = new PushPopPredicate.PopJoin() {
        @Override
        public PsiElementCollection join(PsiElementCollection current, PsiElementCollection popped) {
            Map<String,NamespaceMemberElement> result = new HashMap<String,NamespaceMemberElement>();
            for (PsiElement element : popped) {
                if (element instanceof NamespaceMemberElement) {
                    NamespaceMemberElement e = (NamespaceMemberElement)element;
                    result.put(e.getNamespace(), e);
                }
            }
            for (PsiElement element : current) {
                if (element instanceof NamespaceMemberElement) {
                    result.remove(((NamespaceMemberElement)element).getNamespace());
                }
            }
            return new PsiElementCollection(result.values());
        }
    };

    private static final PsiElementPath FIND_UNUSED_ALIASES = new PsiElementPath(
            PushPopPredicate.push(),
            new ElementTypePredicate(SoyElement.soy_file).onChildren(),
            new ElementTypePredicate(SoyElement.alias_def).onChildren(),
            new ElementTypePredicate(SoyElement.alias_name).onChildrenOfChildren(),
            PushPopPredicate.swap(),
            new ElementTypePredicate(SoyElement.soy_file).onChildren(),
            new ElementTypePredicate(SoyElement.tag_and_doc_comment).onChildren(),
            new ElementTypePredicate(SoyElement.template_tag_pair).onChildren(),
            new ElementTypePredicate(SoyElement.template_name_ref_absolute).onAllDescendants(),
            PushPopPredicate.popAndJoin(POP_JOIN),
            PsiElementPath.PARENT_ELEMENT,
            PsiElementPath.PARENT_ELEMENT
    ).debug("alias_not_used");


    public AliasNotUsedInspection() {
        super("unused.alias");
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Override
    protected void findProblems(@NotNull SoyFile file,
                                @NotNull InspectionManager manager,
                                boolean isOnTheFly,
                                @NotNull List<ProblemDescriptor> problems) {
        PsiElementCollection elements = FIND_UNUSED_ALIASES.navigate(file);
        for (PsiElement element : elements) {
            checkCanceled();
            if (element instanceof AliasTagElement) {
                String name = ((AliasTagElement)element).getNamespace();
                problems.add(manager.createProblemDescriptor(element,
                             getMessage(name),
                             getQuickFix(name),
                             ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                             true));

            }
        }
    }

    @Override
    protected void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        AliasTagElement element = (AliasTagElement)descriptor.getPsiElement();
        VirtualFile containingFile = element.getContainingFile().getVirtualFile();

        if (containingFile != null) {
            PsiElement parentElement = element.getParent();
            String parentText = parentElement.getText();
            int offset = element.getTextOffset();
            int parentOffset = offset - parentElement.getTextOffset();
            int from = offset + parentText.lastIndexOf('\n', parentOffset) - parentOffset;
            int to = offset + parentText.indexOf('\n', parentOffset) - parentOffset;
            if (from >= 0 && from < to) {
                final Document document = FileDocumentManager.getInstance().getDocument(containingFile);
                if (document != null) {
                    document.replaceString(from, to, "");
                    PsiDocumentManager.getInstance(parentElement.getProject()).commitDocument(document);
                }
            }
        }
    }
}
