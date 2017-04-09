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

package net.venaglia.nondairy.soylang.elements;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.refactoring.rename.BindablePsiReference;
import com.intellij.util.IncorrectOperationException;
import net.venaglia.nondairy.soylang.elements.path.ElementPredicate;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import net.venaglia.nondairy.soylang.elements.path.PsiElementPath;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * User: ed
 * Date: 1/20/12
 * Time: 10:09 PM
 *
 * Implementation of PsiReference that uses {@link PsiElementPath} objects to
 * find elements in the psi tree. This class provide support for the
 * {@link BindablePsiReference} and {@link PsiPolyVariantReference} interfaces
 * dynamically delegating their implementation through {@link BindHandler} and
 * {@link ResolveHandler} objects.
 */
public class SoyPsiElementReference extends PsiReferenceBase<SoyPsiElement> {

    private final PsiElementPath path;
    private final ElementPredicate predicate;
    private final boolean withRange;

    private BindHandler bindHandler = null;
    private ResolveHandler resolveHandler = null;

    private long lastFoundAt = 0L;
    private PsiElement lastFound = null;

    /**
     * Builds a SoyPsiElementReference that resolves to itself.
     * @param myElement The element this reference is built for.
     */
    public SoyPsiElementReference(@NotNull SoyPsiElement myElement) {
        this(myElement, PsiElementPath.SELF, null, null, null, null);
    }

    /**
     * Builds a SoyPsiElementReference that resolves to itself, identifying
     * range as the source of the reference.
     * @param myElement The element this reference is built for.
     * @param range The range to use as the reference for the source element.
     */
    public SoyPsiElementReference(@NotNull SoyPsiElement myElement, @NotNull TextRange range) {
        this(myElement, PsiElementPath.SELF, null, null, null, range);
    }

    @SuppressWarnings({ "unchecked" })
    public SoyPsiElementReference(@NotNull SoyPsiElement myElement,
                                  @NotNull PsiElementPath path,
                                  @Nullable ElementPredicate predicate) {
        this(myElement, path, predicate, null, null, null);
    }

    /**
     * Builds a SoyPsiElementReference that resolves using the specified path
     * and an optional predicate. An optional range can also be specified as
     * the source of the reference.
     * @param myElement The element this reference is built for.
     * @param path The path to navigate to find the referenced element.
     * @param predicate A predicate to apply to the results of the path
     *                  traversal.
     * @param range The range to use as the reference for the source element.
     */
    public SoyPsiElementReference(@NotNull SoyPsiElement myElement,
                                  @NotNull PsiElementPath path,
                                  @Nullable ElementPredicate predicate,
                                  @Nullable TextRange range) {
        this(myElement, path, predicate, null, null, range);
    }

    /**
     * This constructor is the delegate of all public constructors. Subclasses
     * that are built to extend the interfaces supported by this class, must
     * provide a public constructor with a signature that matches this one.
     * @param myElement The starting element this reference begins with.
     * @param path The path this element should follow to find the references
     *     element(s).
     * @param predicate The predicate to apply to elements found by the path.
     * @param bindHandler Optional BindHandler implementation, required for
     *     supporting the BindablePsiReference interface.
     * @param resolveHandler Optional ResolveHandler implementaion, required
     *     for supporting the PsiPolyVariantReference interface.
     * @param range Optional range object describing the source range for this
     *     reference.
     */
    SoyPsiElementReference(@NotNull SoyPsiElement myElement,
                           @NotNull PsiElementPath path,
                           @Nullable ElementPredicate predicate,
                           @Nullable BindHandler bindHandler,
                           @Nullable ResolveHandler resolveHandler,
                           @Nullable TextRange range) {
        super(myElement, range);
        this.path = path;
        this.predicate = predicate;
        this.bindHandler = bindHandler;
        this.resolveHandler = resolveHandler;
        withRange = range != null;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        String cn = myElement.getCanonicalName();
        return cn == null ? super.getCanonicalText() : cn;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        if (resolveHandler != null) {
            return resolveHandler.resolve(this, incompleteCode);
        }
        return new ResolveResult[0];
    }

    @Override
    @Nullable
    public PsiElement resolve() {
        if (lastFoundAt > 0L && lastFoundAt > SoyPsiElement.getLastCreated()) {
            return lastFound;
        }
        if (resolveHandler != null) {
            ResolveResult[] resolveResults = multiResolve(false);
            return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
        }
        PsiElementCollection elements = path.navigate(myElement);
        if (predicate != null) {
            elements = elements.applyPredicate(predicate);
        }
        PsiElement element = elements.oneOrNull();
        lastFound = element;
        lastFoundAt = System.currentTimeMillis();
        return element;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        if (resolveHandler != null) {
            final ResolveResult[] results = multiResolve(false);
            for (ResolveResult result : results) {
                if (getElement().getManager().areElementsEquivalent(result.getElement(), element)) {
                    return true;
                }
            }
            return false;
        }
        return (predicate == null || predicate.test(element)) &&
                resolve() == element;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        PsiElementCollection elements = path.navigate(myElement);
        Collection<Object> objects = new LinkedList<Object>();
        for (PsiElement element : elements) {
            objects.add(LookupElementBuilder.create((PsiNamedElement)element));
        }
        return objects.toArray();
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return myElement.setName(newElementName);
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        if (bindHandler != null) {
            return bindHandler.bind(myElement, element);
        }
        return super.bindToElement(element);
    }

