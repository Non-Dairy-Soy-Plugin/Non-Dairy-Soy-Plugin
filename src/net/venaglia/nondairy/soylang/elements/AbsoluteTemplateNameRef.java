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

package net.venaglia.nondairy.soylang.elements;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.SoyProjectComponent;
import net.venaglia.nondairy.soylang.cache.AliasCache;
import net.venaglia.nondairy.soylang.cache.AliasCacheEntry;
import net.venaglia.nondairy.soylang.cache.NamespaceCache;
import net.venaglia.nondairy.soylang.cache.SoyCacheUpdater;
import net.venaglia.nondairy.soylang.cache.TemplateCache;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import net.venaglia.nondairy.soylang.elements.path.TemplatePath;
import net.venaglia.nondairy.util.SimpleRef;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * User: ed
 * Date: Aug 24, 2010
 * Time: 5:37:04 PM
 *
 * PsiElement implementation that represents the fully qualified template name
 * in a call soy tag.
 */
public class AbsoluteTemplateNameRef extends SoyPsiElement implements SoyNamedElement, ItemPresentation, TemplateMemberElement {

    private SimpleRef<String> templateNameCachedRef;

    public AbsoluteTemplateNameRef(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public String getName() {
        String text = getText();
        int i = text.lastIndexOf('.');
        if (i >= 0) {
            text = text.substring(i + 1);
        }
        return text;
    }

    @Override
    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
        String prefix = getText();
        int i = prefix.lastIndexOf('.');
        prefix = prefix.substring(0, i + 1);
        TextRange range = getTextRange().shiftRight(0 - getTextOffset());
        return ElementManipulators.getManipulator(this).handleContentChange(this, range, prefix + name);
    }

    @Override
    public PsiReference getReference() {
        String templateName = getTemplateName();
        PsiElementPath pathToTemplateName = TemplatePath.forTemplateName(templateName)
                .debug("for_template_name!absolute");
        return new SoyPsiElementReference(this, pathToTemplateName, null);
    }

    @Override
    public String getPresentableText() {
        return getTemplateName();
    }

    @Override
    public String getLocationString() {
        return getNamespace();
    }

    @Override
    public Icon getIcon(boolean open) {
        return null;
    }

    @Override
    public String getCanonicalName() {
        return getTemplateName();
    }

    @Override
    @NotNull
    public String getTemplateName() {
        if (templateNameCachedRef == null) {
            SoyProjectComponent soyProjectComponent = SoyProjectComponent.getSoyProjectComponent(this);
            SoyCacheUpdater soyCacheUpdater = soyProjectComponent == null ? null : soyProjectComponent.getSoyCacheUpdater();
            templateNameCachedRef = soyCacheUpdater == null ? null : soyCacheUpdater.getCachedRef(new SimpleRef<String>() {
                @Nullable
                @Override
                public String get() {
                    return getTemplateNameImpl();
                }
            });
        }
        String templateName = templateNameCachedRef == null ? getTemplateNameImpl() : templateNameCachedRef.get();
        return templateName == null ? getText() : templateName;
    }

    @NotNull
    private String getTemplateNameImpl() {
        String rawName = getText();
        int dot = rawName.indexOf('.');
        if (dot == -1 || dot != rawName.lastIndexOf('.')) {
            return rawName;
        }
        String expectedLastPart = rawName.substring(0, dot);

        PsiFile psiFile = getContainingFile();
        VirtualFile file = psiFile.getVirtualFile();
        if (file == null) return rawName;
        Module module = getModule();
        if (module == null) return rawName;
        module.getProject().getComponent(SoyProjectComponent.NON_DAIRY_PROJECT_COMPONENT_NAME);
        NamespaceCache namespaceCache = NamespaceCache.getCache(module);
        String templateShortName = rawName.substring(dot);
        if (exists(rawName.substring(0, dot), templateShortName, namespaceCache)) {
            return rawName;
        }
        AliasCache aliasCache =  AliasCache.getCache(module);
        Collection<AliasCacheEntry> caches = aliasCache.getReferencingAliasCaches(file);

        for (AliasCacheEntry entry : caches) {
            String alias = entry.getNamespace();
            String lastPart = alias.substring(alias.lastIndexOf('.') + 1);
            if (lastPart.equals(expectedLastPart)) {
                if (exists(alias, templateShortName.substring(1), namespaceCache)) {
                    return alias + templateShortName;
                }
            }
        }

        // no match
        return rawName;
    }

    private boolean exists(String namespace, String templateShortName, NamespaceCache cache) {
        TemplateCache templateCache = cache.get(namespace);
        return templateCache != null && templateCache.containsKey(templateShortName);
    }

    @Override
    public String getNamespace() {
        String name = getTemplateName();
        int index = name.lastIndexOf('.');
        return index > 1 ? name.substring(0, index) : null;
    }
}
