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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbModeTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.soylang.ModuleRef;
import net.venaglia.nondairy.soylang.NamespaceRef;
import net.venaglia.nondairy.soylang.SoyFileType;
import net.venaglia.nondairy.soylang.elements.TreeNavigator;
import net.venaglia.nondairy.util.SimpleRef;
import net.venaglia.nondairy.util.TinySet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: ed
 * Date: 1/30/12
 * Time: 7:50 PM
 *
 * CacheUpdater implementation used to initialize the soy template cache.
 */
public class SoyCacheUpdater extends DumbModeTask {
    @NonNls
    private static final String MATCH_COMMANDS_PATTERN = "\\{(delpackage|namespace|alias|deltemplate|template)\\s+\\.?([a-z0-9_.]+)";
    private static final Pattern MATCH_COMMANDS = Pattern.compile(MATCH_COMMANDS_PATTERN, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    @NonNls
    private static final String DEBUG_CACHE_PROPERTY = "net.venaglia.nondairy.cache.debug";
    private static final long DEBUG_CACHE_CHANGE_DETECTION_DELAY = 1000L;

    private final Project project;
    private final AtomicLong lastUpdate = new AtomicLong();

    private volatile boolean disposed = false;

    public SoyCacheUpdater(Project project) {
        this.project = project;
        String property = System.getProperty(DEBUG_CACHE_PROPERTY, "");
        if ("true".equals(property) || property.contains("namespace")) { //NON-NLS
            Thread thread = new Thread(new NamespaceCacheDebugger(project), "Soy Template Cache Debugger - " + project); //NON-NLS
            thread.setDaemon(true);
            thread.start();
        }
        if ("true".equals(property) || property.contains("alias")) { //NON-NLS
            Thread thread = new Thread(new AliasCacheDebugger(project), "Soy Alias Cache Debugger - " + project); //NON-NLS
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void performInDumbMode(@NotNull final ProgressIndicator indicator) {
        // FIXME: Use the indicator to show *real* progress
        indicator.setIndeterminate(true);

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                final Collection<VirtualFile> files = queryNeededFiles(indicator);
                for (VirtualFile file : files) {
                    processFile(file);
                }
            }
        });
    }

    private Collection<VirtualFile> queryNeededFiles(ProgressIndicator indicator) {
        String ext = SoyFileType.INSTANCE.getDefaultExtension();
        return FilenameIndex.getAllFilesByExt(project, ext);
    }

    private void processFile(VirtualFile virtualFile) {
        if (disposed) {
            return;
        }
        updateCache(virtualFile);
    }

    public void updateCache(@NotNull VirtualFile file) {
        if (disposed) {
            return;
        }
        if (isCacheableSoyFile(file)) {
            NamespaceCache namespaceCache = getNamespaceCache(file);
            DelegatePackageCache delegatePackageCache = getDelegatePackageCache(file);
            AliasCache aliasCache = getAliasCache(file);
            if (namespaceCache != null) {
                removeFromCacheImpl(namespaceCache, file);
            }
            if (aliasCache != null) {
                removeFromCacheImpl(aliasCache, file);
            }
            if (delegatePackageCache != null) {
                removeFromCacheImpl(delegatePackageCache, file);
            }
            if (namespaceCache != null || delegatePackageCache != null) {
                updateCacheImpl(namespaceCache, aliasCache, delegatePackageCache, file);
            }
            lastUpdate.set(System.currentTimeMillis());
        }
    }

    private boolean isCacheableSoyFile(VirtualFile file) {
        return file.isValid() &&
               file.getLength() < 1000000 && // don't parse extremely large files
               SoyFileType.INSTANCE
                          .getDefaultExtension()
                          .equals(file.getExtension());
    }

