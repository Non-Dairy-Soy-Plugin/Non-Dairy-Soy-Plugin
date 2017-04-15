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

package net.venaglia.nondairy.soylang.inspection;

import static net.venaglia.nondairy.soylang.SoyElement.*;

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
import net.venaglia.nondairy.soylang.elements.ParameterElement;
import net.venaglia.nondairy.soylang.elements.ParameterRefElement;
import net.venaglia.nondairy.soylang.elements.TemplateMemberElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.ParameterPredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.PushPopPredicate;
import net.venaglia.nondairy.soylang.elements.path.SoyFileElementTraversalPredicate;
import net.venaglia.nondairy.soylang.elements.path.TraverseEmpty;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: ed
 * Date: 3/17/12
 * Time: 1:28 PM
 */
public class CallParameterNotDefinedInspection extends AbstractSoyInspectionWithSingleQuickFix {

    private static final PushPopPredicate.PopJoin FILTER_UNDECLARED_POP_JOIN = new PushPopPredicate.PopJoin() {
        @Override
        public PsiElementCollection join(PsiElementCollection current, PsiElementCollection popped) {
            @NonNls Set<String> namesDeclared = new HashSet<String>(current.size());
            for (PsiElement element : current) {
                if (element instanceof ParameterElement) {
                    namesDeclared.add(((ParameterElement)element).getName());
                }
            }
            PsiElementCollection result = new PsiElementCollection(popped.size());
            for (PsiElement element : popped) {
                if (element instanceof ParameterElement &&
                        !namesDeclared.contains(((ParameterElement) element).getName()) &&
                        ParameterRefElement.PATH_TO_ITERATOR_DEF.navigate(element)
                                .applyPredicate(new ParameterPredicate(((ParameterElement) element).getName()))
                                .isEmpty() &&
                        ParameterRefElement.PATH_TO_INNER_PARAM_DEF.navigate(element)
                                .applyPredicate(new ParameterPredicate(((ParameterElement) element).getName()))
                                .isEmpty()) {
                    result.add(element);
                }
            }
            return result;
        }
    };

    private static final PushPopPredicate.PopJoin FILTER_TEMPLATE_NAMES_POP_JOIN = new PushPopPredicate.PopJoin() {

        @Override
        public PsiElementCollection join(PsiElementCollection current, PsiElementCollection popped) {
            Set<String> names = new HashSet<String>(popped.size());
            for (PsiElement element : popped) {
                if (element instanceof TemplateMemberElement) {
                    names.add(((TemplateMemberElement)element).getTemplateName());
                }
            }
            PsiElementCollection result = new PsiElementCollection();
            for (PsiElement element : current) {
                if (element instanceof TemplateMemberElement &&
                        names.contains(((TemplateMemberElement)element).getTemplateName())) {
                    result.add(element);
                }
            }
            return result;
        }
    };

    private static final PsiElementPath PATH_TO_UNDECLARED_CALL_PARAMS =
            new PsiElementPath(TraverseEmpty.CONTINUE,
                               new ElementTypePredicate(param_tag).onChildren(),
                               new ElementTypePredicate(invocation_parameter_ref).onChildrenOfChildren(),
                               PushPopPredicate.push(),
                               PushPopPredicate.push(),
                               SoyFileElementTraversalPredicate.filesStartingOnNamespaceElement(),
                               new ElementTypePredicate(soy_file).onChildren(),
                               new ElementTypePredicate(tag_and_doc_comment).onChildren(),
                               new ElementTypePredicate(template_tag).onChildrenOfChildren(),
                               PushPopPredicate.popAndJoin(FILTER_TEMPLATE_NAMES_POP_JOIN),
                               new ElementTypePredicate(tag_and_doc_comment).onFirstAncestor(),
                               new ElementTypePredicate(doc_comment).onChildren(),
                               new ElementTypePredicate(doc_comment_tag_with_description).onChildren(),
                               new ElementTypePredicate(doc_comment_param_def).onChildren(),
                               PushPopPredicate.popAndJoin(FILTER_UNDECLARED_POP_JOIN)).debug("call_parameter_not_declared!call_params");

    private static final PsiElementPath FIND_UNDECLARED_PARAMS =
            new PsiElementPath(TraverseEmpty.CONTINUE,
                               new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                               new ElementTypePredicate(SoyElement.tag_and_doc_comment).onChildren(),
                               new ElementTypePredicate(call_tag_pair).onAllDescendants(),
                               PATH_TO_UNDECLARED_CALL_PARAMS.asForkingTraversalPredicate()).debug("call_parameter_not_declared!main");

    private static final PsiElementPath PATH_TO_TEMPLATE_TAG_PAIR =
            new PsiElementPath(new ElementTypePredicate(template_tag_pair).onFirstAncestor());

    private static final PsiElementPath PATH_TO_TEMPLATE_DOC =
            new PsiElementPath(new ElementTypePredicate(tag_and_doc_comment).onFirstAncestor(),
                               new ElementTypePredicate(doc_comment).onChildren()).debug("call_parameter_not_declared!docs");

    public CallParameterNotDefinedInspection() {
        super("undeclared.call.parameter");
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Override
    protected void findProblems(@NotNull SoyFile file,
                                @NotNull InspectionManager manager,
                                boolean isOnTheFly,
                                @NotNull List<ProblemDescriptor> problems) {
        PsiElementCollection elements = FIND_UNDECLARED_PARAMS.navigate(file);
        for (PsiElement element : elements) {
            checkCanceled();
            if (element instanceof ParameterElement) {
                String name = ((ParameterElement)element).getName();
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
        ParameterElement element = (ParameterElement)descriptor.getPsiElement();
        VirtualFile containingFile = element.getContainingFile().getVirtualFile();
        @NonNls String parameterName = element.getName();
        PsiElement templatePair = PATH_TO_TEMPLATE_TAG_PAIR.navigate(element).oneOrNull();
        PsiElement doc = templatePair == null ? null : PATH_TO_TEMPLATE_DOC.navigate(templatePair).oneOrNull();
        int insertionPoint = 0;
        @NonNls String insertionText = null;
        if (containingFile == null) {
            // no-op
        } else if (doc != null) {
            String docText = doc.getText();
            int lastLine = docText.lastIndexOf('\n') + 1;
            insertionPoint = lastLine + doc.getTextOffset();
            insertionText = " * @param " + parameterName + "\n";
            if (lastLine == 0) {
                insertionText = "\n" + insertionText;
            }
        } else if (templatePair != null) {
            PsiElement parent = templatePair.getParent();
            String parentText = parent.getText();
            insertionPoint = parent.getTextOffset() + parentText.lastIndexOf('\n', templatePair.getStartOffsetInParent()) + 1;
            insertionText = "/**\n * @param " + parameterName + "\n */\n";
        }
        if (insertionText != null) {
            final Document document = FileDocumentManager.getInstance().getDocument(containingFile);
            if (document != null) {
                document.replaceString(insertionPoint, insertionPoint, insertionText);
                PsiDocumentManager.getInstance(element.getProject()).commitDocument(document);
            }
        }
    }
}
