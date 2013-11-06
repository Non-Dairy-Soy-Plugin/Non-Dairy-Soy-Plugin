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

import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: Aug 14, 2010
 * Time: 11:24:19 PM
 *
 * Parser component that specializes in parsing expressions.
 */
class ExpressionParser {

    @NonNls
    private static final String EXPRESSION_PARSER_ENABLED_PROPERTY
            = "net.venaglia.nondairy.parser.expressions.enabled";

    private static final boolean EXPRESSION_PARSER_ENABLED
            = Boolean.valueOf(System.getProperty(EXPRESSION_PARSER_ENABLED_PROPERTY,
                                                 String.valueOf(true)));

    // Precedence ordering

    private static final int PREC_LITERAL     = 0xF;
    private static final int PREC_PARENTHESIS = 0xE;
    private static final int PREC_FUNCTION    = 0xD;
    private static final int PREC_DOT         = 0x9;
    private static final int PREC_UMINUS_NOT  = 0x8;
    private static final int PREC_MUL_DIV_MOD = 0x7;
    private static final int PREC_ADD_SUB     = 0x6;
    private static final int PREC_LT_GT       = 0x5;
    private static final int PREC_EQ          = 0x4;
    private static final int PREC_AND         = 0x3;
    private static final int PREC_OR          = 0x2;
    private static final int PREC_TERNARY     = 0x1;
    private static final int PREC_UNSPECIFIED = 0x0;

    private final ExpressionParser parent;
    private final TokenSource source;

    private PsiBuilder.Marker exprMarker;
    private IElementType expressionType = expression;
    private int prec = PREC_UNSPECIFIED;
    private int remainingValues = -1;
    private int capturedTokens = 0;
    private boolean markerIsDone = false;
    private ExpressionType apparentType = ExpressionType.NONE;
    private ExpressionType expectingType = ExpressionType.ANY;

    ExpressionParser(TokenSource source) {
        this.parent = null;
        this.source = source;
        this.exprMarker = source.mark("exprMarker");
    }

    ExpressionParser(ExpressionParser parent) {
        this.parent = parent;
        this.source = parent.source;
        this.exprMarker = source.mark("exprMarker");
        this.expectingType = parent.expectingType;
    }

    public ExpressionParser expecting(ExpressionType expectingType) {
        this.expectingType = expectingType;
        return this;
    }

    private void done() {
        done(null);
    }

    private void done(@Nullable String errorMsg) {
        if (!markerIsDone) {
            if (parent != null) {
                parent.remainingValues -= 1;
                parent.apparentType = parent.apparentType.or(apparentType);
            }
            if (remainingValues > 0) {
                source.error(I18N.msg("syntax.error.expected.expression"));
                remainingValues = 0;
            }
            if (exprMarker != null) {
                if (errorMsg != null) {
                    exprMarker.error(errorMsg);
//                } else if (!apparentType.isAssignableTo(expectingType)) {
//                    PsiBuilder.Marker wrapper = exprMarker.precede();
//                    exprMarker.error(I18N.msg("syntax.error.inconvertable.expression",
//                                              apparentType.getLabel(),
//                                              expectingType.getLabel()));
//                    wrapper.done(expressionType);
                } else if (capturedTokens == 0) {
                    exprMarker.drop();
                } else {
                    exprMarker.done(expressionType);
                }
            }
            markerIsDone = true;
        }
    }

    void parse() {
        ExpressionParser parser = this;
        int parenCount = 0;
        while (!source.eof() && parser != null) {
            IElementType token = source.token();
            if (EXPRESSION_PARSER_ENABLED) {
                if (token == SoyToken.RPAREN || token == SoyToken.RBRACK || token == SoyToken.COMMA) {
                    break;
                }
                switch (parser.prec) {
                    case PREC_UNSPECIFIED:
                        parser = parser.parseInitial(token);
                        break;
                    default:
                        parser = parseResume(parser, token);
                        break;
                }
            } else if (SoyToken.EXPRESSION_TOKENS.contains(token)) {
                parser.prec = -1;
                if (token == SoyToken.LPAREN) {
                    parenCount++;
                } else if (token == SoyToken.RPAREN) {
                    if (--parenCount < 0) break;
                }
                source.advance();
                parser.capturedTokens++;
            } else {
                break;
            }
        }
        while (parser != null) {
            parser.done();
            if (parser == this) break;
            if (parser.parent != null) {
                parser.parent.capturedTokens += parser.capturedTokens;
            }
            parser = parser.parent;
        }
    }

