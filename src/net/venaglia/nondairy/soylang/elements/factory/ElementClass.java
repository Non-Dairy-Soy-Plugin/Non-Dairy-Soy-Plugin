/*
 * Copyright 2010 Ed Venaglia
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

package net.venaglia.nondairy.soylang.elements.factory;

import static java.lang.annotation.ElementType.*;

import net.venaglia.nondairy.soylang.elements.SoyASTElement;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Aug 24, 2010
 * Time: 8:43:41 PM
 *
 * An annotation used on the fields defining SoyElement instances that
 * identifies the SoyASTElement subclass that should be constructed to hold AST
 * nodes of that type.
 * <p>
 * Any SoyElement field not identified with this annotation is contained within
 * an instance of the base SoyASTElement.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD })
public @interface ElementClass {
    Class<? extends SoyASTElement> value();
}
