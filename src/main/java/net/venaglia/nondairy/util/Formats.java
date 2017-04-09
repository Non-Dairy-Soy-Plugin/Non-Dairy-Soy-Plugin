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

package net.venaglia.nondairy.util;

import java.util.Arrays;

/**
 * User: ed
 * Date: Aug 16, 2010
 * Time: 10:19:46 AM
 *
 * String formatting utilities class.
 */
public class Formats {

    // Used when printing formatted output for debugging this mini-parser
    public static final String INDENT;

    static {
        char[] indent = new char[128];
        Arrays.fill(indent, ' ');
        INDENT = new String(indent);
    }

    /**
     * Builds a whitespace string for prefixing lines at the specified indent level.
     * @param depth The nesting level to build an indent string for.
     * @return a whitespace string, suitable for indenting nested structures at
     *     the specified nesting level.
     */
    public static CharSequence indent(int depth) {
        return Formats.INDENT.subSequence(0, depth * 4);
    }
}
