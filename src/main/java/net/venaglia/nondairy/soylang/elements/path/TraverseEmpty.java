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

/**
 * User: ed
 * Date: 3/17/12
 * Time: 9:58 AM
 *
 * Enumeration used when handling traversals and predicates to describe how
 * navigation should proceed after an empty PsiElementCollection is produced.
 */
public enum TraverseEmpty {
    /**
     * Abort navigation and return an empty PsiElementCollection.
     */
    ABORT,

    /**
     * Call traverse() again, with the unfiltered results of the previous
     * traverse().
     */
    TRAVERSE_AGAIN,

    /**
     * Continue anyway, passing the empty PsiElementCollection to the next
     * ElementPredicate
     */
    CONTINUE
}
