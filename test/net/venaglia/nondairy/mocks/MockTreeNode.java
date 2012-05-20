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

package net.venaglia.nondairy.mocks;

import com.intellij.lang.ASTNode;
import com.intellij.lang.FileASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.IdentityCharTable;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.CharTable;
import net.venaglia.nondairy.soylang.elements.factory.PsiElementFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * User: ed
 * Date: 2/20/12
 * Time: 1:17 PM
 */
@SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
public class MockTreeNode implements ASTNode {

    private static final IElementType ERROR = new IElementType("GENERIC_ERROR_NODE", Language.ANY);

    MockTreeNode parent;

    MockTreeNode prevSibling;
    MockTreeNode nextSibling;

    List<MockTreeNode> children;

    IElementType type;
    String errorMessage;
    int startOffset;
    CharSequence text;
    Map<Key<?>,Object> userData;
    Map<Key<?>,Object> copyableUserData;

    PsiElement psiElement;

    @Override
    public IElementType getElementType() {
        return type;
    }

    @Override
    public String getText() {
        return text.toString();
    }

    @Override
    public CharSequence getChars() {
        return text;
    }

    @Override
    public boolean textContains(char c) {
        for (int i = 0, l = text.length(); i < l; ++i) {
            if (text.charAt(i) == c) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public int getTextLength() {
        return text.length();
    }

    @Override
    public TextRange getTextRange() {
        return new TextRange(startOffset, startOffset + text.length());
    }

    @Override
    public ASTNode getTreeParent() {
        return parent;
    }

    @Override
    public ASTNode getFirstChildNode() {
        return children == null ? null : children.get(0);
    }

    @Override
    public ASTNode getLastChildNode() {
        return children == null ? null : children.get(children.size() - 1);
    }

    @Override
    public ASTNode getTreeNext() {
        return nextSibling;
    }

    @Override
    public ASTNode getTreePrev() {
        return prevSibling;
    }

    public List<MockTreeNode> getMockChildren() {
        return children;
    }

    @Override
    public ASTNode[] getChildren(@Nullable TokenSet filter) {
        List<MockTreeNode> filtered = null;
        if (filter == null) {
            filtered = children;
        } else if (children != null) {
            filtered = new ArrayList<MockTreeNode>(children.size());
            for (MockTreeNode node : children) {
                if (filter.contains(node.getElementType())) {
                    filtered.add(node);
                }
            }
        }
        if (filtered == null || filtered.isEmpty()) {
            return EMPTY_ARRAY;
        }
        return filtered.toArray(new ASTNode[filtered.size()]);
    }

    @Override
    public void addChild(@NotNull ASTNode child) {
        if (children == null) {
            children = new ArrayList<MockTreeNode>();
        }
        children.add(makeMeTheParent(coerce(child)));
        relinkChildren();
    }

    @Override
    public void addChild(@NotNull ASTNode child, @Nullable ASTNode anchorBefore) {
        if (anchorBefore == null) {
            addChild(child);
            return;
        }
        children.add(indexOfChild(anchorBefore), makeMeTheParent(coerce(child)));
        relinkChildren();
    }

    @Override
    public void addLeaf(@NotNull IElementType leafType, CharSequence leafText, ASTNode anchorBefore) {
        MockTreeNode node = new MockLeafNode();
        node.type = leafType;
        node.text = text;
        node.startOffset = startOffset;
        addChild(node, anchorBefore);
    }

    @Override
    public void removeChild(@NotNull ASTNode child) {
        children.remove(indexOfChild(child)).orphan();
        relinkChildren();
    }

    @Override
    public void removeRange(@NotNull ASTNode firstNodeToRemove, ASTNode firstNodeToKeep) {
        children.subList(indexOfChild(firstNodeToRemove), indexOfChild(firstNodeToKeep));
        relinkChildren();
    }

    @Override
    public void replaceChild(@NotNull ASTNode oldChild, @NotNull ASTNode newChild) {
        children.set(indexOfChild(oldChild), makeMeTheParent(coerce(newChild)));
        relinkChildren();
    }

    @Override
    public void replaceAllChildrenToChildrenOf(ASTNode anotherParent) {
        MockTreeNode parent = coerce(anotherParent);
        if (children != null) {
            for (MockTreeNode child : children) {
                child.orphan();
            }
            children.clear();
        }
        if (parent.children != null) {
            for (MockTreeNode child : parent.children) {
                child.orphan();
            }
            if (children == null) {
                children = new ArrayList<MockTreeNode>();
            }
            for (MockTreeNode node : parent.children) {
                children.add(makeMeTheParent(node));
            }
            parent.children.clear();
            relinkChildren();
        }
    }

    @Override
    public void addChildren(ASTNode firstChild, ASTNode firstChildToNotAdd, ASTNode anchorBefore) {
        List<MockTreeNode> nodes = new ArrayList<MockTreeNode>();
        int i = indexOfChild(anchorBefore);
        for (ASTNode child = firstChild; child != firstChildToNotAdd; child = child.getTreeNext()) {
            nodes.add(coerce(child));
        }
        for (MockTreeNode node : nodes) {
            children.add(i++, makeMeTheParent(node));
        }
        relinkChildren();
    }

    protected MockTreeNode emptyNewOfSameType() {
        return new MockTreeNode();
    }
    
    @Override
    public MockTreeNode copyElement() {
        MockTreeNode copy = emptyNewOfSameType();
        if (children != null && children.size() > 0) {
            copy.children = new ArrayList<MockTreeNode>(children.size());
            for (MockTreeNode node : children) {
                MockTreeNode childCopy = node.copyElement();
                copy.children.add(childCopy);
                childCopy.parent = copy;
            }
            copy.relinkChildren();
        }
        if (copyableUserData != null) {
            copy.copyableUserData = new HashMap<Key<?>,Object>(copyableUserData);
        }
        return copy;
    }

    @Override
    public ASTNode findLeafElementAt(int offset) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCopyableUserData(Key<T> key) {
        if (copyableUserData == null) {
            return null;
        }
        return (T)copyableUserData.get(key);
    }

    @Override
    public <T> void putCopyableUserData(Key<T> key, T value) {
        if (copyableUserData == null) {
            copyableUserData = new HashMap<Key<?>,Object>();
        }
        copyableUserData.put(key, value);
    }

    @Override
    public ASTNode findChildByType(IElementType type) {
        if (children != null) {
            for (MockTreeNode node : children) {
                if (type.equals(node.getElementType())) {
                    return node;
                }
            }
        }
        return null;
    }

    @Override
    public ASTNode findChildByType(@NotNull TokenSet typesSet) {
        if (children != null) {
            for (MockTreeNode node : children) {
                if (typesSet.contains(node.getElementType())) {
                    return node;
                }
            }
        }
        return null;
    }

    @Override
    public ASTNode findChildByType(@NotNull IElementType type, @Nullable ASTNode anchor) {
        return findChildByType(TokenSet.create(type), anchor);
    }

    @Override
    public ASTNode findChildByType(@NotNull TokenSet typesSet, @Nullable ASTNode anchor) {
        if (children != null && anchor instanceof MockTreeNode) {
            for (int i = indexOfChild(anchor), l = children.size(); i < l; i++) {
                MockTreeNode node = children.get(i);
                if (typesSet.contains(node.getElementType())) {
                    return node;
                }
            }
        }
        return null;
    }

    @Override
    public PsiElement getPsi() {
        return psiElement;
    }

    @Override
    public <T extends PsiElement> T getPsi(Class<T> clazz) {
        if (psiElement != null && clazz.isInstance(psiElement)) {
            return clazz.cast(psiElement);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        if (userData == null) {
            return null;
        }
        return (T)userData.get(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        if (userData == null) {
            userData = new HashMap<Key<?>,Object>();
        }
        userData.put(key, value);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private void orphan() {
        this.parent = null;
        if (this.prevSibling != null) {
            this.prevSibling.nextSibling = nextSibling;
        }
        if (this.nextSibling != null) {
            this.nextSibling.prevSibling = prevSibling;
        }
    }

    private void relinkChildren() {
        for (int i = 0, l = children.size(); i <= l; i++) {
            MockTreeNode prev = i > 0 ? children.get(i - 1) : null;
            MockTreeNode next = i < l ? children.get(i) : null;
            if (prev != null) {
                prev.nextSibling = next;
                prev.parent = this;
            }
            if (next != null) {
                next.prevSibling = prev;
            }
        }
    }

    private int indexOfChild(@NotNull ASTNode node) {
        if (children != null && children.size() > 0 && node instanceof MockTreeNode) {
            return children.indexOf(coerce(node));
        }
        throw new NoSuchElementException();
    }

    private MockTreeNode makeMeTheParent(MockTreeNode node) {
        if (node.parent != null) {
            if (node.parent == this) {
                return node;
            }
            node.parent.removeChild(node);
            node.parent = null;
        }
        node.parent = this;
        return node;
    }
    
    private MockTreeNode coerce(ASTNode node) {
        if (node == null || node instanceof MockTreeNode) {
            return (MockTreeNode)node;
        }
        throw new IllegalArgumentException("Only MockTreeNode objects are supported in tests");
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean indent) {
        StringBuffer buffer = new StringBuffer();
        toString(indent ? "\n    " : null, buffer);
        return buffer.toString();
    }

    private void toString(String indent, StringBuffer buffer) {
        String name = psiElement == null ? null : psiElement.getClass().getSimpleName();
        String s = String.format("%s <%s> \"%s\"", type, name, text); //NON-NLS
        if (indent != null) {
            buffer.append(indent);
        }
        buffer.append(s);
        if (children != null) {
            String childIndent = indent == null ? null : indent + "    ";
            for (MockTreeNode child : children) {
                child.toString(childIndent, buffer);
            }
        }
    }

    public static class MockFileNode extends MockTreeNode implements FileASTNode {

        @NotNull
        @Override
        public CharTable getCharTable() {
            return IdentityCharTable.INSTANCE;
        }

        @Override
        public boolean isParsed() {
            return true;
        }


    }

    private static class MockLeafNode extends MockTreeNode {

        private MockLeafNode() {
            // leaf nodes cannot have children
            children = Collections.emptyList();
        }

        @Override
        protected MockLeafNode emptyNewOfSameType() {
            return new MockLeafNode();
        }
    }

    public static class Builder {

        private IElementType type;
        private CharSequence text;
        private String errorMessage;
        private int startOffset = Integer.MIN_VALUE;
        private List<Builder> children;
        private PsiFile psiFile;
        private boolean built;

        public Builder() {
        }

        public Builder(Builder clone, PsiFile psiFile) {
            this.type = clone.type;
            this.startOffset = clone.startOffset;
            this.text = clone.text;
            this.children = clone.children;
            this.psiFile = psiFile;
            clone.children = null;
            clone.built = true;
        }

        public void setType(IElementType type) {
            this.type = type;
        }

        public void setText(CharSequence text) {
            this.text = text;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }

        public void addChild(Builder child) {
            if (children == null) {
                children = new ArrayList<Builder>();
            }
            children.add(child);
        }

        public MockTreeNode build(PsiElementFactory factory) {
            if (built) {
                throw new IllegalStateException();
            }
            if (type == null || errorMessage != null) {
                type = ERROR;
            }
            if (type == null || startOffset == Integer.MIN_VALUE || text == null) {
                throw new IllegalStateException();
            }
            MockTreeNode node = newNode();
            node.type = type;
            node.text = text;
            node.startOffset = startOffset;
            node.errorMessage = errorMessage;
            if (children != null) {
                node.children = new ArrayList<MockTreeNode>(children.size());
                for (Builder cb : children) {
                    MockTreeNode child = cb.build(factory);
                    child.parent = node;
                    node.children.add(child);
                }
                node.relinkChildren();
                node.psiElement = psiFile != null ? psiFile : factory.create(node);
                if (psiFile instanceof MockSoyFile) {
                    ((MockSoyFile)psiFile).setNode((FileASTNode)node);
                }
            } else {
                node.psiElement = factory.create(node);
            }
            built = true;
            return node;
        }

        protected MockTreeNode newNode() {
            if (psiFile != null) {
                return new MockFileNode();
            }
            return new MockTreeNode();
        }
    }

}
