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

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.elements.SoyCommandTag;
import net.venaglia.nondairy.soylang.elements.path.CommandBoundaryPredicate;
import net.venaglia.nondairy.soylang.elements.path.ElementTypePredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ed
 * Date: Aug 27, 2010
 * Time: 7:52:00 AM
 *
 * Class to build FoldingDescriptor objects for the soy psi tree.
 */
public class SoyFoldingBuilder implements FoldingBuilder {

    public static final FoldingDescriptor[] EMPTY = new FoldingDescriptor[0];

    private static final PsiElementPath PATH_TO_FOLDING_REGIONS =
            new PsiElementPath(new ElementTypePredicate(SoyElement.soy_file).onChildren(),
                               new ElementTypePredicate(SoyElement.TAG_PAIR_TOKENS).onAllDescendants()).debug("path_to_folding_regions");
    private static final PsiElementPath PATH_TO_PLACEHOLDER_LABEL =
            new PsiElementPath(new CommandBoundaryPredicate(SoyCommandTag.Boundary.BEGIN).onFirstChild()).debug("path_to_placeholder_label");

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
        PsiElementCollection elements = PATH_TO_FOLDING_REGIONS.navigate(node.getPsi());
        if (elements.isEmpty()) {
            return EMPTY;
        }
        List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>(elements.size());
        for (PsiElement element : elements) {
            int startOffset = element.getTextOffset();
            int startLine = document.getLineNumber(startOffset);
            int textLength = element.getTextLength();
            int endLine = document.getLineNumber(startOffset + textLength - 1);
            if (startLine < endLine) {
                descriptors.add(new FoldingDescriptor(element.getNode(), element.getTextRange()));
            }
        }
        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
//        if (node.getElementType() == SoyElement.template_tag_pair) {
            PsiElement element = PATH_TO_PLACEHOLDER_LABEL.navigate(node.getPsi()).oneOrNull();
            if (element instanceof SoyCommandTag) {
                SoyCommandTag commandTag = (SoyCommandTag)element;
                String command = commandTag.getCommand();
                String label = commandTag.getFoldedLabel();
                if (label != null) {
                    return String.format("{%1$s %2$s}...{/%1$s}", command, label); //NON-NLS
                } else {
                    return String.format("{%1$s}...(/%1$s}", command); //NON-NLS
                }
            }
//        }
        return null;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

}
