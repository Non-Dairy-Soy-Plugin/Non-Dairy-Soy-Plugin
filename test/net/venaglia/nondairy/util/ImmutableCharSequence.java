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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: ed
 * Date: 3/31/12
 * Time: 5:09 PM
 *
 * Immutable char sequence implementation for testing
 */
public class ImmutableCharSequence implements CharSequence {

    private int hash;

    private final CharSequence val;
    private final int len;

    public ImmutableCharSequence(@NonNls @NotNull CharSequence val) {
        this.val = val;
        this.len = val.length();
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public char charAt(int index) {
        return val.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return val.subSequence(start, end);
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && len > 0) {
            int off = 0;
            for (int i = 0; i < len; i++) {
                h = 31 * h + val.charAt(off++);
            }
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CharSequence)) {
            return false;
        }

        CharSequence cs = (CharSequence)o;
        if (len != cs.length()) {
            return false;
        }

        for (int i = 0; i < len; ++i) {
            if (val.charAt(i) != cs.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
