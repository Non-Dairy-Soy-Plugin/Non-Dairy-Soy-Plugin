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

package net.venaglia.nondairy.spellchecker;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import net.venaglia.nondairy.soylang.SoyElement;
import net.venaglia.nondairy.soylang.elements.LocalTemplateNameDef;
import net.venaglia.nondairy.soylang.elements.NamespaceTagElement;
import org.jetbrains.annotations.NotNull;


/**
 * {@link PsiElement} tokenizer for the spellchecker.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class PsiElementTokenizer extends Tokenizer<PsiElement> {
  private final NamespaceTagElementTokenizer namespaceTokenizer =
      new NamespaceTagElementTokenizer();
  private final LocalTemplateNameDefTokenizer localTemplateNameDefTokenizer =
      new LocalTemplateNameDefTokenizer();
  private final TemplateDocCommentTokenizer templateDocCommentTokenizer =
      new TemplateDocCommentTokenizer();
  private TemplateTagPairTokenizer templateTagPairTokenizer =
      new TemplateTagPairTokenizer();

  @Override
  public void tokenize(@NotNull PsiElement element, TokenConsumer consumer) {
    if (element instanceof NamespaceTagElement) {
      namespaceTokenizer.tokenize(element, consumer);
    } else {
      IElementType elementType = element.getNode().getElementType();

      if (elementType.equals(SoyElement.template_tag_pair)) {
        LocalTemplateNameDef templateNameDef =
            getChildLocalTemplateNameDef(element);
        if (templateNameDef != null) {
          localTemplateNameDefTokenizer.tokenize(templateNameDef, consumer);
        }
        templateTagPairTokenizer.tokenize(element, consumer);
      } else if (elementType.equals(SoyElement.doc_comment)) {
        templateDocCommentTokenizer.tokenize(element, consumer);
      } else {
        for (PsiElement child : element.getChildren()) {
          tokenize(child, consumer);
        }
      }
    }
  }

  /**
   * Traverses PSI tree to find child {@link LocalTemplateNameDef} element.
   *
   * @param parent Parent PsiElement.
   * @return The {@link LocalTemplateNameDef} or {@code null} if not found.
   */
  private LocalTemplateNameDef getChildLocalTemplateNameDef(PsiElement parent) {
    for (PsiElement child : parent.getChildren()) {
      if (child instanceof LocalTemplateNameDef) {
        return (LocalTemplateNameDef) child;
      } else {
        LocalTemplateNameDef templateNameDef =
            getChildLocalTemplateNameDef(child);
        if (templateNameDef != null) {
          return templateNameDef;
        }
      }
    }
    return  null;
  }
}
