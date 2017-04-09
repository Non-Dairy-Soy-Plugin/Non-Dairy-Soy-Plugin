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

package net.venaglia.nondairy.soylang.elements.path;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

/**
 * User: ed
 * Date: 3/15/12
 * Time: 6:10 PM
 */
public class PushPopPredicate {

    private static Key<Deque<PsiElementCollection>> KEY = new Key<Deque<PsiElementCollection>>("push/pop predicate");
    
    public static ElementPredicate push() {
        return new InstancePredicate() {
            @Override
            public TraversalPredicate getInstance(Map<Key, Object> navigationData) {
                Deque<PsiElementCollection> deque = KEY.get(navigationData);
                if (deque == null) {
                    deque = new LinkedList<PsiElementCollection>();
                    KEY.set(navigationData, deque);
                }
                return new PushImpl(deque);
            }

            @NonNls
            @Override
            public String toString() {
                return "push()";
            }
        };
    }

    public static ElementPredicate pushPredicate(final ElementPredicate predicate) {
        return new InstancePredicate() {
            @Override
            public TraversalPredicate getInstance(Map<Key, Object> navigationData) {
                Deque<PsiElementCollection> deque = KEY.get(navigationData);
                if (deque == null) {
                    deque = new LinkedList<PsiElementCollection>();
                    KEY.set(navigationData, deque);
                }
                return new PushPredicateImpl(deque, predicate);
            }

            @NonNls
            @Override
            public String toString() {
                return "push().filter(" + predicate + ")";
            }
        };
    }

    public static ElementPredicate swap() {
        return new InstancePredicate() {
            @Override
            public TraversalPredicate getInstance(Map<Key, Object> navigationData) {
                Deque<PsiElementCollection> deque = KEY.get(navigationData);
                if (deque == null) {
                    deque = new LinkedList<PsiElementCollection>();
                    KEY.set(navigationData, deque);
                }
                return new SwapImpl(deque);
            }

            @NonNls
            @Override
            public String toString() {
                return "swap()";
            }
        };
    }

    public static ElementPredicate pop() {
        return new InstancePredicate() {
            @Override
            public TraversalPredicate getInstance(Map<Key, Object> navigationData) {
                Deque<PsiElementCollection> deque = KEY.get(navigationData);
                if (deque == null) {
                    deque = new LinkedList<PsiElementCollection>();
                    KEY.set(navigationData, deque);
                }
                return new PopImpl(deque);
            }

            @NonNls
            @Override
            public String toString() {
                return "pop()";
            }
        };
    }

    public static ElementPredicate popAdd() {
        return new InstancePredicate() {
            @Override
            public TraversalPredicate getInstance(Map<Key, Object> navigationData) {
                Deque<PsiElementCollection> deque = KEY.get(navigationData);
                if (deque == null) {
                    deque = new LinkedList<PsiElementCollection>();
                    KEY.set(navigationData, deque);
                }
                return new PopAddImpl(deque);
            }

            @NonNls
            @Override
            public String toString() {
                return "pop().add()";
            }
        };
    }

    public static ElementPredicate popAndJoin(final PopJoin join) {
        return new InstancePredicate() {
            @Override
            public ElementPredicate getInstance(Map<Key, Object> navigationData) {
                Deque<PsiElementCollection> deque = KEY.get(navigationData);
                if (deque == null) {
                    deque = new LinkedList<PsiElementCollection>();
                    KEY.set(navigationData, deque);
                }
                return new PopJoinImpl(deque, join);
            }

            @NonNls
            @Override
            public String toString() {
                return "pop().join(" + join + ")";
            }
        };
    }

    public static boolean isStackEmpty(@Nullable Map<Key,Object> navigationData) {
        Deque<PsiElementCollection> deque = navigationData == null ? null : KEY.get(navigationData);
        if (deque != null && !deque.isEmpty()) {
            for (PsiElementCollection elements : deque) {
                if (!elements.isEmpty()) return false;
            }
        }
        return true;

    }

    private static class PushImpl implements TraversalPredicate.AlwaysTrue {