    @SuppressWarnings("StringEquality")
    private void updateCacheImpl(@Nullable NamespaceCache namespaceCache,
                                 @Nullable AliasCache aliasCache,
                                 @Nullable DelegatePackageCache delegatePackageCache,
                                 @NotNull VirtualFile file) {
        Document document = TreeNavigator.INSTANCE.getDocument(file);
        Collection<String> templates = new ArrayList<String>(16);
        Collection<String> deltemplates = new ArrayList<String>(16);
        String delegate = DelegatePackageCache.DEFAULT_DELEGATE;
        String namespace = NamespaceCache.DEFAULT_NAMESPACE;
        if (document != null) {
            Matcher matcher = MATCH_COMMANDS.matcher(document.getCharsSequence());
            while (matcher.find()) {
                String command = matcher.group(1);
                if ("delpackage".equals(command)) { //NON-NLS
                    if (delegate == DelegatePackageCache.DEFAULT_DELEGATE) {
                        delegate = matcher.group(2);
                    }
                } else if ("namespace".equals(command)) { //NON-NLS
                    if (namespace == NamespaceCache.DEFAULT_NAMESPACE) {
                        namespace = matcher.group(2);
                    }
                } else if ("alias".equals(command)) { //NON-NLS
                    AliasCacheEntry aliasCacheEntry = aliasCache.getOrCreate(matcher.group(2));
                    aliasCacheEntry.add(file);
                } else if ("deltemplate".equals(command)) { //NON-NLS
                    deltemplates.add(matcher.group(2));
                } else {
                    templates.add(matcher.group(2));
                }
            }
        }
        if (namespaceCache != null) {
            TemplateCache templateCache = namespaceCache.getOrCreate(namespace);
            templateCache.addFile(file);
            Collection<CacheEntry> newEntries = new ArrayList<CacheEntry>(templates.size());
            for (String template : templates) {
                CacheEntry cacheEntry = new CacheEntry(namespace, template, false, file);
                templateCache.getOrCreate(template).add(cacheEntry);
                newEntries.add(cacheEntry);
            }
            namespaceCache.added(newEntries.iterator());
        }
        if (delegatePackageCache != null) {
            DelegateTemplateCache templateCache = delegatePackageCache.getOrCreate(delegate);
            templateCache.addFile(file);
            Collection<CacheEntry> newEntries = new ArrayList<CacheEntry>(templates.size());
            for (String template : deltemplates) {
                CacheEntry cacheEntry = new CacheEntry(delegate, template, true, file);
                templateCache.getOrCreate(template).add(cacheEntry);
                newEntries.add(cacheEntry);
            }
            delegatePackageCache.added(newEntries.iterator());
        }
    }

    public void removeFromCache(@NotNull VirtualFile file) {
        if (disposed) {
            return;
        }
        NamespaceCache namespaceCache = getNamespaceCache(file);
        if (namespaceCache != null) {
            removeFromCacheImpl(namespaceCache, file);
        } else {
            for (Module module : TreeNavigator.INSTANCE.getModules(project)) {
                removeFromCacheImpl(NamespaceCache.getCache(module), file);
            }
        }
        DelegatePackageCache delegatePackageCache = getDelegatePackageCache(file);
        if (namespaceCache != null) {
            removeFromCacheImpl(delegatePackageCache, file);
        } else {
            for (Module module : TreeNavigator.INSTANCE.getModules(project)) {
                removeFromCacheImpl(DelegatePackageCache.getCache(module), file);
            }
        }
    }

    public <T> SimpleRef<T> getCachedRef(final SimpleRef<T> source) {
        return new CachingRef<T>(source);
    }

    private void removeFromCacheImpl(@NotNull NamespaceCache namespaceCache, @NotNull VirtualFile file) {
        TemplateCache templateCacheToRemove = TemplateCache.fromFile(file);
        if (templateCacheToRemove != null) {
            TemplateCache templateCache = namespaceCache.get(templateCacheToRemove.getNamespace());
            if (templateCache == templateCacheToRemove) {
                namespaceCache.remove(templateCacheToRemove.getNamespace());
                if (namespaceCache.isEmpty()) {
                    namespaceCache.remove(templateCacheToRemove.getNamespace());
                }
            }
        }
    }

