package net.venaglia.nondairy.soylang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import net.venaglia.nondairy.i18n.I18N;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 15, 2010
 * Time: 7:37:31 PM
 */
public abstract class TokenSource {

    public void fastForward(IElementType endToken, IElementType markerType) {
        PsiBuilder.Marker errorMarker = markerType == null ? null : mark();
        while (!eof()) {
            IElementType token = token();
            advance();
            if (token == endToken) break;
        }
        if (errorMarker != null) errorMarker.done(markerType);
    }

    public void fastForwardAndMarkBad(IElementType endToken, IElementType markerType, String errorMessage) {
        PsiBuilder.Marker errorMarker = mark();
        error(errorMessage);
        while (!eof()) {
            IElementType token = token();
            advance();
            if (token == endToken) {
                errorMarker.done(markerType);
                return;
            }
        }
        errorMarker.done(markerType);
    }

    public abstract PsiBuilder.Marker mark();

    public abstract IElementType token();

    public abstract String text();

    public abstract boolean eof();

    public abstract void advance();

    public void advanceAndMark(IElementType type) {
        PsiBuilder.Marker marker = mark();
        advance();
        marker.done(type);
    }

    public void advanceAndMarkBad(IElementType type) {
        PsiBuilder.Marker marker = mark();
        errorBadToken();
        advance();
        marker.done(type);
    }

    public void advanceAndMarkBad(IElementType type, String message) {
        PsiBuilder.Marker marker = mark();
        error(message);
        advance();
        marker.done(type);
    }

    public abstract void error(String message);

    public void errorBadToken() {
        error(I18N.msg("lexer.error.unexpected.token", token()));
    }
}
