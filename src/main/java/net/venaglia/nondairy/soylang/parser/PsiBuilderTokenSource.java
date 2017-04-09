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

package net.venaglia.nondairy.soylang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;

/**
 * User: ed
 * Date: Aug 14, 2010
 * Time: 4:53:07 PM
 */
public class PsiBuilderTokenSource extends TokenSource {

    public static final boolean DEBUG_PSI_BUILDER = true;

    private final PsiBuilder builder;

    private int symbolIndex = 0;
    private IElementType previousToken;

    public PsiBuilderTokenSource(PsiBuilder builder) {
        this.builder = builder;
        this.builder.setDebugMode(DEBUG_PSI_BUILDER);
    }

    @Override
    public PsiBuilder.Marker mark(@NonNls Object name) {
        return builder.mark();
    }

    @Override
    public IElementType token() {
        return builder.getTokenType();
    }

    @Override
    public IElementType previous() {
        return previousToken;
    }

    @Override
    public String text() {
        return builder.getTokenText();
    }

    @Override
    public boolean eof() {
        return builder.eof();
    }

    @Override
    public void advance() {
        if (!builder.eof()) {
            previousToken = token();
        }
        builder.advanceLexer();
        symbolIndex++;
    }

    @Override
    public int index() {
        return symbolIndex;
    }

    @Override
    public void error(String message) {
        builder.error(message);
    }
}
