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
import org.jetbrains.annotations.NonNls;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

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
        testNoErrorsImpl("minimal.soy");
    }

    @Test
    public void testExample() throws Exception {
        testNoErrorsImpl("example.soy");
    }

    @Test
    public void testFeatures() throws Exception {
        testNoErrorsImpl("features.soy");
    }

    @Test
    public void testEdgeCases() throws Exception {
        testNoErrorsImpl("edge-cases.soy");
    }

    @Test
    public void testDelegates() throws Exception {
        testNoErrorsImpl("delegates.soy");
    }

    @Test
    public void testErrorCases() throws Exception {
        testParseSequence(SoyTestUtil.getTestSourceBuffer("error-cases.soy"), "YYINITIAL", "error-cases.soy");
    }

    @Test
    public void testInMultipleThreads() throws Exception {
        @NonNls
        String[] sources = {"example.soy","features.soy","edge-cases.soy", "example.soy","features.soy","edge-cases.soy"};
        SoyTestRunner[] threads = new SoyTestRunner[sources.length];
        for (int i = 0; i < threads.length; i++) {
            @NonNls String threadNameFormat = "soy test %d: %s";
            String name = String.format(threadNameFormat, i, sources[i]);
            threads[i] = new SoyTestRunner(TimeUnit.SECONDS, 15, sources[i], name);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        for (SoyTestRunner thread : threads) {
            thread.representFail();
        }
    }

    private void testNoErrorsImpl(@NonNls String resourceName) throws Exception {
        testParseSequence(SoyTestUtil.getTestSourceBuffer(resourceName), "YYINITIAL", MockParseMetaToken.ASSERT_NO_ERRORS, resourceName);
    }

    private class SoyTestRunner extends Thread {

        private final long millis;
        private final String source;

        private Exception fail = null;

        public SoyTestRunner(TimeUnit unit,
                             int duration,
                             String source,
                             String name) {
            super(name);
            this.source = source;
            millis = unit.toMillis(duration);
        }

        @Override
        public void run() {
            long end = System.currentTimeMillis() + millis;
            try {
                do {
                    testNoErrorsImpl(source);
                } while (System.currentTimeMillis() < end);
            } catch (Exception e) {
                fail = e;
            }
        }

        public void representFail() throws Exception {
            if (fail != null) {
                throw fail;
            }
        }
    }
}
