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

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 27, 2010
 * Time: 7:52:00 AM
 */
public class SoyFoldingBuilder implements FoldingBuilder {

    public static final FoldingDescriptor[] EMPTY = new FoldingDescriptor[0];

    private static final TokenSet SEARCH_FOR = TokenSet.create(SoyFileType.FILE,
                                                               SoyElement.soy_file,
                                                               SoyElement.tag_and_doc_comment,
                                                               SoyElement.template_tag_pair);

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
        Collection<ASTNode> nodes = Collections.singleton(node);
        for (int i = 0; i < 4; ++i) {
            nodes = childrenOf(nodes, SEARCH_FOR);
        }
        nodes = childrenOf(nodes, TokenSet.create(SoyElement.template_tag_pair));
        if (!nodes.isEmpty()) {
            FoldingDescriptor[] descriptors = new FoldingDescriptor[nodes.size()];
            int i = 0;
            for (ASTNode astNode : nodes) {
                descriptors[i++] = new FoldingDescriptor(astNode, astNode.getTextRange());
            }
            return descriptors;
        }
        return EMPTY;
    }

    private Collection<ASTNode> childrenOf(@NotNull Collection<ASTNode> nodes,
                                           @NotNull TokenSet filter) {
        if (nodes.isEmpty()) return nodes;
        Collection<ASTNode> buffer = new LinkedList<ASTNode>();
        for (ASTNode node : nodes) {
            if (filter.contains(node.getElementType())) buffer.add(node);
            buffer.addAll(Arrays.asList(node.getChildren(filter)));
        }
        return buffer.isEmpty() ? Collections.<ASTNode>emptySet() : buffer;
    }

    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        if (node.getElementType() == SoyElement.template_tag_pair) {
            Collection<ASTNode> buffer = Collections.singleton(node);
            buffer = childrenOf(buffer, TokenSet.create(SoyElement.template_tag));
            buffer = childrenOf(buffer, TokenSet.create(SoyElement.tag_between_braces));
            buffer = childrenOf(buffer, TokenSet.create(SoyElement.template_name));
            if (!buffer.isEmpty()) {
                return "{template " + buffer.iterator().next().getText() + " .../}"; //NON-NLS
            }
        }
        return null;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
