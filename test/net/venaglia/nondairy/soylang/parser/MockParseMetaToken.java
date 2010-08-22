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
import org.junit.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 15, 2010
 * Time: 8:05:31 PM
 */
@SuppressWarnings({ "HardCodedStringLiteral" })
public class MockParseMetaToken {

    public static final EventDelegate ASSERT_NO_ERRORS = new EventDelegate() {
        public void event(Object value, MockTokenSource mockTokenSource) {
            if (value instanceof MockParseMetaToken && "error".equals(((MockParseMetaToken)value).name)) {
                Assert.fail("A parse error was recorded: " + value + " at " + mockTokenSource);
            }
        }
    };

    private final String name;
    private final String value;
    private final String where;

    public MockParseMetaToken(String name, MockTokenSource source) {
        this(name, null, source);
    }

    public MockParseMetaToken(String name, String value, MockTokenSource source) {
        this.name = name;
        this.value = value;
        this.where = source == null ? null : source.toString();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockParseMetaToken that = (MockParseMetaToken)o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        buffer.append(name);
        if (value != null) {
            buffer.append(':');
            buffer.append(SoyTestUtil.toSource(value));
        }
        if (where != null) {
            buffer.append(':');
            buffer.append(where);
        }
        buffer.append(']');
        return buffer.toString();
    }
}
