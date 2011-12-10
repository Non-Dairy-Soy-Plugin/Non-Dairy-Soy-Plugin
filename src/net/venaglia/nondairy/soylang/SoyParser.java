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
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import net.venaglia.nondairy.soylang.parser.PsiBuilderTokenSource;
import net.venaglia.nondairy.soylang.parser.SoyStructureParser;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 31, 2010
 * Time: 9:42:17 PM
 */
public class SoyParser implements PsiParser {

    @NotNull
    public ASTNode parse(IElementType root, PsiBuilder builder) {
        synchronized (SoyParser.class) {
            builder.setDebugMode(true);
            builder.enforceCommentTokens(TokenSet.create(SoyToken.COMMENT));
//            PsiBuilder.Marker file = builder.mark();
            new SoyStructureParser(new PsiBuilderTokenSource(builder)).parse();
//            file.done(root);
            return builder.getTreeBuilt();
        }
    }
}
