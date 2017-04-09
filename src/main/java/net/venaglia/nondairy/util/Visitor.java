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

/**
 * User: ed
 * Date: 12/17/11
 * Time: 12:43 PM
 *
 * Generic visitor interface for sequentially passing the elements of a
 * collection or other data structure.
 */
public interface Visitor<E> {

    /**
     * Called for each element in a collection or similar data structure.
     * @param element The element.
     */
    void visit(E element);
}
