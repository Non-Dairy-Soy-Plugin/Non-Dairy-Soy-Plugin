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
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import net.venaglia.nondairy.soylang.SoyFile;
import org.jetbrains.annotations.NotNull;

/**
 * Spellchecking strategy specifying how the words in Closure Templates are
 * tokenized and supplied to the spellchecker.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class SoySpellcheckingStrategy extends SpellcheckingStrategy {
  private final PsiElementTokenizer tokenizer = new PsiElementTokenizer();

  @NotNull
  @Override
  public Tokenizer getTokenizer(PsiElement element) {
    //System.out.println(DebugUtil.print(element));

    if (element instanceof SoyFile) {
      return tokenizer;
    }

    return EMPTY_TOKENIZER;
  }
}
