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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 * User: ed
 * Date: 1/30/12
 * Time: 9:19 PM
 *
 * This cache stores a {@link TemplateCache} object for each namespace.
 */
public class NamespaceCache extends AbstractCache<String,TemplateCache> {

    @NonNls
    public static final String DEFAULT_NAMESPACE = "\u001a [ns]";

    private final DelegateCache parent;
    private final String delegate;

    public NamespaceCache(DelegateCache parent, String delegate) {
        this.parent = parent;
        this.delegate = delegate;
    }

    public String getDelegate() {
        return delegate;
    }

    @NotNull
    @Override
    protected TemplateCache create(String namespace) {
        return new TemplateCache(this, delegate, namespace);
    }

    @Override
    @Nullable
    public String getKeyFor(@NotNull TemplateCache value) {
        return value.getNamespace();
    }

    @Override
    public boolean removeChild(TemplateCache value) {
        boolean b = super.removeChild(value);
        if (b && isEmpty()) {
            parent.removeChild(this);
        }
        return b;
    }

    void removed(Iterator<CacheEntry> iterator) {
        parent.removed(iterator);
    }
}
