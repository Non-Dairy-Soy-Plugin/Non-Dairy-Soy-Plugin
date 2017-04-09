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
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import net.venaglia.nondairy.soylang.elements.factory.SoyPsiElementFactory;
import net.venaglia.nondairy.soylang.lexer.SoyLexer;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: Jul 31, 2010
 * Time: 9:39:00 PM
 *
 * IntelliJ parser definition for closure templates.
 */
public class SoyParserDefinition implements ParserDefinition {

    @NotNull
    public Lexer createLexer(Project project) {
        return new SoyLexer();
    }

    public PsiParser createParser(Project project) {
        return new SoyParser();
    }

    public IFileElementType getFileNodeType() {
        return SoyFileType.FILE;
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return SoyToken.WHITESPACE_TOKENS;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return SoyToken.COMMENT_TOKENS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return SoyToken.STRING_LITERAL_TOKENS;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return SoyPsiElementFactory.getInstance().create(node);
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new SoyFile(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        IElementType leftType = left.getElementType();
        IElementType rightType = right.getElementType();
        if (SoyToken.LBRACE == leftType) {
            if (SoyToken.COMMANDS.contains(rightType) || SoyToken.SPECIAL_CHARS.contains(rightType) || SoyToken.DIV == rightType) {
                return SpaceRequirements.MUST_NOT;
            }
        } else if (SoyToken.DIV == leftType) {
            if (SoyToken.COMMANDS.contains(rightType) || SoyToken.SPECIAL_CHARS.contains(rightType)) {
                return SpaceRequirements.MUST_NOT;
            }
        } else if (SoyToken.WORD_TOKENS.contains(leftType) && SoyToken.WORD_TOKENS.contains(rightType)) {
            return SpaceRequirements.MUST;
        }
        return SpaceRequirements.MAY;
    }
}
