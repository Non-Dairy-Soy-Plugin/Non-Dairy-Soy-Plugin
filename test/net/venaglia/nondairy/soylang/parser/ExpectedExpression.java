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
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import net.venaglia.nondairy.util.Formats;
import org.junit.Assert;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 15, 2010
 * Time: 5:15:50 PM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public class ExpectedExpression {

    private static final boolean DISPLAY_EXPECTED_STRUCTURE = true;

    private static final String EXPECTED_EXPRESSION_BANNER =
            "-----[ %s ]-----------------------------------------------------------------------";

    private static final Class<?> TOKEN_SOURCES[] = { SoyToken.class, SoyElement.class};

    static { Assert.assertTrue("No classes specified in TOKEN_SOURCES[]", TOKEN_SOURCES.length > 0); }

    public static final Pattern MATCH_TREE_TOKEN = Pattern.compile("([a-z][a-z0-9_]*)(:\\{|\\*[1-9]\\d*)?|\\}|\\[([a-z]+)(:\"([^\"]+|\\['\"\\\\rntf])*\")?\\]", Pattern.CASE_INSENSITIVE);
    public static final Pattern MATCH_META_TOKEN = Pattern.compile("\\[([a-z]+)(:\"([^\"]+|\\['\"\\\\rntf])*\")?\\]", Pattern.CASE_INSENSITIVE);

    private final int capturedTokenCount;

    private List<Object> children = new LinkedList<Object>();

    private static Object getToken(String name) {
        Field field;
        for (Class<?> tokenSource : TOKEN_SOURCES) {
            try {
                field = tokenSource.getField(name);
            } catch (NoSuchFieldException e) {
                continue; // not field by this name, not it
            }
            if (!field.getType().equals(tokenSource)) continue; // wrong type, not it
            int modifiers = field.getModifiers();
            if (!Modifier.isPublic(modifiers)) continue; // not a public field, not it
            if (!Modifier.isStatic(modifiers)) continue; // not a static field, not it
            if (!Modifier.isFinal(modifiers)) continue; // not a final field, not it
            try {
                return field.get(null);
            } catch (IllegalAccessException e) {
                // shouldn't happen because we already checked the access
            }
        }
        Assert.fail("Could not find: public static final TOKEN " + name); //NON-NLS
        return null; // never reached
    }

    private ExpectedExpression(Deque<String> tokens, int depth) {
        int tokenCounter = 0;
        boolean first = true;
        while (true) {
            String token = tokens.peek();
            if (DISPLAY_EXPECTED_STRUCTURE) {
                if (token != null) {
                    System.out.println();
                    System.out.print(Formats.indent(depth + ("}".equals(token) ? -1 : 0)) + token);
                }
            }
            if (token == null) {
                Assert.assertFalse("Expect expression syntax error: Unexpected token: " + tokens.poll(), first); //NON-NLS
                Assert.assertEquals(0, depth);
                break;
            } else if ("}".equals(token)) {
                Assert.assertFalse("Expect expression syntax error: Unexpected token: " + tokens.poll(), first); //NON-NLS
                break;
            } else if (token.endsWith(":{")) {
                String name = tokens.poll().substring(0, token.length() - 2);
                ExpectedExpression expression = new ExpectedExpression(tokens, depth + 1);
                tokenCounter += expression.capturedTokenCount;
                children.add(expression);
                children.add(getToken(name));
                children.add(expression.capturedTokenCount);
            } else if (token.indexOf('*') > 0) {
                String[] tokenParts = token.split("\\*");
                for (int i = 0, j = Integer.parseInt(tokenParts[1]); i < j; ++i) {
                    children.add(getToken(tokenParts[0]));
                    tokenCounter++;
                }
                tokens.poll();
            } else {
                Matcher matcher = MATCH_META_TOKEN.matcher(token);
                if (matcher.find()) {
                    String name = matcher.group(1);
                    String value = SoyTestUtil.fromSource(matcher.group(3));
                    children.add(new MockParseMetaToken(name, value, null));
                }
                children.add(getToken(tokens.poll()));
                tokenCounter++;
            }
            first = false;
        }
        capturedTokenCount = tokenCounter;
        if (DISPLAY_EXPECTED_STRUCTURE) {
            if (!tokens.isEmpty()) {
                System.out.print("[" + capturedTokenCount + "]");
            }
        }
    }

    private void buildSequence(Deque<Object> buffer) {
        for (Object child : children) {
            if (child instanceof ExpectedExpression) {
                ((ExpectedExpression)child).buildSequence(buffer);
            } else {
                buffer.add(child);
            }
        }
    }

    private static String banner (String message) {
        return String.format(EXPECTED_EXPRESSION_BANNER, message).substring(0, 78);
    }

    public static Deque<Object> getExpectedSequence(CharSequence source) throws IOException {
        Deque<String> seq = new LinkedList<String>();
        Matcher matcher = MATCH_TREE_TOKEN.matcher(source);
        while (matcher.find()) {
            String token = matcher.group();
            seq.add(token);
        }
        Deque<Object> sequence = new LinkedList<Object>();
        if (DISPLAY_EXPECTED_STRUCTURE) {
            System.out.print(banner("Expected Structure : start")); //NON-NLS
        }
        new ExpectedExpression(seq, 0).buildSequence(sequence);
        if (DISPLAY_EXPECTED_STRUCTURE) {
            System.out.println();
            System.out.println(banner("Expected Structure : end")); //NON-NLS
            System.out.println();
        }
        return sequence;
    }
}
