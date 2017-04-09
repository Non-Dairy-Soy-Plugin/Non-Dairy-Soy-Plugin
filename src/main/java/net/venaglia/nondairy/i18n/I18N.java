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

package net.venaglia.nondairy.i18n;

import org.jetbrains.annotations.NonNls;

import java.text.MessageFormat;
import java.util.*;

/**
 * User: ed
 * Date: Jul 31, 2010
 * Time: 4:21:08 PM
 * <p>
 * Primary class to serve up localized resources.
 */
public class I18N {

    @NonNls
    static final String RESOURCE_BUNDLE_NAME = "nondairy";

    private static final I18N INSTANCE = new I18N();

    private final Locale locale;

    private Map<String, MessageFormat> valuesByKey;

    private I18N() {
        locale = Locale.getDefault();
        reload();
    }

    public final void reload() {
        Package pkg = getClass().getPackage();
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(pkg.getName() + "." + RESOURCE_BUNDLE_NAME,
                    locale,
                    getClass().getClassLoader());
        } catch (MissingResourceException e) {
            bundle = ResourceBundle.getBundle(pkg.getName() + "." + RESOURCE_BUNDLE_NAME,
                    Locale.ENGLISH,
                    getClass().getClassLoader());
        }
        Enumeration<String> keyEnum = bundle.getKeys();
        Map<String, MessageFormat> valuesByKey = new HashMap<String, MessageFormat>();
        while (keyEnum.hasMoreElements()) {
            String key = keyEnum.nextElement();
            valuesByKey.put(key, new MessageFormat(bundle.getString(key), locale));
        }
        this.valuesByKey = Collections.unmodifiableMap(valuesByKey);
    }

    @NonNls
    public static String msg(@NonNls String key, Object... args) {
        MessageFormat message = INSTANCE.valuesByKey.get(key);
        if (message == null) {
            return key;
        }
        return message.format(args);
    }
}
