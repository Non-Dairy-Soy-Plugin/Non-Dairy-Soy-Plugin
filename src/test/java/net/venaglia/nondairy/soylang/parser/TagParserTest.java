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

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 16, 2010
 * Time: 6:28:47 PM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public class TagParserTest extends BaseParserTest {

    public static final String SIMPLE_TAG_SOURCE =
            "{namespace test.namespace}";

    public static final String SIMPLE_TAG_EXPECT =
            "namespace_def:{\n" +
            "    TAG_LBRACE\n" +
            "    tag_between_braces:{\n" +
            "        command_keyword:{\n" +
            "            NAMESPACE\n" +
            "        }\n" +
            "        namespace_name:{\n" +
            "            NAMESPACE_IDENTIFIER\n" +
            "        }\n" +
            "    }\n" +
            "    TAG_RBRACE\n" +
            "}";

    public static final String ATTRIBUTE_TAG_SOURCE =
            "{namespace test.namespace autoescape=\"contextual\"}";

    public static final String ATTRIBUTE_TAG_EXPECT =
            "namespace_def:{\n" +
            "    TAG_LBRACE\n" +
            "    tag_between_braces:{\n" +
            "        command_keyword:{\n" +
            "            NAMESPACE\n" +
            "        }\n" +
            "        namespace_name:{\n" +
            "            NAMESPACE_IDENTIFIER\n" +
            "        }\n" +
            "        attribute:{\n" +
            "            attribute_key:{\n" +
            "                CAPTURED_IDENTIFIER\n" +
            "            }\n" +
            "            EQ\n" +
            "            expression:{\n" +
            "                STRING_LITERAL_BEGIN\n" +
            "                attribute_value:{\n" +
            "                    STRING_LITERAL\n" +
            "                }\n" +
            "                STRING_LITERAL_END\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    TAG_RBRACE\n" +
            "}";

    public static final String SIMPLE_UNARY_TAG_SOURCE =
            "{call .testTemplate/}";

    public static final String SIMPLE_UNARY_TAG_EXPECT =
            "call_tag_pair:{\n" +
            "    call_tag:{\n" +
            "        TAG_LBRACE\n" +
            "        tag_between_braces:{\n" +
            "            command_keyword:{\n" +
            "                CALL\n" +
            "            }\n" +
            "            template_name_ref:{\n" +
            "                TEMPLATE_IDENTIFIER\n" +
            "            }\n" +
            "        }\n" +
            "        TAG_END_RBRACE\n" +
            "    }\n" +
            "}";
    public static final String EXPRESSION_TAG_SOURCE =
            "{$student.year < 2000 ? round($student.year - 1905, -1) + 's' : '00s'}";

    public static final String EXPRESSION_TAG_EXPECT =
            "tag:{\n" +
            "    TAG_LBRACE\n" +
            "    tag_between_braces:{\n" +
            "        PRINT_IMPLICIT\n" +
            "        expression:{\n" +
            "            expression:{\n" +
            "                expression:{\n" +
            "                    parameter_ref:{\n" +
            "                        PARAMETER_REF\n" +
            "                    }\n" +
            "                    DOT\n" +
            "                    member_property_ref:{\n" +
            "                        CAPTURED_IDENTIFIER\n" +
            "                    }\n" +
            "                }\n" +
            "                LT\n" +
            "                constant_expression:{\n" +
            "                    INTEGER_LITERAL\n" +
            "                }\n" +
            "            }\n" +
            "            QUESTION\n" +
            "            expression:{\n" +
            "                expression:{\n" +
            "                    function_call:{\n" +
            "                        function_call_name:{\n" +
            "                            CAPTURED_FUNCTION_IDENTIFIER\n" +
            "                        }\n" +
            "                        function_call_args:{\n" +
            "                            LPAREN\n" +
            "                            function_call_arg_list:{\n" +
            "                                expression:{\n" +
            "                                    expression:{\n" +
            "                                        parameter_ref:{\n" +
            "                                            PARAMETER_REF\n" +
            "                                        }\n" +
            "                                        DOT\n" +
            "                                        member_property_ref:{\n" +
            "                                            CAPTURED_IDENTIFIER\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                    MINUS\n" +
            "                                    constant_expression:{\n" +
            "                                        INTEGER_LITERAL\n" +
            "                                    }\n" +
            "                                }\n" +
            "                                COMMA\n" +
            "                                expression:{\n" +
            "                                    MINUS\n" +
            "                                    constant_expression:{\n" +
            "                                        INTEGER_LITERAL\n" +
            "                                    }\n" +
            "                                }\n" +
            "                            }\n" +
            "                            RPAREN\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "                PLUS\n" +
            "                constant_expression:{\n" +
            "                    STRING_LITERAL_BEGIN\n" +
            "                    STRING_LITERAL\n" +
            "                    STRING_LITERAL_END\n" +
            "                }\n" +
            "            }\n" +
            "            COLON\n" +
            "            constant_expression:{\n" +
            "                STRING_LITERAL_BEGIN\n" +
            "                STRING_LITERAL\n" +
            "                STRING_LITERAL_END\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    TAG_RBRACE\n" +
            "}";

    @Override
    protected void parseImpl(TokenSource tokenSource) {
        new TagParser(tokenSource).parse();
    }

    @Test
    public void testSimpleTag() throws Exception {
        testParseSequence(SIMPLE_TAG_SOURCE, SIMPLE_TAG_EXPECT, "YYINITIAL", null);
    }

    @Test
    public void testAttributeTag() throws Exception {
        testParseSequence(ATTRIBUTE_TAG_SOURCE, ATTRIBUTE_TAG_EXPECT, "YYINITIAL", null);
    }

    @Test
    public void testSimpleUnaryTag() throws Exception {
        testParseSequence(SIMPLE_UNARY_TAG_SOURCE, SIMPLE_UNARY_TAG_EXPECT, "HTML_INITIAL", null);
    }

    @Test
    public void testExpressionTag() throws Exception {
        testParseSequence(EXPRESSION_TAG_SOURCE, EXPRESSION_TAG_EXPECT, "HTML_INITIAL", null);
    }

    @Test
    public void testUnfinishedTag() throws Exception {
        testParseSequence("{tem", "YYINITIAL", null);
    }

    @Test
    public void testUnclosedTag() throws Exception {
        testParseSequence("{template .foo", "YYINITIAL", null);
    }

    @Test
    public void testUnclosedPrintImplicitTag() throws Exception {
        testParseSequence("{$broken", "HTML_INITIAL", null);
    }

    @Test
    public void testUnclosedTagPair() throws Exception {
        testParseSequence("{template .foo}", "YYINITIAL", null);
    }

    @Test
    public void testTagCloseInAttributeValue() throws Exception {
        testParseSequence("{template private=\"true}", "YYINITIAL", null);
    }

    @Test
    public void testTagCloseInStringLiteral() throws Exception {
        testParseSequence("{length($list) == 1 ? 'item' : 'items}", "HTML_INITIAL", null);
    }

}