    /**
     * Extends this SoyPsiElementReference to implement BindablePsiReference.
     * @param bindHandler Handler to provide a custom implementation for
     *     {@link BindablePsiReference#bindToElement(com.intellij.psi.PsiElement)}.
     * @return A new object with the same logic as this reference, that also
     *     implements the BindablePsiReference interface.
     * @throws IllegalStateException if this references already implements
     *     BindablePsiReference.
     */
    public BindablePsiReference bound(@NotNull BindHandler bindHandler) {
        return ReferenceFactory.factoryFor(this)
                               .extend(BindablePsiReference.class)
                               .create(getConstructorParams(bindHandler, null));
    }

    /**
     * Extends this SoyPsiElementReference to implement PsiPolyVariantReference.
     * @param resolveHandler Handler to provide a custom implementation for
     *     {@link PsiPolyVariantReference#multiResolve(boolean)}.
     * @return A new object with the same logic as this reference, that also
     *     implements the PsiPolyVariantReference interface.
     * @throws IllegalStateException if this references already implements
     *     PsiPolyVariantReference.
     */
    public PsiPolyVariantReference poly(@NotNull ResolveHandler resolveHandler) {
        return ReferenceFactory.factoryFor(this)
                               .extend(PsiPolyVariantReference.class)
                               .create(getConstructorParams(null, resolveHandler));
    }

    /**
     * This class and its subclasses are required to provide a normalized
     * constructor signature. This method provides the proper constructor
     * parameters to match that constructor.
     * @param bindHandler Handler to provide a custom implementation for
     *     {@link BindablePsiReference#bindToElement(com.intellij.psi.PsiElement)}.
     * @param resolveHandler Handler to provide a custom implementation for
     *     {@link PsiPolyVariantReference#multiResolve(boolean)}.
     * @return an array of objects matching the normalized constructor
     *     signature.
     */
    Object[] getConstructorParams(@Nullable BindHandler bindHandler,
                                  @Nullable ResolveHandler resolveHandler) {
        return new Object[]{
                myElement,
                path,
                predicate,
                bindHandler == null ? this.bindHandler : bindHandler,
                resolveHandler == null ? this.resolveHandler : resolveHandler,
                withRange ? getRangeInElement() : null,
        };
    }

    private static class Bound extends SoyPsiElementReference implements BindablePsiReference {

        public Bound(SoyPsiElement myElement,
                     PsiElementPath path,
                     @Nullable ElementPredicate predicate,
                     BindHandler bindHandler,
                     ResolveHandler resolveHandler,
                     TextRange range) {
            super(myElement, path, predicate, bindHandler, resolveHandler, range);
        }
    }

    private static class Poly extends SoyPsiElementReference implements PsiPolyVariantReference {

        public Poly(@NotNull SoyPsiElement myElement,
                    @NotNull PsiElementPath path,
                    @Nullable ElementPredicate predicate,
                    @Nullable BindHandler bindHandler,
                    @Nullable ResolveHandler resolveHandler,
                    @Nullable TextRange range) {
            super(myElement, path, predicate, bindHandler, resolveHandler, range);
        }
    }

    private static class BoundPoly extends SoyPsiElementReference implements BindablePsiReference, PsiPolyVariantReference {

        public BoundPoly(@NotNull SoyPsiElement myElement,
                         @NotNull PsiElementPath path,
                         @Nullable ElementPredicate predicate,
                         @Nullable BindHandler bindHandler,
                         @Nullable ResolveHandler resolveHandler,
                         @Nullable TextRange range) {
            super(myElement, path, predicate, bindHandler, resolveHandler, range);
        }
    }

    /**
     * Helper class to construct the proper SoyPsiElementReference based on a
     * combination of supported interfaces.
     * @param <R> The specific type this factory will create.
     */
    private static class ReferenceFactory<R> {

        /** The interfaces we can support support */
        private static final Set<Class<?>> SUPPORTED_INTERFACES;

        /** The factory instances, keyed by the interfaces they support */
        private static Map<Set<Class<?>>,ReferenceFactory<?>> FACTORIES_BY_INTERFACE_SET;

