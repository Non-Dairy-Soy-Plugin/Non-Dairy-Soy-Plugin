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
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import net.venaglia.nondairy.soylang.elements.LocalTemplateNameDef;
import net.venaglia.nondairy.soylang.elements.TemplateDefElement;
import org.jetbrains.annotations.NotNull;


/**
 * {@link TemplateDefElement} tokenizer for the spellchecker.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class LocalTemplateNameDefTokenizer extends Tokenizer<PsiElement> {

  @Override
  public void tokenize(@NotNull PsiElement element, TokenConsumer consumer) {
    if (element instanceof LocalTemplateNameDef) {
      consumer.consumeToken(element, TemplateNameSplitter.getInstance());
    }
  }
}
