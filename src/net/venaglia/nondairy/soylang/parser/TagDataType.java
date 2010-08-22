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
