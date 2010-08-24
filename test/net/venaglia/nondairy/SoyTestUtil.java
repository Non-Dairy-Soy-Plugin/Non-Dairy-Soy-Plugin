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

package net.venaglia.nondairy;

import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 21, 2010
 * Time: 7:25:22 PM
 */
@NonNls
@SuppressWarnings({ "HardCodedStringLiteral" })
public class SoyTestUtil {

    public static String fromSource(String source) {
        if (source == null || "null".equals(source)) return null;
        if (source.length() < 2 ||
            source.charAt(0) != source.charAt(source.length() - 1) ||
            "\"\'".indexOf(source.charAt(0)) == -1) {
            throw new IllegalArgumentException("Unable to parse string literal: " + source);
        }
        StringBuilder buffer = new StringBuilder(source.length());
        for (int i = 0, j = source.length(); i < j; ++i) {
            char c = source.charAt(i);
            if (c == '\\' && i < j - 1) {
                c = source.charAt(++i);
                switch (c) {
                    case '\\':
                    case '\"':
                    case '\'': buffer.append(c); break;
                    case 'r': buffer.append('\r'); break;
                    case 'n': buffer.append('\n'); break;
                    case 't': buffer.append('\t'); break;
                    case 'f': buffer.append('\f'); break;
                }
            } else {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }

    public static String toSource(String text) {
        if (text == null) return "null";
        StringBuilder buffer = new StringBuilder(text.length() + 10);
        buffer.append("\"");
        for (int i = 0, j = text.length(); i < j; ++i) {
            char c = text.charAt(i);
            if (c > 127) {
                buffer.append(String.format("\\u%04x", (int)c));
            } else if (c < 8) {
                buffer.append("\\").append((int)c);
            } else {
                switch (c) {
                    case '\t': buffer.append("\\t"); break;
                    case '\f': buffer.append("\\f"); break;
                    case '\b': buffer.append("\\b"); break;
                    case '\n': buffer.append("\\n"); break;
                    case '\r': buffer.append("\\r"); break;
                    case '\'':
                    case '\"':
                    case '\\': buffer.append("\\").append(c); break;
                    default: buffer.append(c); break;
                }
            }
        }
        buffer.append("\"");
        return buffer.toString();
    }

    public static String getTestSourceBuffer(String name) throws IOException {
        Reader in = new InputStreamReader(SoyTestUtil.class.getResourceAsStream("testSources/" + name));
        StringWriter out = new StringWriter(16384);
        char[] buffer = new char[4096];
        for (int i = 0; i > -1; i = in.read(buffer)) {
            out.write(buffer, 0, i);
        }
        in.close();
        return out.getBuffer().toString();
    }
}
