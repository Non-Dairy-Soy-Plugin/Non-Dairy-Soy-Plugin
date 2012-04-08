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

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;

import java.util.Map;

/**
 * User: ed
 * Date: 3/14/12
 * Time: 9:25 PM
 * 
 * Most element predicates are defined statically for convenience. Sometimes
 * you need a new instance each time it gets used. Sometimes you want to store
 * data for the duration of the path navigation operation.
 */
public abstract class InstancePredicate implements ElementPredicate.AlwaysTrue {

    /**
     * @param navigationData data created when this navigation begins, and
     *      discarded when navigation completes.
     * @return an instance suitable for predicate operations
     */
    public abstract ElementPredicate getInstance(Map<Key,Object> navigationData);


    @Override
    public final boolean test(PsiElement element) {
        return true;
    }
}
