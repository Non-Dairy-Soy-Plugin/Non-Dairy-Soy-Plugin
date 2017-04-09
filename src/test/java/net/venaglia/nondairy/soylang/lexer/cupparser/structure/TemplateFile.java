/*
 * Copyright 2010 - 2013 Ed Venaglia
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

import net.venaglia.nondairy.soylang.lexer.cupparser.structure.tag.BeginTemplateTag;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 21, 2010
 * Time: 9:39:14 PM
 */
public class TemplateFile {

    private final NamespaceDeclaration namespace;
    private final List<AliasDeclaration> aliases;
    private final List<TemplateDeclaration> templates;

    public TemplateFile(NamespaceDeclaration namespace, List<TemplateDeclaration> templates) {
        this(namespace, Collections.<AliasDeclaration>emptyList(), templates);
    }

    public TemplateFile(NamespaceDeclaration namespace, List<AliasDeclaration> aliases, List<TemplateDeclaration> templates) {
        this.namespace = namespace;
        this.aliases = aliases;
        if (namespace != null && templates != null) {
            List<TemplateDeclaration> buffer = new LinkedList<TemplateDeclaration>(templates);
            for (ListIterator<TemplateDeclaration> iter = templates.listIterator(); iter.hasNext();) {
                TemplateDeclaration t = iter.next();
                BeginTemplateTag btt = t.getBeginTag();
                iter.set(new TemplateDeclaration(t.getTemplateDocComment(),
                                                 new ContentTagPair(new BeginTemplateTag(btt.getName(),
                                                                                         namespace,
                                                                                         btt.getAttributes()),
                                                                    t.getEndTag(),
                                                                    t.getContents())));
            }
            this.templates = Collections.unmodifiableList(buffer);
        } else if (templates != null) {
            this.templates = Collections.unmodifiableList(templates);
        } else {
            this.templates = Collections.emptyList();
        }
    }

    public NamespaceDeclaration getNamespace() {
        return namespace;
    }

    public List<AliasDeclaration> getAliases() {
        return aliases;
    }

    public List<TemplateDeclaration> getTemplates() {
        return templates;
    }
}
