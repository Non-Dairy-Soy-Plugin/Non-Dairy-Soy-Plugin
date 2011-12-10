package net.venaglia.nondairy.soylang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import net.venaglia.nondairy.soylang.lexer.SoyLexer;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Sep 5, 2010
 * Time: 7:08:59 PM
 */
public class SoyFindUsagesProvider implements FindUsagesProvider {

    public SoyFindUsagesProvider() {
    }

    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new SoyLexer(),
                                       SoyToken.NAME_TOKENS,
                                       SoyToken.COMMENT_TOKENS,
                                       TokenSet.create(SoyToken.STRING_LITERAL));
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        ASTNode node = psiElement.getNode();
        return node != null && SoyToken.NAME_TOKENS.contains(node.getElementType());
    }

    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        return "";
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        return getNodeText(element, true);
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof PsiNamedElement) {
            final String name = ((PsiNamedElement)element).getName();
            if (name != null) {
                return name;
            }
        }
        return "";
    }
}
