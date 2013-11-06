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

package net.venaglia.nondairy.soylang.cache;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import net.venaglia.nondairy.soylang.NamespaceRef;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: 1/30/12
 * Time: 9:17 PM
 *
 * This cache stores a Collection of {@link CacheEntry} objects for each
 * template name.
 */
public class TemplateCache extends AbstractTemplateCache<TemplateCache> implements NamespaceRef {

    private static final Key<TemplateCache> TEMPLATE_CACHE_KEY =
            new Key<TemplateCache>("non-dairy.template-cache");

    public TemplateCache(NamespaceCache parent, String namespace) {
        super(parent, namespace);
    }

    @Override
    public String getNamespace() {
        return place;
    }

    @Override
    Key<TemplateCache> getCacheKey() {
        return TEMPLATE_CACHE_KEY;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public String toString() {
        return String.format("TemplateCache{namespace=\"%s\",templates=%d}", place, size());
    }

    @Nullable
    public static TemplateCache fromFile(VirtualFile file) {
        return file.getUserData(TEMPLATE_CACHE_KEY);
    }
}
