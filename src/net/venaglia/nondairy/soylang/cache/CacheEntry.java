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

package net.venaglia.nondairy.soylang.cache;

import com.intellij.openapi.vfs.VirtualFile;
import net.venaglia.nondairy.soylang.elements.TreeNavigator;

/**
 * User: ed
 * Date: 1/30/12
 * Time: 10:17 PM
 *
 * Represents a single immutable template definition in a source file.
 */
public class CacheEntry {

    private final String delegate;
    private final String namespace;
    private final String template;
    private final boolean deltemplate;
    private final String fileUrl;

    public CacheEntry(String delegate, String namespace, String template, boolean deltemplate, VirtualFile file) {
        this.delegate = delegate;
        this.namespace = namespace;
        this.template = template;
        this.deltemplate = deltemplate;
        this.fileUrl = file.getUrl();
    }

    public String getDelegate() {
        return delegate;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getTemplate() {
        return template;
    }

    public boolean isDeltemplate() {
        return deltemplate;
    }

    public VirtualFile getFile() {
        return TreeNavigator.INSTANCE.getFile(fileUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheEntry that = (CacheEntry)o;

        if (deltemplate != that.deltemplate) return false;
        if (delegate != null ? !delegate.equals(that.delegate) : that.delegate != null) return false;
        if (fileUrl != null ? !fileUrl.equals(that.fileUrl) : that.fileUrl != null) return false;
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) return false;
        if (template != null ? !template.equals(that.template) : that.template != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = delegate != null ? delegate.hashCode() : 0;
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (template != null ? template.hashCode() : 0);
        result = 31 * result + (deltemplate ? 1 : 0);
        result = 31 * result + (fileUrl != null ? fileUrl.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public String toString() {
        String del = DelegateCache.DEFAULT_DELEGATE.equals(delegate) ? "*" : delegate;
        String ns = NamespaceCache.DEFAULT_NAMESPACE.equals(namespace) ? "" : namespace;
        String dt = deltemplate ? "deltemplate" : "template";
        return String.format("CacheEntry{delegate=%s,%s=%s.%s,file=%s}", del, dt, ns, template, fileUrl);
    }
}
