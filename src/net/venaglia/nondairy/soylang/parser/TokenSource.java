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

package net.venaglia.nondairy.soylang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.i18n.I18N;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * User: ed
 * Date: Aug 15, 2010
 * Time: 7:37:31 PM
 *
 * Abstraction class to facilitate unit testing and simplify common operations
 * in the parser classes.
 */
public abstract class TokenSource {

    /**
     * Shortcut to skip past elements until the specified end token is reached.
     * @param endToken the token to stop at.
     * @param markerType an optional marker to wrap the skipped sequence with,
     *     typically an error marker.
     * @return A count of the number of token skipped over
     */
    public int fastForward(IElementType endToken, @Nullable IElementType markerType) {
        PsiBuilder.Marker errorMarker = markerType == null ? null : mark("errorMarker");
        int count = 0;
        while (!eof()) {
            IElementType token = token();
            advance();
            count ++;
            if (token == endToken) break;
        }
        if (errorMarker != null) errorMarker.done(markerType);
        return count;
    }

    /**
     * Inserts a new marker immediately after the current token.
     * @param name An object that can be used for debugging purposes. This
     *     object's toString() value will be included in debugging output.
     * @return the marker object to be closed or discarded later by the parser.
     */
    public abstract PsiBuilder.Marker mark(@NonNls Object name);

    /**
     * @return the type of the current token.
     */
    public abstract IElementType token();

    /**
     * @return The string that the current token was generated from.
     */
    public abstract String text();

    /**
     * @return true if this token source has reached the end of the file.
     */
    public abstract boolean eof();

    /**
     * Advances to the next token.
     */
    public abstract void advance();

    /**
     * @return the current index within this token source;
     */
    public abstract int index();

    /**
     * Shortcut to skip over the current element and mark it with the specified
     * IElementType.
     * @param type The type to mark the current element with.
     * @param name The name to mark this element with, used for debugging.
     */
    public void advanceAndMark(IElementType type, @NonNls Object name) {
        PsiBuilder.Marker marker = mark(name);
        advance();
        marker.done(type);
    }

    /**
     * Shortcut to skip over the current element and mark it with an error with
     * the specified IElementType.
     * @param type The type to mark the current element with.
     * @param name The name to mark this element with, used for debugging.
     */
    public void advanceAndMarkBad(IElementType type, @NonNls Object name) {
        PsiBuilder.Marker marker = mark(name);
        errorBadToken();
        advance();
        marker.done(type);
    }

    /**
     * Shortcut to skip over the current element and mark it with an error with
     * the specified IElementType.
     * @param type The type to mark the current element with.
     * @param name The name to mark this element with, used for debugging.
     * @param message The error message to associate with the current element.
     */
    public void advanceAndMarkBad(IElementType type, @NonNls Object name, String message) {
        PsiBuilder.Marker marker = mark(name);
        error(message);
        advance();
        marker.done(type);
    }

    /**
     * Inserts an error mark into the PSI tree at the current point
     * @param message The error message.
     */
    public abstract void error(String message);

    /**
     * Shortcut to mark the most recently created marker, that is not yet
     * marked done, as an error, with a default message.
     */
    public void errorBadToken() {
        error(I18N.msg("lexer.error.unexpected.token", token()));
    }

    public interface MarkerAndIndex {
        int getIndex();
        PsiBuilder.Marker getMarker();
    }
}
