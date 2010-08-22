package net.venaglia.nondairy.soylang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 14, 2010
 * Time: 4:53:07 PM
 */
public class PsiBuilderTokenSource extends TokenSource {

    public static final boolean DEBUG_PSI_BUILDER = true;

    private final PsiBuilder builder;

    public PsiBuilderTokenSource(PsiBuilder builder) {
        this.builder = builder;
        this.builder.setDebugMode(DEBUG_PSI_BUILDER);
    }

    public PsiBuilder.Marker mark() {
        return builder.mark();
    }

    public IElementType token() {
        return builder.getTokenType();
    }

    public String text() {
        return builder.getTokenText();
    }

    public boolean eof() {
        return builder.eof();
    }

    public void advance() {
        builder.advanceLexer();
    }

    public void error(String message) {
        // Errors are suppressed for now
//        builder.error(message);
    }

}