    private void removeFromCacheImpl(@NotNull AliasCache aliasCache, @NotNull VirtualFile file) {
        aliasCache.removeAllReferencingAliasCaches(file);
    }

    private void removeFromCacheImpl(@NotNull DelegatePackageCache delegatePackageCache, @NotNull VirtualFile file) {
        DelegateTemplateCache templateCacheToRemove = DelegateTemplateCache.fromFile(file);
        if (templateCacheToRemove != null) {
            delegatePackageCache.remove(templateCacheToRemove.getDelegatePackage());
        }
    }

    private NamespaceCache getNamespaceCache(VirtualFile file) {
        if (disposed) {
            return null;
        }
        ProjectFileIndex fileIndex = TreeNavigator.INSTANCE.getProjectFileIndex(project);
        Module module = fileIndex.getModuleForFile(file);
        return module == null ? null : NamespaceCache.getCache(module);
    }

    private AliasCache getAliasCache(VirtualFile file) {
        ProjectFileIndex fileIndex = TreeNavigator.INSTANCE.getProjectFileIndex(project);
        Module module = fileIndex.getModuleForFile(file);
        return module == null ? null : AliasCache.getCache(module);
    }

    private DelegatePackageCache getDelegatePackageCache(VirtualFile file) {
        if (disposed) {
            return null;
        }
        ProjectFileIndex fileIndex = TreeNavigator.INSTANCE.getProjectFileIndex(project);
        Module module = fileIndex.getModuleForFile(file);
        return module == null ? null : DelegatePackageCache.getCache(module);
    }

    public void dispose() {
        disposed = true;
    }

    private class CachingRef<T> implements SimpleRef<T> {

        @NotNull
        private final SimpleRef<T> source;
        private final AtomicLong cleanUpdate = new AtomicLong();

        private T value;

        private CachingRef(@NotNull SimpleRef<T> source) {
            this.source = source;
        }

        @Override
        @Nullable
        public T get() {
            long current = lastUpdate.get();
            if (cleanUpdate.getAndSet(current) != current) {
                value = source.get();
            }
            return value;
        }
    }