        static {
            Set<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(PsiPolyVariantReference.class);
            classes.add(BindablePsiReference.class);
            SUPPORTED_INTERFACES = Collections.unmodifiableSet(classes);
            Map<Set<Class<?>>,ReferenceFactory<?>> factoriesByInterfaceSet = new HashMap<Set<Class<?>>,ReferenceFactory<?>>();
            ReferenceFactory<SoyPsiElementReference> defaultFactory =
                    new ReferenceFactory<SoyPsiElementReference>() {
                        @Override
                        SoyPsiElementReference create(@NotNull Object[] params) {
                            return new SoyPsiElementReference(
                                    (SoyPsiElement)params[0],
                                    (PsiElementPath)params[1],
                                    (ElementPredicate)params[2],
                                    (BindHandler)params[3],
                                    (ResolveHandler)params[4],
                                    (TextRange)params[5]
                            );
                        }
                    };
            factoriesByInterfaceSet.put(defaultFactory.supportedInterfaces, defaultFactory);
            for (Class<?> cls : SoyPsiElementReference.class.getDeclaredClasses()) {
                if (SoyPsiElementReference.class.isAssignableFrom(cls)) {
                    ReferenceFactory<?> factory = initFor(cls);
                    factoriesByInterfaceSet.put(factory.supportedInterfaces, factory);
                }
            }
            FACTORIES_BY_INTERFACE_SET = Collections.unmodifiableMap(factoriesByInterfaceSet);
        }

        private final Class<R> type;
        private final Constructor<R> ctor;
        private final Set<Class<?>> supportedInterfaces;

        private ReferenceFactory() {
            type = null;
            ctor = null;
            supportedInterfaces = Collections.emptySet();
        }

        private ReferenceFactory(Class<R> type) {
            this.type = type;
            try {
                this.ctor = type.getConstructor(SoyPsiElement.class,
                                                PsiElementPath.class,
                                                ElementPredicate.class,
                                                BindHandler.class,
                                                ResolveHandler.class,
                                                TextRange.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            Set<Class<?>> interfaces = new HashSet<Class<?>>(Arrays.asList(this.type.getInterfaces()));
            interfaces.retainAll(SUPPORTED_INTERFACES);
            this.supportedInterfaces = Collections.unmodifiableSet(interfaces);
        }

        /**
         * Creates a new SoyPsiElementReference from the passed params.
         * @param params params suitable for the normalized constructor.
         * @return The new reference object.
         */
        R create(@NotNull Object[] params) {
            try {
                return ctor.newInstance(params);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Finds a factory to build references that support all current
         * interfaces, plus the one passed.
         * @param iface The interface that also needs ot be implemented.
         * @param <N> The new interface.
         * @return A ReferenceFactory that also support the specified
         *     interface.
         * @throws IllegalStateException if the current factory already
         *     supports the specified interface.
         * @throws UnsupportedOperationException if no factory is available to
         *     support the passed interface.
         */
        @NotNull
        <N> ReferenceFactory<? extends N> extend(Class<N> iface) {
            if (supportedInterfaces.contains(iface)) {
                throw new IllegalStateException("Interface is already implemented: " + iface.getSimpleName());
            }
            Set<Class<?>> newIfaces = new HashSet<Class<?>>(supportedInterfaces);
            newIfaces.add(iface);
            return lookupFailIfNotFound(newIfaces);
        }

        /**
         * Finds a factory applicable for an existing refernce object.
         * @param base The existing reference to obtain a factory for.
         * @return A factory that can build a reference that supports the same
         *     interfaces as the passed object.
         * @throws UnsupportedOperationException if no factory is available to
         *     support the passed interface.
         */
        @NotNull
        static ReferenceFactory<?> factoryFor(SoyPsiElementReference base) {
            Set<Class<?>> supportedInterfaces = new HashSet<Class<?>>();
            for (Class<?> i : SUPPORTED_INTERFACES) {
                if (i.isInstance(base)) {
                    supportedInterfaces.add(i);
                }
            }
            return lookupFailIfNotFound(supportedInterfaces);
        }

        @NotNull
        private static <N> ReferenceFactory<? extends N> lookupFailIfNotFound(Set<Class<?>> supportedInterfaces) {
            ReferenceFactory<N> factory = lookup(supportedInterfaces);
            if (factory == null) {
                @NonNls
                StringBuilder buffer = new StringBuilder();
                buffer.append("Unsupported combination of interfaces: [");
                boolean first = true;
                for (Class<?> i : supportedInterfaces) {
                    if (first) first = false; else buffer.append(", ");
                    buffer.append(i.getSimpleName());
                }
                buffer.append("]");
                throw new UnsupportedOperationException(buffer.toString());
            }
            return factory;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        private static <N> ReferenceFactory<N> lookup(Set<Class<?>> supportedInterfaces) {
            return (ReferenceFactory<N>)FACTORIES_BY_INTERFACE_SET.get(supportedInterfaces);
        }
        
        private static <R> ReferenceFactory<R> initFor(Class<R> type) {
            return new ReferenceFactory<R>(type);
        }
    }
}