    private void markAsConstantExpressionAndAdvance() {
        prec = PREC_LITERAL;
        expressionType = constant_expression;
        source.advance();
        capturedTokens++;
        remainingValues = 0;
        done();
    }

    private ExpressionParser parseInitial(IElementType token) {
        if (token == SoyToken.NULL_LITERAL) {
            markAsConstantExpressionAndAdvance();
        } else if (token == SoyToken.BOOLEAN_LITERAL) {
            apparentType = apparentType.or(ExpressionType.BOOLEAN);
            markAsConstantExpressionAndAdvance();
        } else if (token == SoyToken.INTEGER_LITERAL || token == SoyToken.FLOATING_POINT_LITERAL) {
            apparentType = apparentType.or(ExpressionType.NUMBER);
            markAsConstantExpressionAndAdvance();
        } else if (token == SoyToken.STRING_LITERAL_BEGIN) {
            apparentType = apparentType.or(ExpressionType.STRING);
            char delim = source.text().charAt(0);
            prec = PREC_LITERAL;
            expressionType = constant_expression;
            capturedTokens += source.fastForward(SoyToken.STRING_LITERAL_END, null);
            remainingValues = 0;
            if (delim == '"') {
                done(I18N.msg("syntax.error.string.literal.double.quotes"));
            } else {
                done();
            }
        } else if (token == SoyToken.EMPTY_ARRAY_LITERAL) {
            apparentType = apparentType.or(ExpressionType.ARRAY);
            markAsConstantExpressionAndAdvance();
        } else if (token == SoyToken.EMPTY_OBJECT_LITERAL) {
            apparentType = apparentType.or(ExpressionType.OBJECT);
            markAsConstantExpressionAndAdvance();
        } else if (token == SoyToken.PARAMETER_REF) {
            prec = PREC_LITERAL;
            expressionType = parameter_ref;
            source.advance();
            capturedTokens++;
            remainingValues = 0;
            done();
        } else if (token == SoyToken.LPAREN) {
            prec = PREC_PARENTHESIS;
            source.advance();
            capturedTokens++;
            ExpressionParser innerParser = new ExpressionParser(source);
            innerParser.expecting(expectingType).parse();
            apparentType = apparentType.or(innerParser.apparentType);
            capturedTokens += innerParser.capturedTokens;
            if (!source.eof() && source.token() == SoyToken.RPAREN) {
                source.advance();
                capturedTokens++;
            }
            remainingValues = 0;
        } else if (token == SoyToken.LBRACK && beginExpression(source.previous())) {
            prec = PREC_PARENTHESIS;
            source.advance();
            Object result = null;
            if (!source.eof()) {
                result = parseObjectLiteral();
            }
            if (result == ParsedLiteralObjectType.ARRAY) {
                expressionType = array_literal;
                apparentType = apparentType.or(ExpressionType.ARRAY);
            } else if (result == ParsedLiteralObjectType.MAP) {
                expressionType = object_literal;
                apparentType = apparentType.or(ExpressionType.OBJECT);
            } else if (result != null) {
                source.error(result.toString());
                exprMarker.drop();
                exprMarker = null;
                done();
                return this.parent;
            }
        } else if (token == SoyToken.LBRACK) {
            prec = PREC_PARENTHESIS;
            expressionType = bracket_property_ref;
            source.advance();
            capturedTokens++;
            ExpressionParser innerParser = new ExpressionParser(source);
            innerParser.expecting(expectingType).parse();
            apparentType = apparentType.or(innerParser.apparentType);
            capturedTokens += innerParser.capturedTokens;
            if (!source.eof() && source.token() == SoyToken.RBRACK) {
                source.advance();
                capturedTokens++;
            }
            remainingValues = 0;
        } else if (token == SoyToken.MINUS) {
            apparentType = apparentType.or(ExpressionType.NUMBER);
            prec = PREC_UMINUS_NOT;
            source.advance();
            capturedTokens++;
            remainingValues = 1;
        } else if (token == SoyToken.NOT) {
            apparentType = apparentType.or(ExpressionType.BOOLEAN);
            prec = PREC_UMINUS_NOT;
            source.advance();
            capturedTokens++;
            remainingValues = 1;
        } else if (token == SoyToken.CAPTURED_IDENTIFIER && parent != null && parent.prec == PREC_DOT) {
            prec = PREC_LITERAL;
            source.advance();
            capturedTokens++;
            remainingValues = 0;
        } else if (token == SoyToken.CAPTURED_IDENTIFIER) {
            prec = PREC_LITERAL;
            expressionType = global_expression;
            source.advance();
            capturedTokens++;
            remainingValues = 0;
            done();
        } else if (token == SoyToken.CAPTURED_FUNCTION_IDENTIFIER) {
            prec = PREC_FUNCTION;
            PsiBuilder.Marker beginFunction = source.mark("beginFunction");
            source.advanceAndMark(function_call_name, "function_call_name");
            if (!source.eof()) parseFunctionArgs(null);
            beginFunction.done(function_call);
            remainingValues = 0;
            return this;
        } else {
            exprMarker.drop();
            exprMarker = null;
            done();
            return this.parent;
        }
        return remainingValues == 0 ? this : new ExpressionParser(this);
    }

