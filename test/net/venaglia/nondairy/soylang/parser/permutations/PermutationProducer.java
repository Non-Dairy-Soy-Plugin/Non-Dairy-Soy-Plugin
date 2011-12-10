/*
 * Copyright 2011 Ed Venaglia
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

package net.venaglia.nondairy.soylang.parser.permutations;

import net.venaglia.nondairy.soylang.lexer.SoySymbol;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: 8/6/11
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */
public interface PermutationProducer {

    boolean hasNext();

    void next();

    Iterator<SoySymbol> getIterator();

    CharSequence getModifiedSource();

    String getPermutatorName();

    int getSeq();
}
