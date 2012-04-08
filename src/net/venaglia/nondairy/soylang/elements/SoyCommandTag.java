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

import com.intellij.lang.ASTNode;
import net.venaglia.nondairy.soylang.elements.path.PsiElementCollection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: 1/21/12
 * Time: 8:50 AM
 *
 * Base class for SoyPsiElement objects that represent a soy command.
 */
public class SoyCommandTag extends SoyPsiElement {

    @NonNls
    private static final Pattern MATCH_TAG_NAME = Pattern.compile("\\w+");

    @NonNls
    private static final Set<String> ALWAYS_UNARY_COMMANDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "namespace", "delpackage", "else", "elseif", "case", "default", "ifempty"
    )));

    /**
     * Enumeration of tag boundaries.
     */
    public enum Boundary {
        /** A tag that begins a block, where matching close tag is expected */
        BEGIN,

        /** A tag that end a block, where matching begin tag is expected */
        END,

        /** A tag that stands on its own, and is not part of a pair */
        UNARY
    }

    public SoyCommandTag(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    public String getCommand() {
        Matcher matcher = MATCH_TAG_NAME.matcher(getText());
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    @Nullable
    public String getFoldedLabel() {
        final int labelLength = 32;
        String command = getCommand();
        if (command != null) {
            String text = getText();
            String label = text.substring(command.length() + 1, text.length() - 1).trim();
            if (label.length() > labelLength) {
                label = label.substring(0, labelLength - 3) + "...";
            }
            return label;
        }
        return null;
    }
    
    public Boundary getBoundary() {
        String text = getText();
        Matcher matcher = MATCH_TAG_NAME.matcher(text);
        if (matcher.find() && ALWAYS_UNARY_COMMANDS.contains(matcher.group())) {
            return Boundary.UNARY;
        }
        if (text.endsWith("/}")) {
            return Boundary.UNARY;
        }
        if (text.startsWith("{/")) {
            return Boundary.END;
        }
        return Boundary.BEGIN;
    }

    @Nullable
    public String getDelegatePackage() {
        PsiElementCollection elements = NamespaceMemberElement.PATH_TO_DELEGATE_PACKAGE.navigate(this);
        DelegatePackageElement delegatePackageElement = (DelegatePackageElement)elements.oneOrNull();
        return delegatePackageElement != null ? delegatePackageElement.getDelegatePackage() : null;
    }
}
