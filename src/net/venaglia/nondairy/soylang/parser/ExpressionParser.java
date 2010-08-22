package net.venaglia.nondairy.soylang.parser;

import static net.venaglia.nondairy.soylang.SoyElement.*;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.soylang.lexer.SoyToken;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 14, 2010
 * Time: 11:24:19 PM
 */
class ExpressionParser {

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
    private boolean markerIsDone = false;

    ExpressionParser(TokenSource source) {
        this.parent = null;
        this.source = source;
        this.exprMarker = source.mark();
    }

    ExpressionParser(ExpressionParser parent) {
        this.parent = parent;
        this.source = parent.source;
        this.exprMarker = source.mark();
    }

    private void done() {
        if (!markerIsDone) {
            if (parent != null) parent.remainingValues -= 1;
            if (remainingValues > 0) {
                source.error(I18N.msg("syntax.error.expected.expression"));
                remainingValues = 0;
            }
            if (exprMarker != null) {
                exprMarker.done(expressionType);
            }
            markerIsDone = true;
        }
    }

    void parse() {
        ExpressionParser parser = this;
        boolean advanceOnExit = false;
        while (!source.eof() && parser != null) {
            IElementType token = source.token();
            if (token == SoyToken.RPAREN || token == SoyToken.RBRACK || token == SoyToken.COMMA) {
//                advanceOnExit = parser.prec != PREC_PARENTHESIS;
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
        }
        while (parser != null) {
            parser.done();
            if (parser == this) break;
            parser = parser.parent;
        }
        if (advanceOnExit) source.advance();
    }

    private ExpressionParser parseInitial(IElementType token) {
        if (token == SoyToken.NULL_LITERAL || token == SoyToken.BOOLEAN_LITERAL || token == SoyToken.INTEGER_LITERAL || token == SoyToken.FLOATING_POINT_LITERAL) {
            prec = PREC_LITERAL;
            source.advance();
            remainingValues = 0;
            done();
        } else if (token == SoyToken.STRING_LITERAL_BEGIN) {
            prec = PREC_LITERAL;
            source.fastForward(SoyToken.STRING_LITERAL_END, null);
            remainingValues = 0;
            done();
        } else if (token == SoyToken.PARAMETER_REF) {
            prec = PREC_LITERAL;
            source.advance();
            remainingValues = 0;
            done();
        } else if (token == SoyToken.LPAREN) {
            prec = PREC_PARENTHESIS;
            source.advance();
            new ExpressionParser(source).parse();
            if (!source.eof() && source.token() == SoyToken.RPAREN) source.advance();
            remainingValues = 0;
        } else if (token == SoyToken.LBRACK) {
            prec = PREC_PARENTHESIS;
            source.advance();
            new ExpressionParser(source).parse();
            if (!source.eof() && source.token() == SoyToken.RBRACK) source.advance();
            remainingValues = 0;
        } else if (token == SoyToken.MINUS) {
            prec = PREC_UMINUS_NOT;
            source.advance();
            remainingValues = 1;
        } else if (token == SoyToken.NOT) {
            prec = PREC_UMINUS_NOT;
            source.advance();
            remainingValues = 1;
        } else if (token == SoyToken.CAPTURED_IDENTIFIER && parent != null && parent.prec == PREC_DOT) {
            prec = PREC_LITERAL;
            source.advance();
            remainingValues = 0;
        } else if (token == SoyToken.CAPTURED_FUNCTION_IDENTIFIER) {
            prec = PREC_FUNCTION;
            PsiBuilder.Marker beginFunction = source.mark();
            source.advanceAndMark(function_call_name);
            if (!source.eof()) parseFunctionArgs();
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

    private void parseFunctionArgs() {
        IElementType token = source.token();
        if (token != SoyToken.LPAREN) {
            source.advanceAndMarkBad(unexpected_symbol);
            return;
        }
        PsiBuilder.Marker beginCall = source.mark();
        source.advance();
        if (source.eof()) {
            beginCall.drop();
            return;
        }

        int argCount = 0;
        PsiBuilder.Marker beginArgList = source.mark();
        new ExpressionParser(source).parse();
        while (!source.eof()) {
            token = source.token();
            if (token != SoyToken.COMMA) {
                if (argCount == 0) beginArgList.drop();
                else beginArgList.done(function_call_arg_list);
                break;
            }
            source.advance();
            if (!source.eof()) {
                argCount++;
                new ExpressionParser(source).parse();
            }
        }

        if (!source.eof()) {
            token = source.token();
            if (token != SoyToken.RPAREN) {
                source.error(I18N.msg("syntax.error.invalid.function.parameter.list"));
            } else {
                source.advance();
                beginCall.done(function_call_args);
            }
        }
    }

    private ExpressionParser push(int prec, int remainingValues) {
        ExpressionParser parser = this;

        while (parser.parent != null && parser.parent.prec >= prec && parser.parent.prec != PREC_PARENTHESIS) {
            parser.done();
            parser = parser.parent;
        }

        if (parser.prec == prec && prec == PREC_TERNARY && parser.remainingValues == remainingValues && remainingValues > 0) {
            source.advance();
            return parser;
        }

        PsiBuilder.Marker newMarker = parser.exprMarker.precede();
        parser.done();
        source.advance();
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
        if (token == SoyToken.DOT) {
            parser = parser.push(PREC_DOT, 1);
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
        } else if (token == SoyToken.QUESTION) {
            parser = parser.push(PREC_TERNARY, 2);
            if (!source.eof()) {
                new ExpressionParser(source).parse();
            }
            if (!source.eof()) {
                if (source.token() == SoyToken.COLON) {
                    parser.remainingValues = 1;
                    source.advance();
                    new ExpressionParser(source).parse();
                    parser.remainingValues--;
                    done();
                } else {
                    source.advanceAndMarkBad(expression, I18N.msg("syntax.error.expected.colon"));
                    parser.done();
                    return parser.parent;
                }
            }
        } else if (token == SoyToken.COLON) {
            parser.done();
            return parser.parent;
        } else if (SoyToken.EXPRESSION_TOKENS.contains(token)) {
            parser = new ExpressionParser(parser);
        } else {
            parser.done();
            return parser.parent;
        }
        return parser;
    }
}
