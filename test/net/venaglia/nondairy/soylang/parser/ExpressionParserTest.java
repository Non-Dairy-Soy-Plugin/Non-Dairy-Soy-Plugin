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

package net.venaglia.nondairy.soylang.parser;

import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 16, 2010
 * Time: 7:54:19 AM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public class ExpressionParserTest extends BaseParserTest {

    public static final String SIMPLE_EXPRESSION_SOURCE =
            "$value > 6 / 2 + 5 ? 'too many' : 'too few'";

    public static final String SIMPLE_EXPRESSION_EXPECT =
            "expression:{\n" +
            "    expression:{\n" +
            "        parameter_ref:{\n" +
            "            PARAMETER_REF\n" +
            "        }\n" +
            "        GT\n" +
            "        expression:{\n" +
            "            expression:{\n" +
            "                constant_expression:{\n" +
            "                    INTEGER_LITERAL\n" +
            "                }\n" +
            "                DIV\n" +
            "                constant_expression:{\n" +
            "                    INTEGER_LITERAL\n" +
            "                }\n" +
            "            }\n" +
            "            PLUS\n" +
            "            constant_expression:{\n" +
            "                INTEGER_LITERAL\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    QUESTION\n" +
            "    constant_expression:{\n" +
            "        STRING_LITERAL_BEGIN\n" +
            "        STRING_LITERAL\n" +
            "        STRING_LITERAL_END\n" +
            "    }\n" +
            "    COLON\n" +
            "    constant_expression:{\n" +
            "        STRING_LITERAL_BEGIN\n" +
            "        STRING_LITERAL\n" +
            "        STRING_LITERAL_END\n" +
            "    }\n" +
            "}";

    public static final String SIMPLE_DOT_EXPRESSION_SOURCE =
            "1 + $value.child.length";

    public static final String SIMPLE_DOT_EXPRESSION_EXPECT =
            "expression:{\n" +
            "    constant_expression:{\n" +
            "        INTEGER_LITERAL\n" +
            "    }\n" +
            "    PLUS\n" +
            "    expression:{\n" +
            "        expression:{\n" +
            "            parameter_ref:{\n" +
            "                PARAMETER_REF\n" +
            "            }\n" +
            "            DOT\n" +
            "            member_property_ref:{\n" +
            "                CAPTURED_IDENTIFIER\n" +
            "            }\n" +
            "        }\n" +
            "        DOT\n" +
            "        member_property_ref:{\n" +
            "            CAPTURED_IDENTIFIER\n" +
            "        }\n" +
            "    }\n" +
            "}";

    public static final String BINARY_EXPRESSION_SOURCE_1 =
            "$a + $b + $c + $d";

    public static final String BINARY_EXPRESSION_EXPECT_1 =
            "expression:{\n" +
            "    expression:{\n" +
            "        expression:{\n" +
            "            parameter_ref:{\n" +
            "                PARAMETER_REF\n" +
            "            }\n" +
            "            PLUS\n" +
            "            parameter_ref:{\n" +
            "                PARAMETER_REF\n" +
            "            }\n" +
            "        }\n" +
            "        PLUS\n" +
            "        parameter_ref:{\n" +
            "            PARAMETER_REF\n" +
            "        }\n" +
            "    }\n" +
            "    PLUS\n" +
            "    parameter_ref:{\n" +
            "        PARAMETER_REF\n" +
            "    }\n" +
            "}";

    public static final String BINARY_EXPRESSION_SOURCE_2 =
            "$a / $b + $c > $d";


    public static final String BINARY_EXPRESSION_EXPECT_2 =
            "expression:{\n" +
            "    expression:{\n" +
            "        expression:{\n" +
            "            parameter_ref:{\n" +
            "                PARAMETER_REF\n" +
            "            }\n" +
            "            DIV\n" +
            "            parameter_ref:{\n" +
            "                PARAMETER_REF\n" +
            "            }\n" +
            "        }\n" +
            "        PLUS\n" +
            "        parameter_ref:{\n" +
            "            PARAMETER_REF\n" +
            "        }\n" +
            "    }\n" +
            "    GT\n" +
            "    parameter_ref:{\n" +
            "        PARAMETER_REF\n" +
            "    }\n" +
            "}";

    public static final String PRECEDENCE_EXPRESSION_SOURCE =
            "$a > $b + $c / $d";

    public static final String PRECEDENCE_EXPRESSION_EXPECT =
            "expression:{\n" +
            "    parameter_ref:{\n" +
            "        PARAMETER_REF\n" +
            "    }\n" +
            "    GT\n" +
            "    expression:{\n" +
            "        parameter_ref:{\n" +
            "            PARAMETER_REF\n" +
            "        }\n" +
            "        PLUS\n" +
            "        expression:{\n" +
            "            parameter_ref:{\n" +
            "                PARAMETER_REF\n" +
            "            }\n" +
            "            DIV\n" +
            "            parameter_ref:{\n" +
            "                PARAMETER_REF\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

    public static final String PARENTHESIS_EXPRESSION_SOURCE =
            "(($a > $b) + $c) / $d";

    public static final String PARENTHESIS_EXPRESSION_EXPECT =
            "expression:{\n" +
            "    expression:{\n" +
            "        LPAREN\n" +
            "        expression:{\n" +
            "            expression:{\n" +
            "                LPAREN\n" +
            "                expression:{\n" +
            "                    parameter_ref:{\n" +
            "                        PARAMETER_REF\n" +
            "                    }\n" +
            "                    GT\n" +
            "                    parameter_ref:{\n" +
            "                        PARAMETER_REF\n" +
            "                    }\n" +
            "                }\n" +
            "                RPAREN\n" +
            "            }\n" +
            "            PLUS\n" +
            "            parameter_ref:{\n" +
            "                PARAMETER_REF\n" +
            "            }\n" +
            "        }\n" +
            "        RPAREN\n" +
            "    }\n" +
            "    DIV\n" +
            "    parameter_ref:{\n" +
            "        PARAMETER_REF\n" +
            "    }\n" +
            "}";

    public static final String BRACKET_EXPRESSION_SOURCE =
            "$var[0x10]";

    public static final String BRACKET_EXPRESSION_EXPECT =
            "expression:{\n" +
            "    parameter_ref:{\n" +
            "        PARAMETER_REF\n" +
            "    }\n" +
            "    bracket_property_ref:{\n" +
            "        LBRACK\n" +
            "        constant_expression:{\n" +
            "            INTEGER_LITERAL\n" +
            "        }\n" +
            "        RBRACK\n" +
            "    }\n" +
            "}";

    @Override
    protected void parseImpl(TokenSource tokenSource) {
        new ExpressionParser(tokenSource).parse();
    }

    @Test
    public void testSimpleExpression() throws Exception {
        testParseSequence(SIMPLE_EXPRESSION_SOURCE, SIMPLE_EXPRESSION_EXPECT, "SOY_TAG", null);
    }

    @Test
    public void testSimpleDotExpression() throws Exception {
        testParseSequence(SIMPLE_DOT_EXPRESSION_SOURCE, SIMPLE_DOT_EXPRESSION_EXPECT, "SOY_TAG", null);
    }

    @Test
    public void testBinaryExpression1() throws Exception {
        testParseSequence(BINARY_EXPRESSION_SOURCE_1, BINARY_EXPRESSION_EXPECT_1, "SOY_TAG", null);
    }

    @Test
    public void testBinaryExpression2() throws Exception {
        testParseSequence(BINARY_EXPRESSION_SOURCE_2, BINARY_EXPRESSION_EXPECT_2, "SOY_TAG", null);
    }

    @Test
    public void testPrecedenceExpression() throws Exception {
        testParseSequence(PRECEDENCE_EXPRESSION_SOURCE, PRECEDENCE_EXPRESSION_EXPECT, "SOY_TAG", null);
    }

    @Test
    public void testParenthesisExpression() throws Exception {
        testParseSequence(PARENTHESIS_EXPRESSION_SOURCE, PARENTHESIS_EXPRESSION_EXPECT, "SOY_TAG", null);
    }

    @Test
    public void testBracketExpression() throws Exception {
        testParseSequence(BRACKET_EXPRESSION_SOURCE, BRACKET_EXPRESSION_EXPECT, "SOY_TAG", null);
    }

    @Test
    public void testMissingOperand() throws Exception {
        testParseSequence("4 * }", "SOY_TAG", null);
    }

    @Test
    public void testMissingParenthesis() throws Exception {
        testParseSequence("(4 * 5 }", "SOY_TAG", null);
    }

    @Test
    public void testEofInStringLiteralToFunctionCall() throws Exception {
        testParseSequence("myFunc('foo-bar", "SOY_TAG", null);
    }

//    @Test
//    public void testExtraParenthesis() throws Exception {
//        testParseSequence("(4 * 5)) }", "SOY_TAG");
//    }

//    @Test
//    public void testLoneClosingParenthesis() throws Exception {
//        testParseSequence(") }", "SOY_TAG");
//    }
}
