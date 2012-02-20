package net.venaglia.nondairy.soylang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import net.venaglia.nondairy.i18n.I18N;
import net.venaglia.nondairy.soylang.elements.TemplateMemberElement;
import net.venaglia.nondairy.soylang.lexer.SoyToken;
import net.venaglia.nondairy.soylang.lexer.SoyWordScanningLexer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: ed
 * Date: Sep 5, 2010
 * Time: 7:08:59 PM
 *
 * FindUsagesProvider implementation to support IntelliJ find usages on soy
 * psi elements.
 */
public class SoyFindUsagesProvider implements FindUsagesProvider {

    private static final Map<IElementType,UsageType> USAGE_TYPES_BY_ELEMENT_TYPE;

    static {
        HashMap<IElementType, UsageType> map = new HashMap<IElementType, UsageType>();
        for (UsageType ut : UsageType.values()) {
            for (IElementType et : ut.tokens.getTypes()) {
                if (!map.containsKey(et)) {
                    map.put(et, ut);
                }
            }
        }
        USAGE_TYPES_BY_ELEMENT_TYPE = Collections.unmodifiableMap(map);
    }

    public SoyFindUsagesProvider() {
    }

    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new SoyWordScanningLexer(),
                                       SoyToken.NAME_TOKENS,
                                       SoyToken.COMMENT_TOKENS,
                                       TokenSet.create(SoyToken.STRING_LITERAL));
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return getUsageType(psiElement) != UsageType.UNSUPPORTED;
    }

    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @Nullable
    static String getDefault(@NotNull PsiElement element) {
        if (element instanceof PsiNamedElement) {
            String text = ((PsiNamedElement)element).getName();
            if (text != null) {
                return text;
            }
        }
        return element.getText();
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement psiElement) {
        return defaultString(getUsageType(psiElement).getType());
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        return defaultString(getUsageType(element).getDescriptiveName(element));
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return defaultString(getUsageType(element).getNodeText(element, useFullName));
    }

    @NotNull
    private UsageType getUsageType(@NotNull PsiElement element) {
        ASTNode node = element.getNode();
        UsageType usageType = node == null ? null : USAGE_TYPES_BY_ELEMENT_TYPE.get(node.getElementType());
        return usageType == null ? UsageType.UNSUPPORTED :  usageType;
    }

    @NotNull
    private String defaultString (@Nullable String value) {
        return value == null ? "" : value;
    }

    /**
     * Enumeration used to declare the logic used to describe soy psi elements
     * that support "find usages".
     */
    enum UsageType {
        PARAMETER(SoyElement.PARAMETER_NAME_TOKENS, "find.usage.type.variable") {
            @Override
            @Nullable
            String getShortName(@NotNull PsiElement element) {
                String text = getDefault(element);
                if (text != null) {
                    return text.startsWith("$") ? text.substring(1) : text;
                }
                return null;
            }

            @Override
            @Nullable
            String getFullName(@NotNull PsiElement element) {
                if (element instanceof TemplateMemberElement) {
                    TemplateMemberElement tme = (TemplateMemberElement)element;
                    return tme.getTemplateName() + "$" + getShortName(element);
                }
                return super.getFullName(element);
            }
        },
        FUNCTION(SoyElement.FUNCTION_NAME_TOKENS, "find.usage.type.function"),
        TEMPLATE_ABSOLUTE(TokenSet.create(SoyElement.template_name_ref_absolute),
                          "find.usage.type.template"),
        TEMPLATE_LOCAL(TokenSet.create(SoyElement.template_name,
                                       SoyElement.template_name_ref),
                       "find.usage.type.template") {
            @Override
            String getFullName(@NotNull PsiElement element) {
                if (element instanceof TemplateMemberElement) {
                    return ((TemplateMemberElement)element).getTemplateName();
                }
                return super.getFullName(element);
            }
        },
        PROPERTY(SoyElement.PROPERTY_NAME_TOKENS, "find.usage.type.property"),
        UNSUPPORTED(TokenSet.EMPTY, null) {
            @Override
            @Nullable
            String getType() {
                return null;
            }

            @Override
            @Nullable
            String getDescriptiveName(@NotNull PsiElement element) {
                return null;
            }

            @Override
            @Nullable
            String getNodeText(@NotNull PsiElement element, boolean useFullName) {
                return null;
            }
        };

        private final TokenSet tokens;
        private final String i18nKey;

        UsageType(TokenSet tokens, @NonNls String i18nKey) {
            this.tokens = tokens;
            this.i18nKey = i18nKey;
        }

        @Nullable
        String getType() {
            return I18N.msg(i18nKey);
        }
        
        @Nullable
        String getDescriptiveName(@NotNull PsiElement element) {
            return getShortName(element);
        }

        @Nullable
        String getNodeText(@NotNull PsiElement element, boolean useFullName) {
            return useFullName ? getFullName(element) : getShortName(element);
        }

        @Nullable
        String getShortName(@NotNull PsiElement element) {
            return getDefault(element);
        }

        @Nullable
        String getFullName(@NotNull PsiElement element) {
            return getDefault(element);
        }
    }
}
