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

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.SoyFile;
import net.venaglia.nondairy.soylang.elements.AbsoluteTemplateNameRef;
import net.venaglia.nondairy.soylang.elements.AttributeElement;
import net.venaglia.nondairy.soylang.elements.LocalTemplateNameRef;
import net.venaglia.nondairy.soylang.elements.TemplateDefElement;
import net.venaglia.nondairy.soylang.elements.TemplateMemberElement;
import net.venaglia.nondairy.soylang.elements.path.AttributePredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTextPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementMapper;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.TemplateNamePredicate;
import net.venaglia.nondairy.soylang.elements.path.TemplatePath;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.venaglia.nondairy.soylang.SoyElement.*;

/**
 * User: ed
 * Date: 3/23/12
 * Time: 7:30 PM
 */
public class CallMissingRequiredParamInspection extends AbstractSoyInspection {

    private static final PsiElementPath PATH_TO_CALLS = new PsiElementPath(
            new ElementTypePredicate(call_tag).onAllDescendants()
    ).debug("call_missing_req_param!call_tags");

    private static final PsiElementPath PATH_TO_TEMPLATE_NAME = new PsiElementPath(
            new ElementTypePredicate(tag_between_braces).onChildren(),
            new ElementTypePredicate(template_name_ref, template_name_ref_absolute).onChildren()
    ).debug("call_missing_req_param!template_name");

    private static final PsiElementPath PATH_TO_PARAM_NAMES = new PsiElementPath(
            new ElementTypePredicate(call_tag_pair).onParent(),
            new ElementTypePredicate(param_tag).onChildren(),
            new ElementTypePredicate(invocation_parameter_ref).onChildrenOfChildren()
    ).debug("call_missing_req_param!param_name");

    private static final PsiElementPath PATH_TO_DATA_ATTRIBUTE = new PsiElementPath(
            AttributePredicate.hasAttribute("data").onChildrenOfChildren()
    ).debug("call_missing_req_param!data_attr");

    private static final PsiElementPath PATH_TO_ENCLOSING_TEMPLATE = new PsiElementPath(
            new ElementTypePredicate(template_tag_pair).onFirstAncestor(),
            new ElementTypePredicate(template_tag).onChildren()
    ).debug("call_missing_req_param!enc_template");

    private static final PsiElementPath PATH_TO_REQUIRED_TEMPLATE_PARAMS = new PsiElementPath(
            new ElementTypePredicate(tag_and_doc_comment).onFirstAncestor(),
            new ElementTypePredicate(doc_comment).onChildren(),
            new ElementTypePredicate(doc_comment_tag_with_description).onChildren(),
            new ElementTypePredicate(doc_comment_tag).onChildren(),
            new ElementTextPredicate("@param"),
            new ElementTypePredicate(doc_comment_param_def).onNextSibling()
    ).debug("call_missing_req_param!req_params");

    public static final PsiElementMapper<String> MAP_TO_TEXT_STRING = new PsiElementMapper<String>() {
        @Override
        public String map(PsiElement element) {
            return element.getText();
        }
    };

    public CallMissingRequiredParamInspection() {
        super("call.missing.param");
    }

    @Override
    protected void findProblems(@NotNull SoyFile file,
                                @NotNull InspectionManager manager,
                                boolean isOnTheFly,
                                @NotNull List<ProblemDescriptor> problems) {
        for (PsiElement element : PATH_TO_CALLS.navigate(file)) {
            checkCanceled();
            TemplateMemberElement templateName = (TemplateMemberElement)PATH_TO_TEMPLATE_NAME.navigate(element).oneOrNull();
            Set<String> requiredParams = loadRequiredParams(templateName);
            if (requiredParams.size() > 0) {
                Set<String> passedParams = loadPassedParams(element);
                if (passedParams == null) continue;
                for (String param : requiredParams) {
                    checkCanceled();
                    if (!passedParams.contains(param)) {
                        problems.add(manager.createProblemDescriptor(element,
                                                                     getMessage(getTextFor(templateName), param),
                                                                     ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                                                     null,
                                                                     isOnTheFly));
                    }
                }
            }
        }
    }

    private String getTextFor(@Nullable PsiElement templateName) {
        return templateName == null ? "" : templateName.getText();
    }

    @Nullable
    private Set<String> loadPassedParams(PsiElement callTag) {
        Set<String> params = new HashSet<String>();
        AttributeElement dataAttr = (AttributeElement)PATH_TO_DATA_ATTRIBUTE.navigate(callTag).oneOrNull();
        if (dataAttr != null) {
            @NonNls String dataValue = dataAttr.getAttributeValue("");
            if (dataValue.startsWith("$")) {
                return null; // params passed by object, can't validate
            }
            if ("all".equals(dataValue)) {
                params.addAll(loadRequiredParams(PATH_TO_ENCLOSING_TEMPLATE.navigate(callTag).oneOrNull()));
            }
        }
        PsiElementCollection elements = PATH_TO_PARAM_NAMES.navigate(callTag);
        params.addAll(elements.map(MAP_TO_TEXT_STRING));
        return params;
    }

    @NotNull
    private Set<String> loadRequiredParams(@Nullable PsiElement element) {
        if (element instanceof TemplateMemberElement) {
            TemplateMemberElement templateNameElement = (TemplateMemberElement)element;
            String myTemplateName = templateNameElement.getTemplateName();
            PsiElementPath pathToTemplateParameters = null;
            if (myTemplateName != null) {
                if (templateNameElement instanceof LocalTemplateNameRef) {
                    pathToTemplateParameters = new PsiElementPath(
                            new ElementTypePredicate(soy_file).onFirstAncestor(),
                            new ElementTypePredicate(tag_and_doc_comment).onChildren(),
                            new ElementTypePredicate(template_tag_pair).onChildren(),
                            new TemplateNamePredicate(myTemplateName).onChildren(),
                            PsiElementPath.PARENT_ELEMENT,
                            PsiElementPath.PARENT_ELEMENT,
                            new ElementTypePredicate(doc_comment).onChildren(),
                            new ElementTypePredicate(doc_comment_tag_with_description).onChildren(),
                            new ElementTypePredicate(doc_comment_tag).onChildren(),
                            new ElementTextPredicate("@param"),
                            new ElementTypePredicate(doc_comment_param_def).onNextSibling()
                    );
                }
                else if (templateNameElement instanceof TemplateDefElement) {
                    pathToTemplateParameters = PATH_TO_REQUIRED_TEMPLATE_PARAMS;
                }
                else if (templateNameElement instanceof AbsoluteTemplateNameRef) {
                    templateNameElement = (TemplateMemberElement)TemplatePath.forTemplateName(myTemplateName)
                                                                             .navigate(templateNameElement)
                                                                             .oneOrNull();
                    if (templateNameElement == null) {
                        return Collections.emptySet();
                    }
                    pathToTemplateParameters = PATH_TO_REQUIRED_TEMPLATE_PARAMS;
                }
            }
            if (pathToTemplateParameters != null) {
                PsiElementCollection elements = pathToTemplateParameters.navigate(templateNameElement);
                if (elements.size() > 0) {
                    return new HashSet<String>(elements.map(MAP_TO_TEXT_STRING));
                }
            }
        }
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }
}
