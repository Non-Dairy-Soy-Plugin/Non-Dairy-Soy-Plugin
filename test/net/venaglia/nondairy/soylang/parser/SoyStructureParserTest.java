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

package net.venaglia.nondairy.soylang.parser;

import net.venaglia.nondairy.SoyTestUtil;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 18, 2010
 * Time: 10:39:10 PM
 */
public class SoyStructureParserTest extends BaseParserTest {

    @Override
    protected void parseImpl(TokenSource tokenSource) {
        new SoyStructureParser(tokenSource).parse();
    }

    @Test
    public void testMinimal() throws Exception {
        testParseSequence(SoyTestUtil.getTestSourceBuffer("minimal.soy"), "YYINITIAL", MockParseMetaToken.ASSERT_NO_ERRORS);
    }

    @Test
    public void testExample() throws Exception {
        testParseSequence(SoyTestUtil.getTestSourceBuffer("example.soy"), "YYINITIAL", MockParseMetaToken.ASSERT_NO_ERRORS);
    }

    @Test
    public void testFeatures() throws Exception {
        testParseSequence(SoyTestUtil.getTestSourceBuffer("features.soy"), "YYINITIAL", MockParseMetaToken.ASSERT_NO_ERRORS);
    }

    @Test
    public void testEdgeCases() throws Exception {
        testParseSequence(SoyTestUtil.getTestSourceBuffer("edge-cases.soy"), "YYINITIAL", MockParseMetaToken.ASSERT_NO_ERRORS);
    }

}
