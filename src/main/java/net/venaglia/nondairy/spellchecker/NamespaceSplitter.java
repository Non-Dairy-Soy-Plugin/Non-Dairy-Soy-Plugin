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

import com.intellij.openapi.util.TextRange;
import com.intellij.spellchecker.inspections.BaseSplitter;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Split namespaces on period (.) character.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class NamespaceSplitter extends BaseSplitter {
  private static final NamespaceSplitter INSTANCE = new NamespaceSplitter();
  
  public static NamespaceSplitter getInstance() {
    return INSTANCE;
  }

  @Override
  public void split(@Nullable String text, @NotNull TextRange range,
                    Consumer<TextRange> consumer) {
    if (text == null || range.getLength() < 1 || range.getStartOffset() < 0) {
      return;
    }

    int start = 0;
    for (int i = 0; i < text.length(); i++) {
      final char ch = text.charAt(i);
      if (ch == '.') {
        if (i - start > 1) {
          TextRange found = new TextRange(start, i);
          addWord(consumer, false /* ignore */, found);
        }
        start = i + 1;
      } else if ((i + 1) == text.length()) {
        i += 1;
        if (i - start > 1) {
          TextRange found = new TextRange(start, i);
          addWord(consumer, false /* ignore */, found);
        }
      }
    }
  }
}
