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

package net.venaglia.nondairy.i18n;

import org.jetbrains.annotations.NonNls;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: ed
 * Date: Jul 31, 2010
 * Time: 4:21:08 PM
 */
public class I18N {

    @NonNls
    static final String RESOURCE_BUNDLE_NAME = "nondairy";

    private static final I18N INSTANCE = new I18N();

    private Map<String,String> valuesByKey;

    private I18N() {
        reload();
    }

    public final void reload() {
        Class<? extends I18N> klass = getClass();
        ResourceBundle bundle = ResourceBundle.getBundle(klass.getPackage().getName() + "." + RESOURCE_BUNDLE_NAME,
                                                         Locale.getDefault(),
                                                         klass.getClassLoader());
        Enumeration<String> keyEnum = bundle.getKeys();
        Map<String,String> valuesByKey = new HashMap<String, String>();
        while (keyEnum.hasMoreElements()) {
            String key = keyEnum.nextElement();
            valuesByKey.put(key, bundle.getString(key));
        }
        this.valuesByKey = Collections.unmodifiableMap(valuesByKey);
    }

    @NonNls
    public static String msg(@NonNls String key, Object... args) {
        String message = INSTANCE.valuesByKey.get(key);
        if (message == null) return message;
        return MessageFormat.format(message, args);
    }
}
