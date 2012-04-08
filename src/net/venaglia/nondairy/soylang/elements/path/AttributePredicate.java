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

package net.venaglia.nondairy.soylang.elements.path;

import com.intellij.psi.PsiElement;
import net.venaglia.nondairy.soylang.elements.AttributeElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * User: ed
 * Date: 1/28/12
 * Time: 10:39 AM
 *
 * A predicate that matches soy tags based on their name/value attribute pairs.
 */
public class AttributePredicate extends AbstractElementPredicate {

    private final String name;
    private final Pattern matchValue;

    private AttributePredicate(@Nullable String name, @Nullable Pattern matchValue) {
        this.name = name;
        this.matchValue = matchValue;
        if (name == null && matchValue == null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean test(PsiElement element) {
        if (element instanceof AttributeElement) {
            AttributeElement attr = (AttributeElement)element;
            if (name == null || name.equals(attr.getAttributeName())) {
                String value = attr.getAttributeValue("");
                return this.matchValue == null ||
                       this.matchValue.matcher(value).matches();
            } else {
                return false;
            }
        }
        return false; 
    }

    @Override
    public String toString() {
        if (name == null) {
            return "[*=" + matchValue + "]";
        }
        if (matchValue == null) {
            return "[" + name + "]";
        }
        return "[" + name + "=" + matchValue + "]";
    }

    /**
     * @param name The name of the attribute to look for.
     * @return an ElementPredicate that matches soy tag that has an attribute
     *     with the specified name.
     */
    public static AttributePredicate hasAttribute(@NotNull @NonNls String name) {
        return new AttributePredicate(name, null);
    }

    /**
     * @param value The value of the attribute to look for.
     * @return an ElementPredicate that matches soy tag that has an attribute
     *     with the specified value.
     */
    public static AttributePredicate hasValue(@NotNull @NonNls String value) {
        return new AttributePredicate(null, Pattern.compile(value, Pattern.LITERAL));
    }

    /**
     * @param match The regexp that will match attribute values to look for.
     * @return an ElementPredicate that matches soy tag that has an attribute
     *     with a value that matches the specified pattern.
     */
    public static AttributePredicate hasValue(@NotNull @NonNls Pattern match) {
        return new AttributePredicate(null, match);
    }

    /**
     * @param name The name of the attribute to look for.
     * @param value The value of the attribute to look for.
     * @return an ElementPredicate that matches soy tag that has an attribute
     *     with the specified name and value.
     */
    public static AttributePredicate hasAttributeWithValue(@NotNull @NonNls String name,
                                                           @NotNull @NonNls String value) {
        return new AttributePredicate(name, Pattern.compile(value, Pattern.LITERAL));
    }

    /**
     * @param name The name of the attribute to look for.
     * @param match The regexp that will match attribute values to look for.
     * @return an ElementPredicate that matches soy tag that has an attribute
     *     with the specified name and a value that matches the specified
     *     pattern.
     */
    public static AttributePredicate hasAttributeWithValue(@NotNull @NonNls String name,
                                                           @NotNull @NonNls Pattern match) {
        return new AttributePredicate(name, match);
    }
}
