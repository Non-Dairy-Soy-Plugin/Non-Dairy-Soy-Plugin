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

package net.venaglia.nondairy.soylang.lexer.cupparser.structure;

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.Attribute;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 21, 2010
 * Time: 9:39:38 PM
 */
public class NamespaceDeclaration {

    private final String namespace;
    private final List<Attribute> attributes;

    public NamespaceDeclaration(String namespace) {
        this(namespace, Collections.<Attribute>emptyList());
    }

    public NamespaceDeclaration(String namespace, List<Attribute> attributes) {
        this.namespace = namespace;
        this.attributes = Collections.unmodifiableList(attributes);
    }

    public String getNamespace() {
        return namespace;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }
}
