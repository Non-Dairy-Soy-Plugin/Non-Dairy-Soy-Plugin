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

package net.venaglia.nondairy.soylang.elements.path;

import net.venaglia.nondairy.soylang.SoyElement;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: 1/27/12
 * Time: 6:19 PM
 *
 * A PsiElementPath that will find template tab elements by their fully
 * qualified name.
 *
 * This path makes use of {@link net.venaglia.nondairy.soylang.elements.path.SoyFileElementTraversalPredicate} to navigate
 * to other files.
 */
public class DeltemplatePath extends PsiElementPath {

    private DeltemplatePath(@NotNull String templateName) {
        super(buildPath(templateName));
    }

    private DeltemplatePath(@NotNull String packageName, @NotNull String templateName) {
        super(buildPath(packageName, templateName));
    }

    private static ElementPredicate[] buildPath(@NotNull String templateName) {
        templateName = templateName.trim();
        if (templateName.length() == 0) {
            throw new IllegalArgumentException("template name cannot be blank.");
        }
        if (templateName.endsWith(".") || templateName.startsWith(".")) {
            throw new IllegalArgumentException("invalid template name: " + templateName);
        }
        return new ElementPredicate[]{
                SoyFileElementTraversalPredicate.filesForDelegateTemplate(templateName),
                new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                new ElementTypePredicate(SoyElement.deltemplate_tag).onDescendants(2,3),
                new DeltemplateNamePredicate(templateName),
                new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                new ElementTypePredicate(SoyElement.deltemplate_name).onChildren()
        };
    }

    private static ElementPredicate[] buildPath(@NotNull String packageName, @NotNull String templateName) {
        templateName = templateName.trim();
        if (templateName.length() == 0) {
            throw new IllegalArgumentException("template name cannot be blank.");
        }
        if (templateName.endsWith(".") || templateName.startsWith(".")) {
            throw new IllegalArgumentException("invalid template name: " + templateName);
        }
        return new ElementPredicate[]{
                SoyFileElementTraversalPredicate.filesForDelegateTemplate(templateName),
                new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                new ElementTypePredicate(SoyElement.package_def).onChildren(),
                new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                PushPopPredicate.push(),
                new PackageNamePredicate(packageName).onChildren(),
                PushPopPredicate.pop(),
                new ElementTypePredicate(SoyElement.deltemplate_tag).onDescendants(2,3),
                new DeltemplateNamePredicate(templateName),
                new ElementTypePredicate(SoyElement.tag_between_braces).onChildren(),
                new ElementTypePredicate(SoyElement.deltemplate_name).onChildren()
        };
    }

    /**
     * Safely builds a new {@link net.venaglia.nondairy.soylang.elements.path.PsiElementPath} for the specified template
     * name. If the template name is malformed, {@link net.venaglia.nondairy.soylang.elements.path.PsiElementPath#EMPTY} is
     * returned.
     * @param templateName The template name to navigate to.
     * @return A path object to navigate to the specified template.
     */
    @NotNull
    public static PsiElementPath forTemplateName(@NotNull String templateName) {
        try {
            return new DeltemplatePath(templateName);
        } catch (IllegalArgumentException e) {
            return PsiElementPath.EMPTY;
        }
    }

    /**
     * Safely builds a new {@link net.venaglia.nondairy.soylang.elements.path.PsiElementPath} for the specified template
     * name. If the template name is malformed, {@link net.venaglia.nondairy.soylang.elements.path.PsiElementPath#EMPTY} is
     * returned.
     * @param packageName The package name to navigate within.
     * @param templateName The template name to navigate to.
     * @return A path object to navigate to the specified template.
     */
    @NotNull
    public static PsiElementPath forTemplateName(@NotNull String packageName, @NotNull String templateName) {
        try {
            return new DeltemplatePath(packageName, templateName);
        } catch (IllegalArgumentException e) {
            return PsiElementPath.EMPTY;
        }
    }
}
