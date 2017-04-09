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
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Split identifiers ignoring occurrences of "autoescape".
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class AutoescapeSplitter extends BaseSplitter {
  private static final AutoescapeSplitter INSTANCE = new AutoescapeSplitter();
  
  public static AutoescapeSplitter getInstance() {
    return INSTANCE;
  }

  private static final Pattern IGNORE_AUTOESCAPE =
      Pattern.compile("autoescape(.*)");
  private static final PlainTextSplitter plainTextSplitter =
      PlainTextSplitter.getInstance();

  @Override
  public void split(@Nullable String text, @NotNull TextRange range,
                    Consumer<TextRange> consumer) {
    if (text == null || range.getLength() < 1 || range.getStartOffset() < 0) {
      return;
    }

    List<TextRange> extracted = excludeByPattern(text, range,
        IGNORE_AUTOESCAPE, 1);

    for (TextRange textRange : extracted) {
      if (textRange.getLength() < 2) {
        continue;
      }
      plainTextSplitter.split(text, textRange, consumer);
    }
  }
}
