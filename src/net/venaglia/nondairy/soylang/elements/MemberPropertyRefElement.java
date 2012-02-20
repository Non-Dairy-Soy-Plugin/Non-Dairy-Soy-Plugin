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

package net.venaglia.nondairy.soylang.elements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: 1/17/12
 * Time: 8:03 PM
 *
 * SoyPsiElement class that represents a dotted property reference on a
 * ParameterRefElement, or another MemberPropertyRefElement.
 */
public class MemberPropertyRefElement
        extends SoyPsiElement {

//    private static final PsiElementPath STRING_LITERAL_TEXT_PATH = new PsiElementPath(new ElementTypePredicate(SoyToken.STRING_LITERAL).onChildren());

    private String name;

    public MemberPropertyRefElement(@NotNull ASTNode node) {
        this(node, node.getText());
    }

    private MemberPropertyRefElement(@NotNull ASTNode node, String name) {
        super(node);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * SoyPsiElement class that represents a property reference on a
     * ParameterRefElement, or another MemberPropertyRefElement.
     * 
     * This class implements {@link IntermediateElement} to act as an
     * intermediary for building a MemberPropertyRefElement for bracketed
     * string literals.
     */
    public static class ForBracketedStringLiteral extends SoyPsiElement implements IntermediateElement {

        public ForBracketedStringLiteral(@NotNull ASTNode node) {
            super(node);
        }

        @Override
        public SoyPsiElement resolveFinalElement() {
            ASTNode[] children = getNode().getChildren(null);
            if (children != null && children.length == 3 &&
                children[0].getElementType() == SoyToken.LBRACK &&
                children[1].getElementType() == SoyElement.constant_expression &&
                children[2].getElementType() == SoyToken.RBRACK) {
                children = children[1].getChildren(null);
                if (children != null && children.length >= 3 &&
                    children[0].getElementType() == SoyToken.STRING_LITERAL_BEGIN &&
                    children[children.length - 1].getElementType() == SoyToken.STRING_LITERAL_END) {
                    StringBuilder buffer = new StringBuilder();
                    for (int i = 1, l = children.length - 1; i < l; i++) {
                        ASTNode node = children[i];
                        IElementType elementType = node.getElementType();
                        String text = node.getText();
                        if (elementType == SoyToken.STRING_LITERAL) {
                            buffer.append(text);
                        } else if (elementType == SoyToken.STRING_LITERAL_ESCAPE && text.charAt(0) == '\\') {
                            switch (text.charAt(1)) {
                                case '\\': text = "\\"; break;
                                case '\'': text = "\'"; break;
                                case '\"': text = "\""; break;
                                case 'r' : text = "\r"; break;
                                case 'n' : text = "\n"; break;
                                case 'f' : text = "\f"; break;
                                case 't' : text = "\t"; break;
                                case 'b' : text = "\b"; break;
                                case 'u' :
                                    char c = (char)Integer.parseInt(text.substring(2), 16);
                                    text = String.valueOf(c); 
                                    break;
                                default: return new SoyPsiElement(getNode());
                            }
                            buffer.append(text);
                        } else {
                            return new SoyPsiElement(getNode());
                        }
                    }
                    return new MemberPropertyRefElement(getNode(), buffer.toString());
                }
            }
            return new SoyPsiElement(getNode());
        }
    }
}
