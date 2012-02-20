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
import com.intellij.lang.CodeDocumentationAwareCommenterEx;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.lexer.SoyToken;

/**
 * User: ed
 * Date: Aug 10, 2010
 * Time: 8:43:42 PM
 *
 * Class to define comments as they are used in soy files.
 */
public class SoyCommenter implements CodeDocumentationAwareCommenterEx {

    public String getLineCommentPrefix() {
        return "//";
    }

    public String getBlockCommentPrefix() {
        return "/*";
    }

    public String getBlockCommentSuffix() {
        return "*/";
    }

    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    public String getCommentedBlockCommentSuffix() {
        return null;
    }

    @Override
    public IElementType getLineCommentTokenType() {
//        return SoyToken.LINE_COMMENT;
        return null;
    }

    @Override
    public IElementType getBlockCommentTokenType() {
//        return SoyToken.COMMENT;
        return null;
    }

    @Override
    public IElementType getDocumentationCommentTokenType() {
        return SoyToken.DOC_COMMENT_BEGIN;
//        return null;
    }

    @Override
    public String getDocumentationCommentPrefix() {
        return "/**";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDocumentationCommentLinePrefix() {
        return " *";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDocumentationCommentSuffix() {
        return "*/";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDocumentationComment(PsiComment element) {
        if (element == null) return false;
        ASTNode node = element.getNode();
        return node != null && SoyToken.DOC_COMMENT_TOKENS.contains(node.getElementType());
    }

    @Override
    public boolean isDocumentationCommentText(PsiElement element) {
        if (element == null || element.getNode() == null || element.getNode().getElementType() == null) return false;
        IElementType type = element.getNode().getElementType();
        return type == SoyToken.DOC_COMMENT ||
               type == SoyToken.DOC_COMMENT_BEGIN ||
               type == SoyToken.DOC_COMMENT_END;
    }
}