    private boolean beginExpression(IElementType previousToken) {
        return previousToken == null ||
               SoyToken.INITIAL_EXPRESSION_OPERATORS.contains(previousToken);
    }

    private Object parseObjectLiteral() {
        ObjectLiteralParseState state = new ObjectLiteralParseState();
        state.keyValueMarker = source.mark("key_value_pair");
        state.keyMarker = source.mark("key");
        state.valueMarker = source.mark("value");

        state.lastExpression = captureSingleExpression();
        state.token = source.eof() ? null : source.token();
        if (state.lastExpression != null && state.token == SoyToken.COLON) {
            state.parsingType = ParsedLiteralObjectType.MAP;
            state.valueMarker = drop(state.valueMarker);
        } else if (state.lastExpression != null && state.token == SoyToken.COMMA) {
            state.parsingType = ParsedLiteralObjectType.ARRAY;
            state.keyMarker = drop(state.keyMarker);
            state.keyValueMarker = drop(state.keyValueMarker);
        }
        while (state.token != null && (state.token == SoyToken.COLON || state.token == SoyToken.COMMA)) {
            parseObjectLiteralElement(state);
        }
        apparentType = apparentType.and(ExpressionType.OBJECT);
        if (state.parsingType != null && state.token == SoyToken.RBRACK) {
            close(state.valueMarker, value_literal);
            close(state.keyMarker, key_literal);
            close(state.keyValueMarker, key_value_literal);
            source.advance();
            return state.parsingType;
        } else {
            drop(state.valueMarker);
            drop(state.keyMarker);
            drop(state.keyValueMarker);
            if (state.token == null) {
                return null; // eof
            } else {
                return I18N.msg("syntax.error.unexpected.tokens.in.literal", state.token);
            }
        }
    }

