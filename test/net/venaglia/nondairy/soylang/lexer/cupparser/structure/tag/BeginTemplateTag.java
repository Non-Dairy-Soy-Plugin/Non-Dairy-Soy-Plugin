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

package net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag;

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.NamespaceDeclaration;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 24, 2010
 * Time: 11:58:39 AM
 */
public class BeginTemplateTag extends BeginTag {

    private final String name;
    private final NamespaceDeclaration namespace;

    public BeginTemplateTag(String name, NamespaceDeclaration namespace, List<Attribute> attributes) {
        super("template", attributes);
        this.name = name;
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public NamespaceDeclaration getNamespace() {
        return namespace;
    }
}
