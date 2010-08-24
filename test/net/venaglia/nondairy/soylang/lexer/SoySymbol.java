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

package net.venaglia.nondairy.soylang.lexer;

import com.intellij.psi.tree.IElementType;
import java_cup.runtime.ComplexSymbolFactory;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 16, 2010
 * Time: 3:27:51 PM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public class SoySymbol extends ComplexSymbolFactory.ComplexSymbol {
//class SoySymbol extends IElementType {

    private static Map<Integer,String> STATES_BY_ID;

    public final IElementType token;
    private final int state;
    public final int line;
    public final int column;
    public final int position;
    private final int length;
    public final Object payload;

    SoySymbol(SoyToken token, int state, int line, int column, int position, int length) {
        this(token, state, line, column, position, length, null);
    }

    SoySymbol(SoyToken token, int state, int line, int column, int position, int length, Object payload) {
        super(token.name(), token.parserValue(), location(line, position), location(line, position, payload), payload);
        this.token = token;
        this.state = state;
        this.line = line;
        this.column = column;
        this.position = position;
        this.length = length;
        this.payload = payload;
    }

    SoySymbol(IElementType token, int state, int line, int column, int position, int length) {
        this(token, state, line, column, position, length, null);
    }

    SoySymbol(IElementType token, int state, int line, int column, int position, int length, Object payload) {
        super(token.toString(), token.getIndex(), location(line, position), location(line, position, payload), payload);
        this.token = token;
        this.state = state;
        this.line = line;
        this.column = column;
        this.position = position;
        this.length = length;
        this.payload = payload;
    }

    public IElementType getToken() {
        return token;
    }

    public String getState() {
        if (STATES_BY_ID == null) {
            STATES_BY_ID = SoySyntaxUtil.extractSymbolNames(SoyScanner.class);
        }
        return STATES_BY_ID.get(state);
    }

    public int getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }

    public int getLength() {
        return length;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return String.format("%s [line:%d|col:%d] <%s>", token, line, column, getState());
    }

    private static ComplexSymbolFactory.Location location(int line, int column) {
        return new ComplexSymbolFactory.Location(line, column);
    }

    private static ComplexSymbolFactory.Location location(int line, int column, Object payload) {
        if (payload instanceof SoyDocCommentBuffer) {
            payload = ((SoyDocCommentBuffer)payload).getCapturedText();
        }
        if (payload instanceof CharSequence) {
            CharSequence value = (CharSequence)payload;
            for (int i = 0, j = value.length(); i < j; ++i) {
                switch (value.charAt(i)) {
                    case '\n': ++line; column = 0; break;
                    case '\t': column += 8 - column % 8; break;
                    case '\r': column = 0; break;
                    case '\b': column = Math.max(0, column - 1); break;
                    default: ++column; break;
                }
            }
        }
        return new ComplexSymbolFactory.Location(line, column);
    }
}