        private final Deque<PsiElementCollection> deque;

        public PushImpl(Deque<PsiElementCollection> deque) {
            this.deque = deque;
        }

        @NotNull
        @Override
        public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
            PsiElementCollection result = PsiElementCollection.castOrCopy(current);
            deque.push(new PsiElementCollection(current));
            return result;
        }

        @Override
        public boolean test(PsiElement element) {
            return true;
        }

        @Override
        public String toString() {
            return "push()   "; // NON-NLS
        }
    }

    private static class PushPredicateImpl implements TraversalPredicate.AlwaysTrue {

        private final Deque<PsiElementCollection> deque;
        private final ElementPredicate predicate;

        public PushPredicateImpl(Deque<PsiElementCollection> deque, ElementPredicate predicate) {
            this.deque = deque;
            this.predicate = predicate;
        }

        @NotNull
        @Override
        public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
            PsiElementCollection result = new PsiElementCollection();
            PsiElementCollection toPush = new PsiElementCollection();
            for (PsiElement element : current) {
                if (predicate.test(element)) {
                    toPush.add(element);
                } else {
                    result.add(element);
                }
            }
            deque.push(toPush);
            return result;
        }

        @Override
        public boolean test(PsiElement element) {
            return true;
        }

        @Override
        public String toString() {
            return "push(?)  "; // NON-NLS
        }
    }

    @NoMatchHanding(onStart = TraverseEmpty.CONTINUE)
    private static class SwapImpl implements TraversalPredicate.AlwaysTrue {

        private final Deque<PsiElementCollection> deque;

        public SwapImpl(Deque<PsiElementCollection> deque) {
            this.deque = deque;
        }

        @NotNull
        @Override
        public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
            PsiElementCollection elements = deque.pop();
            deque.push(PsiElementCollection.castOrCopy(current));
            return elements;
        }

        @Override
        public boolean test(PsiElement element) {
            return true;
        }


        @Override
        public String toString() {
            return "pop()    "; // NON-NLS
        }
    }

    @NoMatchHanding(onStart = TraverseEmpty.CONTINUE)
    private static class PopImpl implements TraversalPredicate.AlwaysTrue {

        private final Deque<PsiElementCollection> deque;

        public PopImpl(Deque<PsiElementCollection> deque) {
            this.deque = deque;
        }

        @NotNull
        @Override
        public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
            return deque.pop();
        }

        @Override
        public boolean test(PsiElement element) {
            return true;
        }


        @Override
        public String toString() {
            return "pop()    "; // NON-NLS
        }
    }

    @NoMatchHanding(onStart = TraverseEmpty.CONTINUE)
    private static class PopAddImpl implements TraversalPredicate.AlwaysTrue {

        private final Deque<PsiElementCollection> deque;

        public PopAddImpl(Deque<PsiElementCollection> deque) {
            this.deque = deque;
        }

        @NotNull
        @Override
        public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
            PsiElementCollection result = new PsiElementCollection(current);
            result.addAll(deque.pop());
            return result;
        }

        @Override
        public boolean test(PsiElement element) {
            return true;
        }


        @Override
        public String toString() {
            return "popAdd() "; // NON-NLS
        }
    }

    @NoMatchHanding(onStart = TraverseEmpty.CONTINUE)
    private static class PopJoinImpl implements TraversalPredicate.AlwaysTrue {

        private final Deque<PsiElementCollection> deque;
        private final PopJoin join;

        public PopJoinImpl(Deque<PsiElementCollection> deque, PopJoin join) {
            this.deque = deque;
            this.join = join;
        }

        @NotNull
        @Override
        public PsiElementCollection traverse(@NotNull Collection<PsiElement> current) {
            return join.join(PsiElementCollection.castOrCopy(current), deque.pop());
        }

        @Override
        public boolean test(PsiElement element) {
            return true;
        }


        @Override
        public String toString() {
            return "popJoin()"; // NON-NLS
        }
    }

    public interface PopJoin {
        PsiElementCollection join (PsiElementCollection current, PsiElementCollection popped);
    }
}
