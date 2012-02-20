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

import com.intellij.ide.caches.CacheUpdater;
import com.intellij.ide.caches.FileContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import net.venaglia.nondairy.soylang.SoyFileType;
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
public class SoyCacheUpdater implements CacheUpdater {

    @NonNls
    private static final String MATCH_COMMANDS_PATTERN = "\\{(delpackage|namespace|deltemplate|template)\\s+\\.?([a-z0-9_.]+)";
    private static final Pattern MATCH_COMMANDS = Pattern.compile(MATCH_COMMANDS_PATTERN, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    @NonNls
    private static final String DEBUG_CACHE_PROPERTY = "net.venaglia.nondairy.cache.debug";
    private static final long DEBUG_CACHE_CHANGE_DETECTION_DELAY = 1000L;

    private final Project project;
    private final ProjectFileIndex fileIndex;
    private final AtomicLong lastUpdate = new AtomicLong();

    public SoyCacheUpdater(Project project) {
        this.project = project;
        fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        if ("true".equals(System.getProperty(DEBUG_CACHE_PROPERTY))) { //NON-NLS
            Thread thread = new Thread(new CacheDebugger(project), "Soy Template Cache Debugger - " + project); //NON-NLS
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public int getNumberOfPendingUpdateJobs() {
        return 0;
    }

    @Override
    public VirtualFile[] queryNeededFiles(ProgressIndicator indicator) {
        String ext = SoyFileType.INSTANCE.getDefaultExtension();
        Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project, ext);
        return files.toArray(new VirtualFile[files.size()]);
    }

    @Override
    public void processFile(FileContent fileContent) {
        updateCache(fileContent.getVirtualFile());
    }

    @Override
    public void updatingDone() {
    }

    @Override
    public void canceled() {
    }

    public void updateCache(@NotNull VirtualFile file) {
        if (file.getLength() <= 1000000) { // don't parse extremely large files
            DelegateCache delegateCache = getDelegateCache(file);
            if (delegateCache != null) {
                removeFromCacheImpl(delegateCache, file);
                updateCacheImpl(delegateCache, file);
            }
            lastUpdate.set(System.currentTimeMillis());
        }
    }

    @SuppressWarnings("StringEquality")
    private void updateCacheImpl(@NotNull DelegateCache delegateCache, @NotNull VirtualFile file) {
        Document document = FileDocumentManager.getInstance().getDocument(file);
        Collection<String> templates = new ArrayList<String>(16);
        Collection<String> deltemplates = new ArrayList<String>(16);
        String delegate = DelegateCache.DEFAULT_DELEGATE;
        String namespace = NamespaceCache.DEFAULT_NAMESPACE;
        if (document != null) {
            Matcher matcher = MATCH_COMMANDS.matcher(document.getCharsSequence());
            while (matcher.find()) {
                String command = matcher.group(1);
                if ("delpackage".equals(command)) { //NON-NLS
                    if (delegate == DelegateCache.DEFAULT_DELEGATE) {
                        delegate = matcher.group(2);
                    }
                } else if ("namespace".equals(command)) { //NON-NLS
                    if (namespace == NamespaceCache.DEFAULT_NAMESPACE) {
                        namespace = matcher.group(2);
                    }
                } else if ("deltemplate".equals(command)) { //NON-NLS
                    deltemplates.add(matcher.group(2));
                } else {
                    templates.add(matcher.group(2));
                }
            }
        }
        TemplateCache templateCache = delegateCache.getOrCreate(delegate).getOrCreate(namespace);
        templateCache.addFile(file);
        Collection<CacheEntry> newEntries = new ArrayList<CacheEntry>(templates.size());
        for (String template : templates) {
            CacheEntry cacheEntry = new CacheEntry(delegate, namespace, template, false, file);
            templateCache.getOrCreate(template).add(cacheEntry);
            newEntries.add(cacheEntry);
        }
        for (String template : deltemplates) {
            CacheEntry cacheEntry = new CacheEntry(delegate, namespace, template, true, file);
            templateCache.getOrCreate(template).add(cacheEntry);
            newEntries.add(cacheEntry);
        }
        delegateCache.added(newEntries.iterator());
    }

    public void removeFromCache(@NotNull VirtualFile file) {
        DelegateCache delegateCache = getDelegateCache(file);
        if (delegateCache != null) {
            removeFromCacheImpl(delegateCache, file);
        } else {
            for (Module module : ModuleManager.getInstance(project).getModules()) {
                removeFromCacheImpl(DelegateCache.getDelegateCache(module), file);
            }
        }
    }

    private void removeFromCacheImpl(@NotNull DelegateCache delegateCache, @NotNull VirtualFile file) {
        TemplateCache templateCacheToRemove = TemplateCache.fromFile(file);
        if (templateCacheToRemove != null) {
            NamespaceCache namespaceCache = delegateCache.get(templateCacheToRemove.getDelegate());
            if (namespaceCache != null) {
                TemplateCache templateCache = namespaceCache.get(templateCacheToRemove.getNamespace());
                if (templateCache == templateCacheToRemove) {
                    namespaceCache.remove(templateCacheToRemove.getNamespace());
                    if (namespaceCache.isEmpty()) {
                        delegateCache.remove(templateCacheToRemove.getNamespace());
                    }
                }
            }
        }
    }

    private DelegateCache getDelegateCache(VirtualFile file) {
        Module module = fileIndex.getModuleForFile(file);
        return module == null ? null : DelegateCache.getDelegateCache(module);
    }

    /**
     * Diagnostic class, run in a separate thread, that monitors the cache and
     * logs changes for a single project.
     */
    @SuppressWarnings("HardCodedStringLiteral")
    private class CacheDebugger implements Runnable {

        private final Reference<Project> project;
        private final long offset = System.currentTimeMillis() % 250;
        private final Map<Module,DelegateCache> lastVersions = new HashMap<Module,DelegateCache>();

        private long lastUpdate = 0L;

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
                    Arrays.asList(ModuleManager.getInstance(project).getModules())
            );
            lastVersions.keySet().retainAll(modules);
            modules.removeAll(lastVersions.keySet());
            for (Module module : modules) {
                lastVersions.put(module, new DelegateCache(module));
            }
            
            for (Module module : lastVersions.keySet()) {
                DelegateCache current = DelegateCache.getDelegateCache(module);
                DelegateCache previous = lastVersions.get(module);
                logChanges(current, current, previous, log, "");
                lastVersions.put(module, current.clone());
            }
        }

        private String getLabel(@NotNull Object obj) {
            if (obj instanceof TemplateCache) {
                String namespace = ((TemplateCache)obj).getNamespace();
                if (NamespaceCache.DEFAULT_NAMESPACE.equals(namespace)) {
                    return "TemplateCache for default namespace";
                } else {
                    return "TemplateCache for {namespace " + namespace + "}";
                }
            } else if (obj instanceof NamespaceCache) {
                String delegate = ((NamespaceCache)obj).getDelegate();
                if (DelegateCache.DEFAULT_DELEGATE.equals(delegate)) {
                    return "NamespaceCache for default delegate";
                } else {
                    return "NamespaceCache for {delpackage " + delegate + "}";
                }
            } else if (obj instanceof DelegateCache) {
                return "DelegateCache for module '" + ((DelegateCache)obj).getModule().getName() + "'";
            } else {
                return String.valueOf(obj);
            }
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
                        changeCount += logChanges(cValue, cValue, pValue, log, nextIndent);
                    } else {
                        changeCount++;
                        logReplaced(nextIndent, p.getValue(), c.getValue(), log);
                    }
                    c = cIter.hasNext() ? cIter.next() : null;
                    p = pIter.hasNext() ? pIter.next() : null;
                }
            }
            while (c != null) {
                if (c.getValue() instanceof NavigableMap) {
                    @SuppressWarnings("unchecked")
                    NavigableMap<String,Object> cValue = (NavigableMap<String,Object>)c.getValue();
                    changeCount += logChanges(cValue, cValue, null, log, nextIndent);
                } else {
                    changeCount++;
                    logAdded(nextIndent, c.getValue(), log);
                }
                c = cIter.hasNext() ? cIter.next() : null;
            }
            while (p != null) {
                if (p.getValue() instanceof NavigableMap) {
                    @SuppressWarnings("unchecked")
                    NavigableMap<String,Object> pValue = (NavigableMap<String,Object>)p.getValue();
                    changeCount += logChanges(pValue, null, pValue, log, nextIndent);
                } else {
                    changeCount++;
                    logRemoved(nextIndent, p.getValue(), log);
                }
                p = pIter.hasNext() ? pIter.next() : null;
            }
            if (changeCount > 0 && parent != null) {
                log.print(indent);
                log.print("recorded ");
                log.print(changeCount);
                log.print(changeCount == 1 ? " change to " : " changes to ");
                log.println(getLabel(parent));
            }
            return changeCount;
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
}
