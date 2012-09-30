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
 * Splits a template body by excluding HTML tags and soy commands for
 * spellchecking.
 *
 * @author cpeisert{at}gmail{dot}com (Christopher Peisert)
 */
public class TemplateTagPairSplitter extends BaseSplitter {
  private static final TemplateTagPairSplitter INSTANCE =
      new TemplateTagPairSplitter();
  
  public static TemplateTagPairSplitter getInstance() {
    return INSTANCE;
  }

  private static final PlainTextSplitter plainTextSplitter =
      PlainTextSplitter.getInstance();

  private static final Pattern TEMPLATE =
      Pattern.compile("\\{template[^\\}]*?\\}(.*?)\\{/template\\s*\\}");
  private static final Pattern SOY_CMD_TAG =
      Pattern.compile("\\{/?\\S+?[^\\}]*?\\}(.*)");
  private static final Pattern HTML = Pattern.compile("</?\\S+?[^<>]*?>(.*)");

  @Override
  public void split(@Nullable String text, @NotNull TextRange range,
                    Consumer<TextRange> consumer) {
    if (text == null || range.getLength() < 1 || range.getStartOffset() < 0) {
      return;
    }

    List<TextRange> templateBodyList = excludeByPattern(text, range, TEMPLATE, 1);
    if (templateBodyList.isEmpty()) {
      return;
    }
    TextRange templateBody = templateBodyList.get(0);

    List<TextRange> templateBodyNoHtmlList = excludeByPattern(text,
        templateBody, HTML, 1);

    for (TextRange textRange : templateBodyNoHtmlList) {
      if (textRange.getLength() < 2) {
        continue;
      }
      List<TextRange> contentNoSoyCommandsList = excludeByPattern(text,
          textRange, SOY_CMD_TAG, 1);
      for (TextRange contentNoSoyCommands : contentNoSoyCommandsList) {
        plainTextSplitter.split(text, contentNoSoyCommands, consumer);
      }
    }
  }
}
