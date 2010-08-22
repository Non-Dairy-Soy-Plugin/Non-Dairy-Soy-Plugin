/*
 * Copyright 2010 Ed Venaglia
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

package net.venaglia.nondairy.soylang.lexer;

import org.jetbrains.annotations.NonNls;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 13, 2010
 * Time: 2:39:59 PM
 */
public class SoyDocCommentBuffer {

    public enum InferredDataType {
        SCALAR, MAP, LIST, AMBIGUIOUS, UNDEFINED;

        public InferredDataType mergeWith(InferredDataType other) {
            if (other == null || other == this) return this;
            if (this == UNDEFINED) return other;
            return AMBIGUIOUS;
        }
    }

    @NonNls
    private static final Pattern MATCH_IDENTIFIER = Pattern.compile("[a-z_][0-9a-z_]*(\\.[a-z_][0-9a-z_]*)*");

    @NonNls
    private static final Pattern MATCH_ANTICIPATE_PARAM_NAME = Pattern.compile("\\s*([*]\\s*)?@param\\s*");

    private boolean changed = false;
    private String description = null;
    private StringBuilder buffer;
    private StringBuilder capturedText;
    private Map<String,String> parameterDescriptionByName = new LinkedHashMap<String,String>();
    private Map<String,Boolean> parameterOptionalByName = new LinkedHashMap<String,Boolean>();
    private Map<String,InferredDataType> parameterUsageByName = new LinkedHashMap<String,InferredDataType>();
    private String currentParameterName = null;
    private String namespace = null;
    private String templateName = "['?']";
    private int templateDeclarationLine = -1;

    public SoyDocCommentBuffer() {
        this((String)null);
    }

    public SoyDocCommentBuffer(String namespace) {
        this.namespace = namespace;
        this.buffer = new StringBuilder();
        this.capturedText = new StringBuilder();
    }

    public SoyDocCommentBuffer(String namespace, String buffer) {
        this.namespace = namespace;
        this.buffer = new StringBuilder(buffer);
        this.capturedText = new StringBuilder(buffer.length() + 10);
        close();
    }

    public SoyDocCommentBuffer(SoyDocCommentBuffer that) {
        this.changed = that.changed;
        this.description = that.description;
        this.buffer = new StringBuilder(that.buffer);
        this.capturedText = new StringBuilder(that.capturedText);
        this.parameterDescriptionByName = new LinkedHashMap<String,String>(that.parameterDescriptionByName);
        this.parameterOptionalByName = new LinkedHashMap<String,Boolean>(that.parameterOptionalByName);
        this.parameterUsageByName = new LinkedHashMap<String,InferredDataType>(that.parameterUsageByName);
        this.currentParameterName = that.currentParameterName;
    }

    private String appendLine(String to, String line) {
        if (to == null || to.length() == 0) return line;
        return (to + " " + line).trim();
    }

    private void appendLine(String line) {
        if (capturedText.length() > 0) capturedText.append('\n');
        capturedText.append(line);
        Boolean optional = null;
        line = line.trim();
        if (line.startsWith("*")) line = line.substring(1).trim();
        if (line.startsWith("@param")) { //NON-NLS
            optional = line.length() > 6 && line.charAt(6) == '?';
            line = optional ? line.substring(7).trim() : line.substring(6).trim();
            Matcher matcher = MATCH_IDENTIFIER.matcher(line);
            currentParameterName = "";
            if (matcher.find()) {
                currentParameterName = matcher.group(0);
                line = line.substring(currentParameterName.length()).trim();
            }
        }
        if (currentParameterName == null) {
            description = appendLine(description, line);
            if (description.length() > 0) changed = true;
        } else if (currentParameterName.length() > 0) {
            if (optional != null) {
                parameterOptionalByName.put(currentParameterName, optional);
            }
            line = appendLine(parameterDescriptionByName.get(currentParameterName), line);
            parameterDescriptionByName.put(currentParameterName, line);
            changed = true;
        }
    }

    public void reset() {
        changed = false;
        description = null;
        currentParameterName = null;
        parameterDescriptionByName.clear();
        parameterOptionalByName.clear();
        parameterUsageByName.clear();
        buffer.setLength(0);
    }

    private void flush() {
        String[] lines = buffer.toString().split("\n");
        for (int i = 0, j = lines.length - 1; i < j; i++) {
            appendLine(lines[i]);
        }
        buffer.setLength(0);
        if (lines.length > 0) buffer.append(lines[lines.length - 1]);
    }

    public void close() {
        flush();
        if (buffer.length() > 0) {
            appendLine(buffer.toString());
            buffer.setLength(0);
        }
    }

    public SoyDocCommentBuffer closeCloneAndReset() {
        close();
        SoyDocCommentBuffer newBuffer = new SoyDocCommentBuffer(this);
        reset();
        return newBuffer;
    }

    public void append(CharSequence text) {
        if (text != null) this.buffer.append(text);
    }

//    public boolean hasValue() {
//        return changed;
//    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public int getTemplateDeclarationLine() {
        return templateDeclarationLine;
    }

    public void setTemplateDeclarationLine(int templateDeclarationLine) {
        this.templateDeclarationLine = templateDeclarationLine;
    }

    public String getFQTemplateName() {
        return (namespace == null ? "" : namespace) + (templateName == null ? "???" : templateName);
    }

    public boolean anticipatingParameterName() {
        flush();
        return MATCH_ANTICIPATE_PARAM_NAME.matcher(buffer).matches();
    }

    public void parameterUsed(String parameterName) {
        InferredDataType currentUsage = parameterUsageByName.get(parameterName);
        if (currentUsage == null) currentUsage = InferredDataType.UNDEFINED;
        parameterUsageByName.put(parameterName, currentUsage.mergeWith(InferredDataType.AMBIGUIOUS));
    }

    public CharSequence getCapturedText() {
        return capturedText;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getFQTemplateName()).append("(");
        boolean first = true;
        for (String paramName : parameterDescriptionByName.keySet()) {
            if (first) first = false; else buffer.append(",");
            buffer.append(paramName);
        }
        buffer.append(");");
        return buffer.toString();
    }
}
