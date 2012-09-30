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
import com.intellij.spellchecker.inspections.TextSplitter;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;

import net.venaglia.nondairy.soylang.elements.AttributeElement;
import net.venaglia.nondairy.soylang.elements.NamespaceDefElement;
import net.venaglia.nondairy.soylang.elements.NamespaceTagElement;

import org.jetbrains.annotations.NotNull;


/**
 * {@link NamespaceTagElement} tokenizer for the spellchecker.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class NamespaceTagElementTokenizer extends Tokenizer<PsiElement> {

  @Override
  public void tokenize(@NotNull PsiElement element, TokenConsumer consumer) {

    if (element instanceof NamespaceTagElement
        || element instanceof AttributeElement) {
      for (PsiElement child : element.getChildren()) {
        tokenize(child, consumer);
      }
    } else if (element instanceof NamespaceDefElement) {
      consumer.consumeToken(element, NamespaceSplitter.getInstance());
    } else if (element instanceof AttributeElement.Key) {
      consumer.consumeToken(element, AutoescapeSplitter.getInstance());
    } else if (element instanceof AttributeElement.Value) {
      consumer.consumeToken(element, TextSplitter.getInstance());
    } else {
      if (element.getChildren().length == 0) {
        consumer.consumeToken(element, IgnoreElementSplitter.getInstance());
      }
      for (PsiElement child : element.getChildren()) {
        tokenize(child, consumer);
      }
    }
  }
}
