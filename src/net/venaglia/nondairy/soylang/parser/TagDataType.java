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

import net.venaglia.nondairy.util.Assert;

/**
* Created by IntelliJ IDEA.
* User: ed
* Date: Aug 14, 2010
* Time: 5:02:51 PM
*/
enum TagDataType {

    COMMAND, NAME, EXPRESSION, ATTRIBUTES, DIRECTIVES;

    public static final TagDataType[] TAG_DATA_TYPES = TagDataType.class.getEnumConstants();

    static {
        // Since this enum uses bit masking, this simple check makes sure we
        // never exceed the number of bits safely available.
        Assert.maxEnumValuesLessThan(TagDataType.class, 32);
    }

    private final int bit = 1 << ordinal();

    public int value() {
        return bit;
    }

    public int removeExclusive(int a) {
        return a & ~(bit - 1);
    }

    public int removeInclusive(int a) {
        return a & ~((bit << 1) - 1);
    }

    public boolean containedIn(int expected) {
        return (expected & bit) == bit;
    }

    public static TagDataType nextIn(int expected) {
        if (expected == 0) return null;
        int i = 0;
        while ((expected & 1) == 0) {
            expected >>= 1;
            i++;
        }
        return i >= TAG_DATA_TYPES.length ? null : TAG_DATA_TYPES[i];
    }
}
