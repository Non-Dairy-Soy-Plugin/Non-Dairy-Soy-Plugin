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

package net.venaglia.nondairy.soylang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: ed
 * Date: 12/10/11
 * Time: 2:00 PM
 *
 * Subclass of PsiBuilderTokenSource that can track the psi tree as it is being
 * built. This class creates an HTML file that captures every discrete change
 * as it is made on the psi tree.
 */
public class TrackedPsiBuilderTokenSource extends PsiBuilderTokenSource {

    final List<Node<?>> tree = new NodeList();
    final AtomicInteger nodeSeq = new AtomicInteger();

    private Node<? extends IElementType> tokenForTree = null;

    public TrackedPsiBuilderTokenSource(PsiBuilder builder) {
        super(builder);
    }

    private void loadTokenForTree() {
        if (tokenForTree == null) {
            if (super.eof()) {
                tokenForTree = new Node<SoyToken>(new Stack(nodeSeq.getAndIncrement()), SoyToken.EOF, true);
            } else {
                tokenForTree = new Node<IElementType>(new Stack(nodeSeq.getAndIncrement()), super.token(), true);
                tokenForTree.setText(super.text());
            }
        }
    }

    private void addTokenToTree() {
        if (tokenForTree != null) {
            tree.add(tokenForTree);
            tokenForTree = null;
        }
    }

    private <T> T addToTree(T value, boolean immutable) {
        return addToTree(value, null, immutable);
    }

    private <T> T addToTree(T value, @Nullable String text, boolean immutable) {
        if (tree.isEmpty() || tree.get(tree.size() - 1) != value) {
            Node<T> node = new Node<T>(value, immutable);
            node.setText(text);
        }
        return value;
    }

    @Override
    public PsiBuilder.Marker mark(@NonNls Object name) {
        addTokenToTree();
        return new TrackedMarker(super.mark(name), String.valueOf(name));
//        return super.mark(name);
    }

    @Override
    public void advance() {
        addTokenToTree();
        super.advance();
        loadTokenForTree();
    }

    @Override
    public void error(String message) {
        super.error(message);
        addToTree(message, true);
    }

    public List<Node<?>> getTree() {
        addTokenToTree();
        return tree;
    }

    public List<Node<?>> getTree(int upToSeq) {
        addTokenToTree();
        NodeList tree = new NodeList(this.tree.size(), upToSeq);
        for (Node<?> node : this.tree) {
            tree.add(node.upTo(upToSeq));
        }
        return tree;
    }

