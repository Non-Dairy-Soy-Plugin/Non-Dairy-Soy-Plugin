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
import net.venaglia.nondairy.soylang.elements.ParameterElement;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.PushPopPredicate;
import net.venaglia.nondairy.soylang.elements.path.TraverseEmpty;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: ed
 * Date: 3/14/12
 * Time: 6:18 PM
 */
public class ParameterNotUsedInspection extends AbstractSoyInspectionWithSingleQuickFix {

    private static final PushPopPredicate.PopJoin POP_JOIN = new PushPopPredicate.PopJoin() {
        @Override
        public PsiElementCollection join(PsiElementCollection current, PsiElementCollection popped) {
            Set<String> namesUsed = new HashSet<String>(current.size());
            for (PsiElement element : current) {
                if (element instanceof ParameterElement) {
                    namesUsed.add(((ParameterElement)element).getName());
                }
            }
            PsiElementCollection result = new PsiElementCollection(popped.size());
            for (PsiElement element : popped) {
                if (element instanceof ParameterElement &&
                    !namesUsed.contains(((ParameterElement)element).getName())) {
                    result.add(element);
                }
            }
            return result;
        }
    };

    private static final PsiElementPath PATH_TO_UNUSED_PARAMS = new PsiElementPath(
            TraverseEmpty.CONTINUE,
            new ElementTypePredicate(SoyElement.doc_comment).onChildren(),
            new ElementTypePredicate(SoyElement.doc_comment_tag_with_description).onChildren(),
            new ElementTypePredicate(SoyElement.doc_comment_param_def).onChildren(),
            PushPopPredicate.push(),
            PsiElementPath.PARENT_ELEMENT,
            PsiElementPath.PARENT_ELEMENT,
            new ElementTypePredicate(SoyElement.template_tag_pair).onNextSiblings(false),
            new ElementTypePredicate(SoyElement.parameter_ref).onAllDescendants(),
            PushPopPredicate.popAndJoin(POP_JOIN)
    ).debug("parameter_not_used!unused_params");

    private static final PsiElementPath FIND_UNUSED_PARAMS = new PsiElementPath(
            new ElementTypePredicate(SoyElement.soy_file).onChildren(),
            new ElementTypePredicate(SoyElement.tag_and_doc_comment).onChildren(),
            PATH_TO_UNUSED_PARAMS.asForkingTraversalPredicate()
    ).debug("parameter_not_used!templates");

    public ParameterNotUsedInspection() {
        super("unused.parameter");
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
        PsiElementCollection elements = FIND_UNUSED_PARAMS.navigate(file);
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

    private static final Pattern MATCH_PARAMS = Pattern.compile("^[\\s*]+@param\\??\\s+(\\w+)|" + // NON-NLS
                                                                "^[\\s*]+@", Pattern.MULTILINE); // NON-NLS

    @Override
    protected void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        ParameterElement element = (ParameterElement)descriptor.getPsiElement();
        VirtualFile containingFile = element.getContainingFile().getVirtualFile();
        String param = element.getName();
        PsiElement docElement = element.getParent();
        String docText = docElement.getText();
        Matcher matcher = MATCH_PARAMS.matcher(docText);
        int startIndex = -1;
        int endIndex = docText.length();
        while (matcher.find()) {
            if (param.equals(matcher.group(1))) {
                startIndex = matcher.start();
            } else if (startIndex >= 0) {
                endIndex = matcher.start();
                break;
            }
        }
        if (startIndex >= 0 && containingFile != null) {
            int offset = docElement.getTextOffset();
            int from = offset + docText.lastIndexOf('\n', startIndex);
            int to = offset + docText.lastIndexOf('\n', endIndex);
            if (from == to) {
                // last line
                to = docText.lastIndexOf("*/");
            }
            if (from >= 0 && from < to) {
                final Document document = FileDocumentManager.getInstance().getDocument(containingFile);
                if (document != null) {
                    document.replaceString(from, to, "");
                    PsiDocumentManager.getInstance(docElement.getProject()).commitDocument(document);
                }
            }
        }
    }
}
