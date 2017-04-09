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

import net.venaglia.nondairy.i18n.I18N;

/**
 * User: ed
 * Date: Aug 14, 2010
 * Time: 9:03:44 PM
 *
 * A generic assertions utility class intended to break out simple run-time
 * fail-fast tests. By having these tests here it makes our production code
 * cleaner, simpler, easier to read and introduces fewer execution paths
 * allowing for better line coverage in unit testing.
 */
public class Assert {

    private Assert() {} // pure static class

    /**
     * Ensures that the passed Enum class contains no more than the indicated
     * number of values. Yeah, its kinda silly, but this helps enums from
     * growing beyond manageable sizes that usually indicate that they are
     * either being misused, or it is time to refactor a single enum field into
     * multiple fields or introduce a delegate class.
     * @param cls The enum class to test
     * @param max The maximum allowable number of enum values
     */
    public static void maxEnumValuesLessThan(Class<? extends Enum> cls, int max) {
        if (cls.getEnumConstants().length > max)
            throw new AssertionError(I18N.msg("assertion.error.too.many.enum.values",
                                              cls.getSimpleName(),
                                              cls.getEnumConstants().length));
    }

    /**
     * Ensures that a value is not null
     * @param obj The value to test
     * @param name The name of the value being tested
     */
    public static void notNull(Object obj, String name) {
        if (obj == null) {
            throw new AssertionError(I18N.msg("assertion.error.value.cannot.be.null", name));
        }
    }

    /**
     * Ensures that two values are equal
     * @param expected The expected value
     * @param actual The actual value
     */
    public static void equal(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(I18N.msg("assertion.error.values.not.equal", expected, actual));
        }
    }

    /**
     * Ensures that two values are equal
     * @param expected The expected value
     * @param actual The actual value
     */
    public static void equal(long expected, long actual) {
        if (expected != actual) {
            throw new AssertionError(I18N.msg("assertion.error.values.not.equal", expected, actual));
        }
    }

    /**
     * Ensures that two values are equal
     * @param expected The expected value
     * @param actual The actual value
     */
    public static void equal(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(I18N.msg("assertion.error.values.not.equal", expected, actual));
        }
    }
}