    public int getLastSeq() {
        addTokenToTree();
        return nodeSeq.get() - 1;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public void writeHtml(PrintWriter out) {
        final StringBuilder options = new StringBuilder(1024);
        final StringBuilder frames = new StringBuilder(16384);
        HtmlTemplate template = new HtmlTemplate(options, frames);
        OutputBuffer buffer = new OutputBuffer() {

            private int level = -1;
            private int nodeLevel = -1;
            private String nodeText = null;

            private int toLevel(int level, String css) {
                int delta = 0;
                for (; this.level < level; this.level++) {
                    frames.append("<div class=\"").append(css).append("\">\n");
                    delta++;
                }
                for (; this.level > level; this.level--) {
                    frames.append("</div>\n");
                    delta--;
                }
                return delta;
            }

            private void mouseover() {
                if (toLevel(nodeLevel + 1, "mouseover") > 0) {
                    frames.append("<a href=\"#\" class=\"label\">detail</a>\n");
                    frames.append(nodeText);
                }
            }
            
            @Override
            public void node(int level, Node<?> node) {
                closeNode();
                toLevel(level, "level");
                nodeLevel = level;
                String css = "node-text";
                if (!node.immutable && node.done == null && node.dropped == null && node.error == null) {
                    css = "node-text node-open";
                }
                String fmt = node.text == null
                             ? "<span id=\"node_%d\" class=\"%s\"><span class=\"name\">%s</span> &lt;<span class=\"type\">%s</span>&gt;</span>\n"
                             : "<span id=\"node_%d\" class=\"%s\"><span class=\"name\">%s</span> &lt;<span class=\"type\">%s</span>&gt; <span class=\"text\">%s</span></span>\n";
                nodeText = String.format(fmt,
                                         node.created.seq,
                                         css,
                                         String.valueOf(node.value).replace("&","&amp;").replace("<","&lt;"),
                                         String.valueOf(node.value.getClass().getSimpleName()),
                                         escape(node.text).replace("&", "&amp;").replace("<","&lt;"));
                frames.append("<div class=\"node\">${node_text}");
            }

            @Override
            public void warn(String message) {
                mouseover();
                frames.append("<li class=\"warn\">").append(message.replace("<","&lt;")).append("</li>\n");
            }

            @Override
            public void info(String verb, Stack stack) {
                mouseover();
                frames.append("<li class=\"info\"><span class=\"verb\">").append(verb).append("</span> at [<span class=\"seq\">").append(stack.seq).append("</span>] <span class=\"source\">").append(stack.getWhere()).append("</span></li>\n");
            }

            @Override
            public void close() {
                closeNode();
                toLevel(-1, "level");
            }

            private void closeNode() {
                if (nodeText != null) {
                    int start = frames.indexOf("${node_text}");
                    frames.replace(start, start + 12, nodeText);
                    frames.append("</div>");
                    nodeText = null;
                }
            }
        };
        for (int i = 0, l = getLastSeq(); i < l; ++i) {
            options.append("<option value=\"frame_").append(i).append("\"").append(i == l - 1 ? " selected" : "").append(">Step ").append(i + 1).append("</option>\n");
            frames.append("<div id=\"frame_").append(i).append("\">\n");
            toString(buffer, 0, getTree(i), -1);
            buffer.close();
            frames.append("</div>\n");
        }
        out.print(template);
    }

    private void toString(OutputBuffer buffer, int level, List<Node<?>> tree, int includeTraceForSeq) {
        if (tree.isEmpty()) return;
        for (int i = 0, l = tree.size(); i < l; ++i) {
            Node<?> node = tree.get(i);
            if (node == null) continue;
            toString(buffer, level, node, includeTraceForSeq);
            int indexOffset = node.index;
            int closeIndex = node.getCloseIndex();
            boolean silent = includeTraceForSeq >= 0 && node.created.seq != includeTraceForSeq;
            closeIndex = closeIndex > Integer.MIN_VALUE ? i + closeIndex - indexOffset : closeIndex;
            if (closeIndex > tree.size()) {
                toString(buffer, silent, "This node is closed after the current scope");
            } else if (closeIndex == i + 1) {
                toString(buffer, silent, "This node is closed with no content");
            } else if (closeIndex > i) {
                toString(buffer, level + 1, tree.subList(i + 1, closeIndex), includeTraceForSeq);
                i = closeIndex;
            } else if (closeIndex == Integer.MIN_VALUE) {
                // nothing to do, this is a simple node
            } else if (closeIndex < 0) {
                toString(buffer, silent, "This node is closed before the current scope");
            } else if (closeIndex == 0) {
                toString(buffer, silent, "This node is closed on itself -- this shouldn't be possible");
            } else {
                toString(buffer, silent, "This node is closed before it was opened -- this shouldn't be possible");
            }
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void toString(OutputBuffer buffer, boolean silent, @NonNls String message) {
        if (!silent) buffer.warn(message);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void toString(OutputBuffer buffer, int level, Node<?> node, int includeTraceForSeq) {
        if (node == null) return;
        buffer.node(level, node);
        if (!node.immutable) {
            toString(buffer, "created", node, includeTraceForSeq);
        } else if (node.value instanceof SoyToken) {
            toString(buffer, "scanned", node, includeTraceForSeq);
        } else {
            toString(buffer, "marked", node, includeTraceForSeq);
        }
        toString(buffer, "dropped", node.dropped, includeTraceForSeq);
        toString(buffer, "done", node.done, includeTraceForSeq);
        toString(buffer, "error", node.error, includeTraceForSeq);
        if (!node.immutable && node.done == null && node.dropped == null && node.error == null) {
            boolean silent = includeTraceForSeq >= 0 && node.created.seq != includeTraceForSeq;
            toString(buffer, silent, "THIS NODE IS NOT CLOSED!!!");
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void toString(OutputBuffer buffer, @NonNls String verb, Node<?> node, int includeTraceForSeq) {
        if (node == null) return;
        toString(buffer, verb, node.created, includeTraceForSeq);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void toString(OutputBuffer buffer, @NonNls String verb, Stack stack, int includeTraceForSeq) {
        if (stack == null) return;
        if (includeTraceForSeq != -1 && includeTraceForSeq != stack.seq) return;
        buffer.info(verb, stack);
    }

    public class Node<T> {
        private final Stack created;
        private final T value;
        private final boolean immutable;

        private int index;
        private String text;

        private Node<?> done = null;
        private Stack dropped = null;
        private Node<?> error = null;

        private Node(Stack created, T value, boolean immutable) {
            this.created = created;
            this.value = value;
            this.immutable = immutable;
        }

        public Node(T value, boolean immutable) {
            this(new Stack(nodeSeq.getAndIncrement()), value, immutable);
            this.index = tree.size();
            tree.add(this);
        }

        public Node(T value, boolean immutable, Node<?> before) {
            this(new Stack(nodeSeq.getAndIncrement()), value, immutable);
            before.checkState("precede", true);
            this.index = before.index;
            tree.add(index, this);
            for (int i = index + 1, l = tree.size(); i < l; ++i) {
                tree.get(i).index++;
            }
        }

        public T getValue() {
            return value;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getCloseIndex() {
            if (done != null) {
                return done.index;
            }
            if (error != null) {
                return error.index;
            }
            return Integer.MIN_VALUE;
        }

        void checkState(@NonNls String newState, boolean mayBeClosed) {
            @NonNls String whyNot = null;
            if (immutable) {
                whyNot = "created immutable";
            } else if (dropped != null) {
                whyNot = "not found in the current tree";
            } else if (done != null && !mayBeClosed) {
                whyNot = "already marked done";
            } else if (error != null && !mayBeClosed) {
                whyNot = "already marked error";
            }
            if (whyNot != null) {
                @NonNls String msg = "Cannot %s: this marker was %s [%s]";
                throw new RuntimeException(String.format(msg, newState, whyNot, this));
            }
        }

        public void drop() {
            checkState("drop", false);
            this.dropped = new Stack(nodeSeq.getAndIncrement());
        }

        public <DONE> void markAsDone(DONE done) {
            checkState("mark as done", false);
            this.done = new Node<DONE>(done, true);
        }

        public <ERR> void markAsError(ERR err) {
            checkState("mark as error", false);
            this.error = new Node<ERR>(err, true);
        }

        Node<T> upTo(int seq) {
            if (created.seq > seq) {
                return null;
            }
            if (done != null && done.created.seq <= seq) {
                return this;
            }
            if (dropped != null && dropped.seq <= seq) {
                return this;
            }
            if (error != null && error.created.seq <= seq) {
                return this;
            }
            Node<T> clone = new Node<T>(created, value, immutable);
            clone.index = index;
            clone.text = text;
            clone.done = (done == null) ? null : done.upTo(seq);
            clone.dropped = (dropped == null || dropped.seq > seq) ? null : dropped;
            clone.error = (error == null) ? null : error.upTo(seq);
            return clone;
        }
        
        @SuppressWarnings("HardCodedStringLiteral")
        public String toString() {
            if (text == null) {
                return String.format("%s<%s>", value, value.getClass().getSimpleName());
            } else {
                return String.format("%s<%s> \"%s\"", value, value.getClass().getSimpleName(), escape(text));
            }
        }

        public void checkReadyForDone() {
            List<Node<?>> undone = new LinkedList<Node<?>>();
            for (int i = index, l = tree.size(); i < l; ++i) {
                Node<?> n = tree.get(i);
                if (n.immutable) continue;
                if (n.dropped == null && n.done == null && n.error == null) {
                    undone.add(n);
                }
            }
            if (undone.isEmpty()) {
                throw new RuntimeException("Node [" + this + "] is already marked done");
            } else if (undone.size() > 1 || undone.get(0) != this) {
                throw new RuntimeException("Node [" + this + "] cannot be marked done, it contains undone markers: " + undone);
            }
        }
    }

    @SuppressWarnings({ "HardCodedStringLiteral", "ConstantConditions" })
    private static String escape (String text) {
        if (text == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder(text.length() * 2);
        b.append('"');
        for (int i = 0, l = text.length(); i < l; ++i) {
            char c = text.charAt(i);
            switch (c) {
                case '\n': b.append("\\n"); break;
                case '\t': b.append("\\t"); break;
                case '\f': b.append("\\f"); break;
                case '\b': b.append("\\b"); break;
                case '\0': b.append("\\0"); break;
                case '\1': b.append("\\1"); break;
                case '\2': b.append("\\2"); break;
                case '\3': b.append("\\3"); break;
                case '\4': b.append("\\4"); break;
                case '\5': b.append("\\5"); break;
                case '\6': b.append("\\6"); break;
                case '\7': b.append("\\7"); break;
                case '\\': b.append("\\\\"); break;
                default:
                    if (c < ' ') {
                        b.append("\\x").append(Integer.toHexString(c));
                    } else if (c > 127) {
                        b.append(String.format("\\u%04x", (int)c));
                    } else {
                        b.append(c);
                    }
            }
        }
        b.append('"');
        return b.toString();
    }
    
    public class TrackedMarker implements PsiBuilder.Marker {

        private final PsiBuilder.Marker marker;
        private final String name;
        private final Node<TrackedMarker> node;

        public TrackedMarker(PsiBuilder.Marker marker, @NonNls String name) {
            this.marker = marker;
            this.name = name;
            this.node = new Node<TrackedMarker>(this, false);
        }

        public TrackedMarker(PsiBuilder.Marker marker, @NonNls String name, Node<?> before) {
            this.marker = marker;
            this.name = name;
            this.node = new Node<TrackedMarker>(this, false, before);
        }

        @Override
        public PsiBuilder.Marker precede() {
            return new TrackedMarker(marker.precede(), "before(" + name + ")", node);
        }

        @Override
        public void drop() {
            marker.drop();
            node.drop();
        }

        @Override
        public void rollbackTo() {
            throw new UnsupportedOperationException("rollbackTo() is not used by this project");
        }

        @Override
        public void done(IElementType type) {
            node.checkReadyForDone();
            marker.done(type);
            node.markAsDone(type);
        }

        @Override
        public void collapse(IElementType type) {
            throw new UnsupportedOperationException("collapse() is not used by this project");
        }

        @Override
        public void doneBefore(IElementType type, PsiBuilder.Marker before) {
            throw new UnsupportedOperationException("doneBefore() is not used by this project");
        }

        @Override
        public void doneBefore(IElementType type, PsiBuilder.Marker before, String errorMessage) {
            throw new UnsupportedOperationException("doneBefore() is not used by this project");
        }

        @Override
        public void error(String message) {
            marker.error(message);
            node.markAsError(message);
        }

        @Override
        public void errorBefore(String message, PsiBuilder.Marker before) {
            throw new UnsupportedOperationException("errorBefore() is not used by this project");
        }

        @Override
        public void setCustomEdgeTokenBinders(@Nullable WhitespacesAndCommentsBinder left,
                                              @Nullable WhitespacesAndCommentsBinder right) {
            throw new UnsupportedOperationException("setCustomEdgeTokenBinders() is not used by this project");
        }

        @Override
        public String toString() {
            return name == null ? marker.toString() : name;
        }
    }

    public class Stack extends Throwable {
        public final int seq;

        public Stack(int seq) {
            this.seq = seq;
        }
        
        @SuppressWarnings("HardCodedStringLiteral")
        public StackTraceElement getWhere() {
            for (StackTraceElement ste : getStackTrace()) {
                if (ste.getClassName().endsWith("Parser")) {
                    return ste;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            StackTraceElement where = getWhere();
            return where == null ? super.toString() : where.toString();
        }
    }

    private class NodeList extends ArrayList<Node<?>> {

        private final int includingUpTo;

        private NodeList() {
            includingUpTo = -1;
        }

        private NodeList(int allocateSize, int includingUpTo) {
            super(allocateSize);
            this.includingUpTo = includingUpTo;
        }

        @Override
        public String toString() {
            final StringBuilder out = new StringBuilder();
            OutputBuffer buffer = new OutputBuffer() {
                
                private int level = 0;
                
                @Override
                public void node(int level, Node<?> node) {
                    for (int i = -2; i < level; ++i) out.append("  ");
                    this.level = level;
                    out.append(node);
                    out.append("\n");
                }

                @SuppressWarnings("HardCodedStringLiteral")
                @Override
                public void warn(String message) {
                    for (int i = -2; i < level; ++i) out.append("  ");
                    out.append("  ! ");
                    out.append(message);
                    out.append("\n");
                }

                @SuppressWarnings("HardCodedStringLiteral")
                @Override
                public void info(String verb, Stack stack) {
                    for (int i = -2; i < level; ++i) out.append("  ");
                    out.append("  > ");
                    out.append(verb);
                    out.append(" at [");
                    out.append(stack.seq);
                    out.append("] ");
                    out.append(stack.getWhere());
                    out.append("\n");
                }

                @Override
                public void close() {
                }
            };
            TrackedPsiBuilderTokenSource.this.toString(buffer, 0, this, includingUpTo);
            return out.toString();
        }
    }
    
    private interface OutputBuffer {
        void node(int level, Node<?> node);
        void warn(String message);
        void info(String verb, Stack stack);
        void close();
    }

    private static class HtmlTemplate {

        private final StringBuilder options;
        private final StringBuilder frames;

        public HtmlTemplate(StringBuilder options, StringBuilder frames) {
            //To change body of created methods use File | Settings | File Templates.
            this.options = options;
            this.frames = frames;
        }

        @NonNls
        private static String getCSS() {
            return "" +
                    "        body {\n" +
                    "          font-family: Arial, sans-serif;\n" +
                    "        }\n" +
                    "        .instructions {\n" +
                    "          font-size: 10px;\n" +
                    "          color: #999;\n" +
                    "          float: right;\n" +
                    "        }\n" +
                    "        #frames > * {\n" +
                    "          display: none;\n" +
                    "        }\n" +
                    "        #frames > .visible {\n" +
                    "          display: inherit;\n" +
                    "        }\n" +
                    "        #frames div {\n" +
                    "          padding-left: 12px;\n" +
                    "          font-size: 12px;\n" +
                    "          color: #333;\n" +
                    "        }\n" +
                    "        #frames div .mouseover {\n" +
                    "          padding: 4px;\n" +
                    "        }\n" +
                    "        .node {\n" +
                    "          position: relative;\n" +
                    "        }\n" +
                    "        .mouseover {\n" +
                    "          position: absolute;\n" +
                    "          top: 0px;\n" +
                    "          right: 0;\n" +
                    "          width: 64px;\n" +
                    "          overflow: hidden;\n" +
                    "          height: 14px;\n" +
                    "          border: 1px none transparent;\n" +
                    "        }\n" +
                    "        .mouseover > * {\n" +
                    "          visibility: hidden;\n" +
                    "        }\n" +
                    "        .mouseover:hover, .mouseover.hover {\n" +
                    "          top: -4px;\n" +
                    "          overflow: visible;\n" +
                    "          height: auto;\n" +
                    "          width: 100%;\n" +
                    "          border: 1px solid #333;\n" +
                    "          background-color: #ffe;\n" +
                    "          z-index: 2;\n" +
                    "        }\n" +
                    "        .mouseover:hover > *, .mouseover.hover > * {\n" +
                    "          visibility: visible;\n" +
                    "        }\n" +
                    "        .mouseover .label {\n" +
                    "          position: absolute;\n" +
                    "          top: 0;\n" +
                    "          right: 0;\n" +
                    "          visibility: visible;\n" +
                    "          line-height: 14px;\n" +
                    "        }\n" +
                    "        .mouseover:hover .label, .mouseover.hover .label {\n" +
                    "          visibility: hidden;\n" +
                    "        }\n" +
                    "        .info { color: #006; }\n" +
                    "        .warn { color: #630; }\n" +
                    "        .verb { color: #393; }\n" +
                    "        .seq { color: #000; font-weight: bold; }\n" +
                    "        .source { color: #33C; }\n" +
                    "        .name { color: #000; font-weight: bold }\n" +
                    "        .type { color: #C3C; }\n" +
                    "        .text { color: #33C; }\n" +
                    "        .node-open > .name { color: #930; }\n" +
                    "        .timestamp { color: #000; font-size: 9px; padding-top: 64px; }";
        }

        @NonNls
        private String getJavaScript() {
            return "" +
                    "        var visible = null;\n" +
                    "        function update() {\n" +
                    "          if (visible) visible.className = \"\";\n" +
                    "          var sel = document.getElementById(\"seq\");\n" +
                    "          var val = sel.options[sel.selectedIndex].value;\n" +
                    "          visible = document.getElementById(val);\n" +
                    "          if (visible) visible.className = \"visible\";\n" +
                    "        }\n" +
                    "        function keyDown(code) {\n" +
                    "          var sel = document.getElementById(\"seq\");\n" +
                    "          switch(code) {\n" +
                    "            case 37: // left arrow\n" +
                    "              sel.selectedIndex = Math.max(0, sel.selectedIndex - 1);\n" +
                    "              update();\n" +
                    "              break;\n" +
                    "            case 39: // right arrow\n" +
                    "              sel.selectedIndex = Math.min(sel.options.length - 1, sel.selectedIndex + 1);\n" +
                    "              update();\n" +
                    "              break;\n" +
                    "          }\n" +
                    "        }";
        }

        private String getOptions() {
            return options.toString();
        }

        private String getTree() {
            return frames.toString();
        }

        private String getTimestamp() {
            return new Date().toString();
        }

        @NonNls
        @Override
        public String toString() {
            return "" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <title>Replay of a PSI tree as it was built</title>\n" +
                    "    <style type=\"text/css\">\n" +
                    getCSS() + "\n" +
                    "    </style>\n" +
                    "    <script type=\"text/javascript\">\n" +
                    getJavaScript() + "\n" +
                    "    </script>\n" +
                    "</head>\n" +
                    "<body onload=\"update();\" onkeydown=\"keyDown(event.keyCode);\">\n" +
                    "<select id=\"seq\" onChange=\"update();\" onClick=\"update();\">\n" +
                    getOptions() + "\n" +
                    "</select>\n" +
                    "<div class=\"instructions\">Use left and right arrow keys to navigate back and forth</div>\n" +
                    "<hr/>\n" +
                    "<div id=\"frames\">\n" +
                    getTree() + "\n" +
                    "</div>\n" +
                    "<div class=\"timestamp\">Report created at " + getTimestamp() + ": </div>\n" +
                    "</body>\n" +
                    "</html>\n";
        }
    }
}
