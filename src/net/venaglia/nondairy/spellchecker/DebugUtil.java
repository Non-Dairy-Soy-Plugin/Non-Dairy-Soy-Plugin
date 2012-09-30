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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA. User: cpeisert Date: 9/22/12
 */
class DebugUtil {
  /**
   * Produces a human readable, multi-line string, that describes the passed
   * element and its children as a tree.
   * @param element The element to be described.
   * @return A multi-line indented string, representing a tree structure.
   */
  public static String print(PsiElement element) {
    StringWriter buffer = new StringWriter(4096);
    print(element, "", new AtomicInteger(), new PrintWriter(buffer));
    return buffer.toString();
  }

  private static void print(PsiElement element, String indent, AtomicInteger count, PrintWriter out) {
    count.getAndIncrement();
    out.print(indent);
    out.print(element + "   CLASS => ");
    out.println(element.getClass().getName());
    PsiElement[] children = element.getChildren();
    if (children.length > 0) {
      String childIndent = indent + "    ";
      for (PsiElement child : children) {
        print(child, childIndent, count, out);
      }
    }
  }
}