    private void parseObjectLiteralElement(ObjectLiteralParseState state) {
        boolean expectKey = false;
        if (state.token == SoyToken.COLON) {
            if (state.parsingType == null) {
                state.parsingType = ParsedLiteralObjectType.MAP;
            }
            state.valueMarker = drop(state.valueMarker);
            if (state.keyMarker == null || state.lastExpression == null) {
                source.error(I18N.msg("syntax.error.unexpected.colon.in.literal"));
            } else if (state.lastExpression.isConstantOfType(ExpressionType.STRING)) {
                state.keyMarker = close(state.keyMarker, key_literal);
            } else if (state.lastExpression.prec == PREC_UNSPECIFIED) {
                state.keyMarker = drop(state.keyMarker);
            } else {
                state.keyMarker = error(state.keyMarker, "syntax.error.expected.string.key.in.literal");
            }
        } else { // SoyToken.COMMA
            if (state.parsingType == null) {
                state.parsingType = ParsedLiteralObjectType.ARRAY;
            }
            if (state.valueMarker == null) {
                source.error(I18N.msg("syntax.error.unexpected.comma.in.literal"));
            } else if (state.lastExpression == null) {
                state.valueMarker = error(state.valueMarker, "syntax.error.unexpected.comma.in.literal");
            } else if (state.lastExpression.prec == PREC_UNSPECIFIED) {
                state.valueMarker = drop(state.valueMarker);
            } else {
                state.valueMarker = close(state.valueMarker, value_literal);
            }
            state.keyMarker = error(state.keyMarker, "syntax.error.unexpected.comma.in.literal");
            state.keyValueMarker = close(state.keyValueMarker, key_value_literal);
            expectKey = state.parsingType == ParsedLiteralObjectType.MAP;
        }
        source.advance();
        if (!source.eof()) {
            if (expectKey) {
                state.keyValueMarker = source.mark("key_value_pair");
                state.keyMarker = source.mark("key");
            } else {
                state.valueMarker = source.mark("value");
            }
            if (SoyToken.EXPRESSION_TOKENS.contains(source.token())) {
                state.lastExpression = captureSingleExpression();
            } else {
                state.lastExpression = null;
            }
        }
        state.token = source.eof() ? null : source.token();
    }

    @Nullable
    private PsiBuilder.Marker close(@Nullable PsiBuilder.Marker marker, @NotNull IElementType type) {
        if (marker != null) {
            marker.done(type);
        }
        return null;
    }

    @Nullable
    private PsiBuilder.Marker error(@Nullable PsiBuilder.Marker marker, @NotNull @NonNls String errorMsg) {
        if (marker != null) {
            marker.error(I18N.msg(errorMsg));
        }
        return null;
    }

    @Nullable
    private PsiBuilder.Marker drop(@Nullable PsiBuilder.Marker marker) {
        if (marker != null) {
            marker.drop();
        }
        return null;
    }

    int parseFunctionArgs(@Nullable IElementType closeWith) {
        IElementType token = source.token();
        if (token != SoyToken.LPAREN) {
            source.advanceAndMarkBad(unexpected_symbol, "unexpected_symbol");
            return -1;
        }
        PsiBuilder.Marker beginCall = source.mark("beginCall");
        source.advance();
        capturedTokens++;
        if (source.eof()) {
            beginCall.drop();
            return -1;
        }

        int argCount = 0;
        PsiBuilder.Marker beginArgList = source.mark("beginArgList");
        if (parseSingleExpression()) argCount++;
        while (!source.eof()) {
            token = source.token();
            if (token != SoyToken.COMMA) {
                if (argCount == 0) beginArgList.drop();
                else beginArgList.done(function_call_arg_list);
                break;
            }
            source.advance();
            capturedTokens++;
            if (!source.eof()) {
                if (parseSingleExpression()) argCount++;
            }
        }

        if (source.eof()) {
            beginArgList.drop();
            beginCall.drop();
        } else {
            token = source.token();
            if (token != SoyToken.RPAREN) {
                source.error(I18N.msg("syntax.error.invalid.function.parameter.list"));
                beginCall.drop();
            } else {
                source.advance();
                capturedTokens++;
                beginCall.done(function_call_args);
            }
        }
        if (closeWith != null) {
            expressionType = closeWith;
            done();
        }
        return argCount;
    }

    private boolean parseSingleExpression() {
        ExpressionParser expressionParser = captureSingleExpression();
        return expressionParser != null && expressionParser.prec != PREC_UNSPECIFIED;
    }

    private ExpressionParser captureSingleExpression() {
        ExpressionParser expressionParser = new ExpressionParser(source).expecting(expectingType);
        expressionParser.parse();
        apparentType = apparentType.or(expressionParser.expectingType);
        capturedTokens += expressionParser.capturedTokens;
        return expressionParser.capturedTokens == 0 ? null : expressionParser;
    }