    /**
     * Diagnostic class, run in a separate thread, that monitors the cache and
     * logs changes for a single project.
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private abstract class CacheDebugger<V,E extends NavigableMap<String,V>> implements Runnable {

        private final Reference<Project> project;
        private final long offset = System.currentTimeMillis() % 250;
        private final Map<Module,E> lastVersions = new HashMap<Module,E>();

        private long lastUpdate = 0L;

        protected String defaultNamespaceKey = "cache.debugger.format.default.namespace";
        protected String namespaceKey = "cache.debugger.format.namespace";
        protected String moduleKey = "cache.debugger.format.module";

        public CacheDebugger(@NotNull Project project) {
            this.project = new WeakReference<Project>(project);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    checkForUpdate();
                } catch (NoSuchElementException e) {
                    break;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(250 - ((System.currentTimeMillis() + offset) % 250));
                } catch (InterruptedException e) {
                    // don't care
                }
            }
        }

        private boolean checkForUpdate() throws NoSuchElementException {
            Project project = this.project.get();
            if (project == null) {
                return false;
            }
            long lastUpdate = SoyCacheUpdater.this.lastUpdate.get();
            if (lastUpdate != this.lastUpdate && lastUpdate + DEBUG_CACHE_CHANGE_DETECTION_DELAY < System.currentTimeMillis()) {
                this.lastUpdate = lastUpdate;
                StringWriter buffer = new StringWriter(1024);
                logChanges(project, new PrintWriter(buffer));
                System.out.println(buffer);
            }
            return true;
        }

        @NonNls
        private void logChanges(Project project, PrintWriter log) {
            Collection<Module> modules = new HashSet<Module>(
                    Arrays.asList(TreeNavigator.INSTANCE.getModules(project))
            );
            lastVersions.keySet().retainAll(modules);
            modules.removeAll(lastVersions.keySet());
            for (Module module : modules) {
                lastVersions.put(module, createEmpty(module));
            }

            for (Module module : lastVersions.keySet()) {
                E current = getForModule(module);
                E previous = lastVersions.get(module);
                logChanges(current, current.getClass().getSimpleName(), current, previous, log, "");
                lastVersions.put(module, cloneElement(current));
            }
        }

        @NotNull
        protected abstract E createEmpty(@NotNull Module module);

        @NotNull
        protected abstract E getForModule(@NotNull Module module);

        @NotNull
        protected abstract E cloneElement(@NotNull E value);

        @NotNull
        protected String getLabel(@NotNull String simpleName, @NotNull Object obj) {
            if (obj instanceof NamespaceRef) {
                String namespace = ((NamespaceRef)obj).getNamespace();
                if (NamespaceCache.DEFAULT_NAMESPACE.equals(namespace)) {
                    return I18N.msg(defaultNamespaceKey, simpleName);
                } else if (namespace != null) {
                    return I18N.msg(namespaceKey, simpleName, namespace);
                }
            }
            if (obj instanceof ModuleRef) {
                Module module = ((ModuleRef)obj).getModule();
                if (module != null) {
                    return I18N.msg(moduleKey, simpleName, module.getName());
                }
            }
            return String.valueOf(obj);
        }

        @SuppressWarnings("unchecked")
        private <V> Iterator<Map.Entry<String,V>> iter(@Nullable Map<String,V> map) {
            if (map == null) {
                return Collections.<String,V>emptyMap().entrySet().iterator();
            } else {
                return map.entrySet().iterator();
            }
        }

        private <V> int logChanges(@Nullable Object parent,
                                   @Nullable String parentSimpleName,
                                   @Nullable NavigableMap<String,V> current,
                                   @Nullable NavigableMap<String,V> previous,
                                   PrintWriter log,
                                   String indent) {
            int changeCount = 0;
            String nextIndent = indent + "    ";
            Iterator<Map.Entry<String,V>> cIter = iter(current);
            Iterator<Map.Entry<String,V>> pIter = iter(previous);
            Map.Entry<String,V> c = cIter.hasNext() ? cIter.next() : null;
            Map.Entry<String,V> p = pIter.hasNext() ? cIter.next() : null;
            while (c != null && p != null) {
                int cmp = c.getKey().compareTo(p.getKey());
                @SuppressWarnings("unchecked")
                NavigableMap<String,Object> cValue = c.getValue() instanceof NavigableMap
                                                     ? (NavigableMap<String,Object>)c.getValue()
                                                     : null;
                @SuppressWarnings("unchecked")
                NavigableMap<String,Object> pValue = p.getValue() instanceof NavigableMap
                                                     ? (NavigableMap<String,Object>)p.getValue()
                                                     : null;
                if (cmp < 0) {
                    changeCount++;
                    logAdded(nextIndent, c.getValue(), log);
                    if (pValue != null) {
                        pValue.put(c.getKey(), cloneOf(c.getValue()));
                    }
                    c = cIter.hasNext() ? cIter.next() : null;
                } else if (cmp > 0 ) {
                    changeCount++;
                    logRemoved(nextIndent, p.getValue(), log);
                    if (pValue != null) {
                        pValue.remove(c.getKey());
                    }
                    p = pIter.hasNext() ? pIter.next() : null;
                } else {
                    if (p.getValue().equals(c.getValue())) {
                        // identical, no changes in this sub-tree
                    } else if (pValue != null && cValue != null) {
                        changeCount += logChanges(cValue, cValue.getClass().getSimpleName(), cValue, pValue, log, nextIndent);
                    } else {
                        changeCount++;
                        logReplaced(nextIndent, p.getValue(), c.getValue(), log);
                    }
                    c = cIter.hasNext() ? cIter.next() : null;
                    p = pIter.hasNext() ? pIter.next() : null;
                }
            }
            while (c != null) {
                Object v = getValue(c);
                if (v instanceof NavigableMap) {
                    @SuppressWarnings("unchecked")
                    NavigableMap<String,Object> cValue = (NavigableMap<String,Object>)v;
                    changeCount += logChanges(cValue, c.getValue().getClass().getSimpleName(), cValue, null, log, nextIndent);
                } else {
                    changeCount++;
                    logAdded(nextIndent, v, log);
                }
                c = cIter.hasNext() ? cIter.next() : null;
            }
            while (p != null) {
                Object v = getValue(p);
                if (v instanceof NavigableMap) {
                    @SuppressWarnings("unchecked")
                    NavigableMap<String,Object> pValue = (NavigableMap<String,Object>)v;
                    changeCount += logChanges(pValue, p.getValue().getClass().getSimpleName(), null, pValue, log, nextIndent);
                } else {
                    changeCount++;
                    logRemoved(nextIndent, v, log);
                }
                p = pIter.hasNext() ? pIter.next() : null;
            }
            if (changeCount > 0 && parent != null) {
                if (parentSimpleName == null) {
                    parentSimpleName = parent.getClass().getSimpleName();
                }
                log.print(indent);
                log.print("recorded ");
                log.print(changeCount);
                log.print(changeCount == 1 ? " change to " : " changes to ");
                log.println(getLabel(parentSimpleName, parent));
            }
            return changeCount;
        }

        protected Object getValue(Map.Entry<String,?> entry) {
            return entry.getValue();
        }

        @SuppressWarnings("unchecked")
        private <V> V cloneOf(V value) {
            if (value instanceof TinySet) {
                return (V)((TinySet<?>)value).clone();
            } else if (value instanceof AbstractCache) {
                AbstractCache cache = (AbstractCache)value;
                return (V)cache.clone();
            }
            return value;
        }

        private <V> void logAdded(String indent, V value, PrintWriter log) {
            log.print(indent);
            log.print("+ ");
            log.println(value);
        }

        private <V> void logRemoved(String indent, V value, PrintWriter log) {
            log.print(indent);
            log.print("- ");
            log.println(value);
        }

        private <V> void logReplaced(String indent, V previous, V replacement, PrintWriter log) {
            log.print(indent);
            log.print("- ");
            log.println(previous);
            log.print(indent);
            log.print("+ ");
            log.println(replacement);
        }
    }

    private class NamespaceCacheDebugger extends CacheDebugger<TemplateCache,NamespaceCache> {

        public NamespaceCacheDebugger(Project project) {
            super(project);
        }

        @Override
        @NotNull
        protected NamespaceCache createEmpty(@NotNull Module module) {
            return new NamespaceCache(module);
        }

        @Override
        @NotNull
        protected NamespaceCache getForModule(@NotNull Module module) {
            return NamespaceCache.getCache(module);
        }

        @Override
        @NotNull
        protected NamespaceCache cloneElement(@NotNull NamespaceCache value) {
            return value.clone();
        }
    }

    private class AliasCacheDebugger extends CacheDebugger<AliasCacheEntry,AliasCache> {

        public AliasCacheDebugger(Project project) {
            super(project);
            namespaceKey = "cache.debugger.format.alias";
        }

        @Override
        @NotNull
        protected AliasCache createEmpty(@NotNull Module module) {
            return new AliasCache(module);
        }

        @Override
        @NotNull
        protected AliasCache getForModule(@NotNull Module module) {
            return AliasCache.getCache(module);
        }

        @Override
        @NotNull
        protected AliasCache cloneElement(@NotNull AliasCache value) {
            return value.clone();
        }

        @Override
        protected Object getValue(Map.Entry<String,?> entry) {
            Object value = super.getValue(entry);
            if (value instanceof AliasCacheEntry) {
                value = ((AliasCacheEntry)value).getDebugMap();
            }
            return value;
        }
    }
}
