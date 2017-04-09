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

import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: 5/22/12
 * Time: 8:37 AM
 */
public class DelegateTemplateCache extends AbstractTemplateCache<DelegateTemplateCache> {

    private static final Key<DelegateTemplateCache> DELEGATE_TEMPLATE_CACHE_KEY =
            new Key<DelegateTemplateCache>("non-dairy.delegate-template-cache");

    public DelegateTemplateCache(DelegatePackageCache parent, String delegatePackage) {
        super(parent, delegatePackage);
    }

    @Override
    Key<DelegateTemplateCache> getCacheKey() {
        return DELEGATE_TEMPLATE_CACHE_KEY;
    }

    public String getDelegatePackage() {
        return place;
    }


    @Nullable
    public static DelegateTemplateCache fromFile(VirtualFile file) {
        return file.getUserData(DELEGATE_TEMPLATE_CACHE_KEY);
    }
}