    private boolean isConstantOfType(ExpressionType expectedType) {
        return (expectedType == ExpressionType.ANY || expectedType == this.apparentType) &&
               expressionType == constant_expression &&
               prec != PREC_UNSPECIFIED;
    }

    private ExpressionParser push(int prec, int remainingValues) {
        ExpressionParser parser = this;

        while (parser.parent != null && parser.parent.prec >= prec && parser.parent.prec != PREC_PARENTHESIS) {
            parser.done();
            parser.parent.capturedTokens += parser.capturedTokens;
            parser = parser.parent;
        }

        PsiBuilder.Marker newMarker = parser.exprMarker.precede();
        parser.done();
        source.advance();
        parser.capturedTokens++;
        parser.expressionType = expression;
        parser.exprMarker = newMarker;
        parser.markerIsDone = false;
        parser.prec = prec;
        if (remainingValues == 0 || prec == PREC_TERNARY) {
            return parser;
        } else {
            parser.remainingValues = remainingValues;
            return new ExpressionParser(parser);
        }
    }

    private ExpressionParser parseResume(ExpressionParser parser, IElementType token) {
        if (token == SoyToken.DOT || token == SoyToken.QUESTION_DOT) {
            parser = parser.push(PREC_DOT, 1);
            parser.expressionType = member_property_ref;
        } else if (token == SoyToken.MULT || token == SoyToken.DIV || token == SoyToken.MOD) {
            parser = parser.push(PREC_MUL_DIV_MOD, 1);
        } else if (token == SoyToken.PLUS || token == SoyToken.MINUS) {
            parser = parser.push(PREC_ADD_SUB, 1);
        } else if (token == SoyToken.LT || token == SoyToken.LTEQ || token == SoyToken.GT || token == SoyToken.GTEQ) {
            parser = parser.push(PREC_LT_GT, 1);
        } else if (token == SoyToken.EQEQ || token == SoyToken.NOTEQ) {
            parser = parser.push(PREC_EQ, 1);
        } else if (token == SoyToken.AND) {
            parser = parser.push(PREC_AND, 1);
        } else if (token == SoyToken.OR) {
            parser = parser.push(PREC_OR, 1);
        } else if (token == SoyToken.QUESTION || token == SoyToken.ELVIS) {
            boolean ternary = token == SoyToken.QUESTION;
            parser = parser.push(PREC_TERNARY, 2);
            if (!source.eof()) {
                ExpressionParser leftParser = new ExpressionParser(source);
                leftParser.expecting(expectingType).parse();
                apparentType = leftParser.apparentType;
                parser.capturedTokens += leftParser.capturedTokens;
            }
            if (ternary && !source.eof()) {
                if (source.token() == SoyToken.COLON) {
                    parser.remainingValues = 1;
                    source.advance();
                    parser.capturedTokens++;
                    ExpressionParser rightParser = new ExpressionParser(source);
                    rightParser.expecting(expectingType).parse();
                    apparentType.or(rightParser.apparentType);
                    parser.capturedTokens += rightParser.capturedTokens;
                    parser.remainingValues--;
                    done();
                } else {
                    source.advanceAndMarkBad(expression, "expression", I18N.msg("syntax.error.expected.colon.in.ternary"));
                    parser.done();
                    return parser.parent;
                }
            }
        } else if (token == SoyToken.COLON) {
            parser.done();
            return parser.parent;
        } else if (SoyToken.EXPRESSION_TOKENS.contains(token)) {
            parser = new ExpressionParser(parser);
        } else if (parser.parent != null) {
            parser.parent.capturedTokens += parser.capturedTokens;
            parser.done();
            return parser.parent;
        } else {
            parser.done();
            return null;
        }
        return parser;
    }

    enum ParsedLiteralObjectType {
        MAP, ARRAY
    }

    static class ObjectLiteralParseState {
        ParsedLiteralObjectType parsingType;
        PsiBuilder.Marker keyValueMarker;
        PsiBuilder.Marker keyMarker;
        PsiBuilder.Marker valueMarker;
        ExpressionParser lastExpression;
        IElementType token;
    }

}
