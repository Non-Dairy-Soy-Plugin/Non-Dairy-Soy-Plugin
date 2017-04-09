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

package net.venaglia.nondairy.soylang.parser.permutations.impl;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: 8/21/11
 * Time: 5:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class SingleSymbolOmittingPermutator extends SymbolOmittingPermutator {

    protected <T> List<T> permuteSubListToDelete(List<T> symbolGroup, int sequence) {
        return symbolGroup.subList(sequence, sequence + 1);
    }

    protected int getPermutationCount(int size) {
        return size < 2 ? 0 : size;
    }
}
